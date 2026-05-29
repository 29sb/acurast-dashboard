package com.acurast.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.acurast.dashboard.data.AcurastNetwork
import com.acurast.dashboard.data.StatItem
import com.acurast.dashboard.viewmodel.DashboardViewModel

/**
 * 网络概览页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(viewModel: DashboardViewModel) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentNetwork by viewModel.network.collectAsState()
    var showNetworkMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Acurast 网络", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    // 网络切换按钮
                    Box {
                        IconButton(onClick = { showNetworkMenu = true }) {
                            Icon(Icons.Outlined.Link, contentDescription = "切换网络")
                        }
                        DropdownMenu(
                            expanded = showNetworkMenu,
                            onDismissRequest = { showNetworkMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (currentNetwork == AcurastNetwork.MAINNET) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text("Mainnet（主网）")
                                    }
                                },
                                onClick = {
                                    showNetworkMenu = false
                                    viewModel.switchNetwork(AcurastNetwork.MAINNET)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (currentNetwork == AcurastNetwork.CANARY) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text("Canary（测试网）")
                                    }
                                },
                                onClick = {
                                    showNetworkMenu = false
                                    viewModel.switchNetwork(AcurastNetwork.CANARY)
                                }
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.loadNetworkData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("加载网络数据...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadNetworkData() }) {
                            Text("重试")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 标题卡片
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Cloud,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Acurast Network",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "去中心化手机算力网络",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // 统计卡片列表
                        items(stats) { stat ->
                            StatCard(stat)
                        }

                        // 底部信息
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "数据来源: ${currentNetwork.displayName} RPC",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(stat: StatItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标（根据标签选择）
            val icon = when {
                stat.label.contains("处理器") || stat.label.contains("Provider") ->
                    Icons.Outlined.Devices
                stat.label.contains("任务") || stat.label.contains("Task") ->
                    Icons.Outlined.TaskAlt
                stat.label.contains("链") || stat.label.contains("区块") ->
                    Icons.Outlined.Link
                else -> Icons.Outlined.Link
            }

            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}