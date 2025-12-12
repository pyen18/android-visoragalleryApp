package com.example.learnapp01.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.learnapp01.screens.home.HomeScreen
import com.example.learnapp01.screens.players.PlayersScreen
import com.example.learnapp01.screens.search.SearchScreen
import com.example.learnapp01.screens.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen() }
        composable("search") { SearchScreen() }
        composable("settings") { SettingsScreen() }
        composable("players") { PlayersScreen() }
    }
}