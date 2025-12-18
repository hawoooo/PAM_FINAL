package com.mobile.quickbite.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

object BottomNavConfig {
    val items = listOf(
        BottomNavItem(
            title = "Menu", // Dulu Search
            route = Screen.Menu.route,
            icon = Icons.Default.Search
        ),
        BottomNavItem(
            title = "Riwayat",
            route = Screen.History.route,
            icon = Icons.Default.History
        )
    )
}