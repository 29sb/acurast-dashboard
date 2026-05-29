package com.acurast.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.acurast.dashboard.viewmodel.DashboardViewModel
import java.math.BigDecimal

/**
 * 钱包查询页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: DashboardViewModel) {
    val searchAddress by viewModel.accountSearch.collectAsState()
    val accountInfo by viewModel.accountInfo.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("钱包查询", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 搜索输入区域
            Text(
                text = "查询 Acurast 链上地址",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "输入 Substrate 格式地址（以 5 开头）查看余额和状态",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            // 搜索框
            OutlinedTextField(
                value = searchAddress,
                onValueChange = { viewModel.updateSearchAddress(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("钱包地址") },
                placeholder = { Text("5...") },
                leadingIcon = {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                },
                trailingIcon = {
                    if (searchAddress.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.clearAccountInfo()
                            viewModel.updateSearchAddress("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "清空")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.searchAddress()
                    }
                ),
                enabled = !isSearching
            )

            Spacer(Modifier.height(12.dp))

            // 搜索按钮
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.searchAddress()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = searchAddress.isNotBlank() && !isSearching
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("查询中...")
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("查询")
                }
            }

            Spacer(Modifier.height(24.dp))

            // 显示结果
            accountInfo?.let { info ->
                if (info.isSuccess) {
                    // 成功结果卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // 地址
                            Text(
                                text = "地址信息",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = info.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                            // 余额
                            DetailRowWithIcon(
                                icon = Icons.Outlined.Info,
                                label = "ACU 余额",
                                value = formatBalanceDisplay(info.balance)
                            )

                            Spacer(Modifier.height(12.dp))

                            // 处理器状态
                            DetailRowWithIcon(
                                icon = Icons.Outlined.PhoneAndroid,
                                label = "处理器注册",
                                value = if (info.isRegisteredProcessor) "已注册" else "未注册",
                                valueColor = if (info.isRegisteredProcessor)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // 错误结果卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "查询失败",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                info.errorMessage?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRowWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun formatBalanceDisplay(balance: BigDecimal): String {
    return when {
        balance >= BigDecimal("1000000") ->
            "${balance.divide(BigDecimal("1000000")).setScale(2, BigDecimal.ROUND_HALF_UP)}M ACU"
        balance >= BigDecimal("1000") ->
            "${balance.divide(BigDecimal("1000")).setScale(2, BigDecimal.ROUND_HALF_UP)}K ACU"
        balance >= BigDecimal.ONE ->
            "${balance.setScale(2, BigDecimal.ROUND_HALF_UP)} ACU"
        balance > BigDecimal.ZERO ->
            "${balance.setScale(4, BigDecimal.ROUND_HALF_UP)} ACU"
        else -> "0 ACU"
    }
}