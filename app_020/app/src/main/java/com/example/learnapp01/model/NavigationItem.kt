package com.example.learnapp01.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

object NavigationItems {
    val bottomNavItems = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("search", "Search", Icons.Default.Search),
        BottomNavItem("settings", "Settings", Icons.Default.Settings),
        BottomNavItem("players", "Players", Icons.Default.Person)
    )
}