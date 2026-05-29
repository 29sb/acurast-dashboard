package com.acurast.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acurast.dashboard.data.AccountInfo
import com.acurast.dashboard.data.AcurastNetwork
import com.acurast.dashboard.data.AcurastRepository
import com.acurast.dashboard.data.NetworkOverview
import com.acurast.dashboard.data.StatItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Acurast Dashboard 主 ViewModel
 */
class DashboardViewModel : ViewModel() {

    // 默认用主网
    private var _currentNetwork = AcurastNetwork.MAINNET
    private var repository = AcurastRepository(_currentNetwork)

    // 当前网络
    private val _network = MutableStateFlow(_currentNetwork)
    val network: StateFlow<AcurastNetwork> = _network.asStateFlow()

    // 网络概览状态
    private val _overview = MutableStateFlow(NetworkOverview())
    val overview: StateFlow<NetworkOverview> = _overview.asStateFlow()

    // 统计列表状态
    private val _stats = MutableStateFlow<List<StatItem>>(emptyList())
    val stats: StateFlow<List<StatItem>> = _stats.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误信息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 账户搜索状态
    private val _accountSearch = MutableStateFlow("")
    val accountSearch: StateFlow<String> = _accountSearch.asStateFlow()

    private val _accountInfo = MutableStateFlow<AccountInfo?>(null)
    val accountInfo: StateFlow<AccountInfo?> = _accountInfo.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        loadNetworkData()
    }

    /**
     * 切换网络
     */
    fun switchNetwork(targetNetwork: AcurastNetwork) {
        if (_currentNetwork == targetNetwork) return
        _currentNetwork = targetNetwork
        _network.value = targetNetwork
        repository = AcurastRepository(targetNetwork)
        // 清空旧数据并重新加载
        _accountInfo.value = null
        _overview.value = NetworkOverview()
        _stats.value = emptyList()
        loadNetworkData()
    }

    /**
     * 加载网络数据
     */
    fun loadNetworkData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _overview.value = repository.getNetworkOverview()
                _stats.value = repository.getStats()
            } catch (e: Exception) {
                _errorMessage.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 更新搜索地址
     */
    fun updateSearchAddress(address: String) {
        _accountSearch.value = address
    }

    /**
     * 搜索钱包地址
     */
    fun searchAddress() {
        val address = _accountSearch.value.trim()
        if (address.isEmpty()) return

        viewModelScope.launch {
            _isSearching.value = true
            _accountInfo.value = null
            try {
                _accountInfo.value = repository.getAccountInfo(address)
            } catch (e: Exception) {
                _accountInfo.value = AccountInfo(
                    address = address,
                    isSuccess = false,
                    errorMessage = "查询失败: ${e.message}"
                )
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * 清空账户搜索结果
     */
    fun clearAccountInfo() {
        _accountInfo.value = null
        _accountSearch.value = ""
    }
}