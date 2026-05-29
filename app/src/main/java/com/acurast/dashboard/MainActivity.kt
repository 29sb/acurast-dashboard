package com.acurast.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.acurast.dashboard.ui.screens.OverviewScreen
import com.acurast.dashboard.ui.screens.WalletScreen
import com.acurast.dashboard.ui.theme.AcurastDashboardTheme
import com.acurast.dashboard.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcurastDashboardTheme {
                AcurastDashboardApp()
            }
        }
    }
}

/**
 * 底部导航栏项
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Overview : BottomNavItem("overview", "网络概览", Icons.Default.Cloud)
    data object Wallet : BottomNavItem("wallet", "钱包查询", Icons.Default.AccountBalanceWallet)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcurastDashboardApp() {
    val navController = rememberNavController()
    val viewModel: DashboardViewModel = viewModel()
    val navItems = listOf(BottomNavItem.Overview, BottomNavItem.Wallet)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Overview.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNavItem.Overview.route) {
                OverviewScreen(viewModel)
            }
            composable(BottomNavItem.Wallet.route) {
                WalletScreen(viewModel)
            }
        }
    }
}