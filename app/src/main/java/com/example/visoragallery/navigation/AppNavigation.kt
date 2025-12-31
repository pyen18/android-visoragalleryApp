package com.example.visoragallery.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.visoragallery.data.NavigationItem
import com.example.visoragallery.ui.screens.*
import com.example.visoragallery.ui.screens.albums.AlbumsScreen
import com.example.visoragallery.ui.screens.singlephoto.SinglePhotoScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = NavigationItem.Photos.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main tabs
        composable(NavigationItem.Photos.route) {
            PhotosScreen(navController = navController)
        }

        composable(NavigationItem.Albums.route) {
            AlbumsScreen(navController = navController)
        }

        composable(NavigationItem.Search.route) {
            SearchScreen()
        }

        composable(NavigationItem.More.route) {
            MoreScreen(navController = navController)
        }

        // Single photo viewer
        composable(
            route = "single_photo/{photoIndex}",
            arguments = listOf(
                navArgument("photoIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            val photoPaths = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Array<String>>("photoPaths") ?: emptyArray()

            if (photoPaths.isNotEmpty()) {
                SinglePhotoScreen(
                    navController = navController,
                    photoPaths = photoPaths,
                    initialPosition = photoIndex
                )
            }
        }
    }
}