package com.mobile.quickbite.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mobile.quickbite.ui.nav.BottomNavConfig

@Composable
fun BottomNavigationBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        BottomNavConfig.items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                label = { Text(item.title) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                selected = isSelected,
                onClick = {
                    // Cek agar tidak reload jika tab yang sama diklik
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // LOGIKA BARU YANG LEBIH STABIL:
                            // 1. Pop sampai ke Start Destination (Menu) untuk hindari tumpukan
                            popUpTo(navController.graph.findStartDestination().id) {
                            }
                            // 2. Hindari instance ganda
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}