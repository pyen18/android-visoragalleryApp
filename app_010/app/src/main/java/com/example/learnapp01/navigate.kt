package com.example.learnapp01



import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learnapp01.ui.splash.SplashScreen
import com.example.learnapp01.ui.players.PlayersScreen
import com.example.learnapp01.ui.playerdetail.PlayerDetailScreen

/**
 * Navigation routes cho app
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Players : Screen("players")
    object PlayerDetail : Screen("player_detail/{playerName}") {
        fun createRoute(playerName: String) = "player_detail/$playerName"
    }
}

/**
 * Setup navigation graph cho app
 * Flow: Splash -> Players (với Modal Bottom Sheet) -> Player Detail
 */
@Composable
fun T1Navigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToPlayers = {
                    navController.navigate(Screen.Players.route) {
                        // Remove splash from back stack
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Players.route) {
            PlayersScreen(
                onPlayerClick = { playerName ->
                    navController.navigate(Screen.PlayerDetail.createRoute(playerName))
                }
            )
        }

        composable(Screen.PlayerDetail.route) { backStackEntry ->
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            PlayerDetailScreen(
                playerName = playerName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}