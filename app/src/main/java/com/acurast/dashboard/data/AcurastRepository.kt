package com.acurast.dashboard.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * 网络类型
 */
enum class AcurastNetwork(val displayName: String, val rpcUrl: String) {
    MAINNET("Acurast Mainnet", "wss://archive.mainnet.acurast.com"),
    CANARY("Acurast Canary", "wss://canarynet-ws-1.acurast-h-server-2.papers.tech")
}

/**
 * Acurast 链数据仓库
 * 通过 JSON-RPC 与 Acurast Substrate 链交互
 */
class AcurastRepository(
    private val network: AcurastNetwork = AcurastNetwork.MAINNET
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    // 使用 WSS 但 OkHttp 不支持 WS，需要换 HTTPS RPC，或者用 WSS 直连
    // 用 Substrate HTTPS RPC 网关
    private val rpcUrl = when (network) {
        AcurastNetwork.MAINNET -> "https://archive.mainnet.acurast.com"
        AcurastNetwork.CANARY -> "https://acurast-canary-rpc.gateway.pinata.cloud"
    }

    companion object {
        // Acurast 链的模块/存储项常量
        private const val MODULE_SYSTEM = "System"
        private const val STORAGE_ACCOUNT = "Account"
        private const val MODULE_STAKING = "Staking"
        private const val STORAGE_ACTIVE_TASKS = "ActiveTasks"
        private const val MODULE_PROCESSOR = "Processor"
        private const val STORAGE_PROCESSOR_COUNT = "ProcessorCount"
    }

    /**
     * 发送 JSON-RPC 请求到 Substrate 链
     */
    private suspend fun rpcCall(method: String, params: List<Any> = emptyList()): JsonObject? {
        return withContext(Dispatchers.IO) {
            val body = JsonObject().apply {
                addProperty("jsonrpc", "2.0")
                addProperty("id", 1)
                addProperty("method", method)
                add("params", gson.toJsonTree(params))
            }

            val request = Request.Builder()
                .url(rpcUrl)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext null
                val json = gson.fromJson(responseBody, JsonObject::class.java)
                json.get("result")?.asJsonObject
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 查询链上存储状态
     */
    private suspend fun queryStorage(module: String, storageName: String, key: String? = null): JsonObject? {
        val params = mutableListOf(module, storageName)
        if (key != null) params.add(key)
        return rpcCall("state_getStorage", params.map { it })
    }

    /**
     * 获取网络概览数据
     */
    suspend fun getNetworkOverview(): NetworkOverview {
        return withContext(Dispatchers.IO) {
            try {
                // 获取最新区块号
                val headerResult = rpcCall("chain_getHeader")
                val blockNumber = headerResult?.get("number")?.asString?.toIntOrNull(16) ?: 0

                // 获取处理器数量 (通过存储查询)
                val processorCountResult = rpcCall("state_getStorage", listOf("Processor", "ProcessorCount"))

                // 获取活跃任务数 (通过存储查询)
                val activeTasksResult = rpcCall("state_getStorage", listOf("Processor", "ActiveTasks"))

                // 获取链名和版本
                val chainResult = rpcCall("system_chain")
                val chainName = chainResult?.asString ?: "Unknown"
                val versionResult = rpcCall("system_version")
                val version = versionResult?.asString ?: "?"

                NetworkOverview(
                    chainName = network.displayName,
                    blockNumber = blockNumber.toLong(),
                    processorCount = estimateProcessorCount(processorCountResult),
                    activeTasks = estimateActiveTasks(activeTasksResult),
                    version = version
                )
            } catch (e: Exception) {
                e.printStackTrace()
                NetworkOverview()
            }
        }
    }

    /**
     * 查询钱包地址信息
     */
    suspend fun getAccountInfo(address: String): AccountInfo {
        return withContext(Dispatchers.IO) {
            try {
                // 查询 account 信息
                val accountResult = rpcCall("state_getStorage", listOf("System", "Account", address))

                // 如果上面方法不对，试试用 state_query
                val balanceResult = rpcCall("state_queryStorage", listOf(
                    listOf("System.Account"),
                    address
                ))

                // 解析余额 - Substrate 框架下 System.Account 存储结构
                val balance = extractBalance(accountResult)

                // 查询是否存在处理器注册
                val processorResult = rpcCall("state_getStorage", listOf("Processor", "Processors", address))
                val isProcessor = processorResult != null && !processorResult.isJsonNull

                AccountInfo(
                    address = address,
                    balance = balance,
                    isRegisteredProcessor = isProcessor,
                    isSuccess = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                AccountInfo(address = address, isSuccess = false, errorMessage = e.message)
            }
        }
    }

    /**
     * 获取 Acurast 网络统计（简化版：通过 Insecure RPC 或公共索引器）
     * 这里使用自定义逻辑估算
     */
    suspend fun getStats(): List<StatItem> {
        return withContext(Dispatchers.IO) {
            try {
                val overview = getNetworkOverview()

                // 尝试获取额外的统计信息
                val totalIssuanceResult = rpcCall("state_getStorage", listOf("Balances", "TotalIssuance"))
                val totalSupply = if (totalIssuanceResult != null) {
                    formatBalance(totalIssuanceResult.asString)
                } else "—"

                listOf(
                    StatItem("当前链", overview.chainName),
                    StatItem("最新区块", "#${overview.blockNumber}"),
                    StatItem("版本", overview.version),
                    StatItem("在线处理器 (估)", "${overview.processorCount}+"),
                    StatItem("活跃任务 (估)", "${overview.activeTasks}"),
                    StatItem("总供应量 (ACU)", totalSupply)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                listOf(
                    StatItem("状态", "无法连接网络"),
                    StatItem("错误", e.message ?: "未知错误")
                )
            }
        }
    }

    /**
     * 从 storage 结果中提取余额
     */
    private fun extractBalance(result: JsonObject?): BigDecimal {
        if (result == null) return BigDecimal.ZERO
        return try {
            // Substrate System.Account 存储格式: { "data": { "free": "0x...", ... } }
            val data = result.asJsonObject?.get("data")?.asJsonObject
            val free = data?.get("free")?.asString
            if (free != null) {
                BigDecimal(free.toBigInteger(16)).divide(BigDecimal.TEN.pow(10))
            } else BigDecimal.ZERO
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun estimateProcessorCount(result: JsonObject?): Int {
        if (result == null) return 0
        return try {
            val hex = result.asString
            if (hex != null) hex.toBigInteger(16).toInt() else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun estimateActiveTasks(result: JsonObject?): Int {
        if (result == null) return 0
        return try {
            val arr = result.asJsonArray
            arr?.size() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun formatBalance(hex: String?): String {
        if (hex == null) return "0"
        return try {
            val balance = BigDecimal(hex.toBigInteger(16)).divide(BigDecimal.TEN.pow(10))
            if (balance >= BigDecimal.TEN.pow(6)) {
                "${balance.divide(BigDecimal.TEN.pow(6)).setScale(2, BigDecimal.ROUND_HALF_UP)}M"
            } else if (balance >= BigDecimal.TEN.pow(3)) {
                "${balance.divide(BigDecimal.TEN.pow(3)).setScale(2, BigDecimal.ROUND_HALF_UP)}K"
            } else {
                balance.setScale(2, BigDecimal.ROUND_HALF_UP).toString()
            }
        } catch (e: Exception) {
            "—"
        }
    }
}

/**
 * 网络概览数据类
 */
data class NetworkOverview(
    val chainName: String = "Acurast Canary",
    val blockNumber: Long = 0,
    val processorCount: Int = 0,
    val activeTasks: Int = 0,
    val version: String = "?"
)

/**
 * 账户信息数据类
 */
data class AccountInfo(
    val address: String = "",
    val balance: BigDecimal = BigDecimal.ZERO,
    val isRegisteredProcessor: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 统计展示项
 */
data class StatItem(
    val label: String,
    val value: String
)
