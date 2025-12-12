package com.example.visoragallery

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.visoragallery.settings.AppConfig
import com.example.visoragallery.ui.screens.MainScreen
import com.example.visoragallery.ui.theme.VisoraGalleryTheme

class MainActivity : ComponentActivity() {

    private lateinit var appConfig: AppConfig

    private val manageStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    finish()
                }
            }
        }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appConfig = AppConfig.getInstance(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(appConfig.getNightMode()) }

            VisoraGalleryTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Runtime permissions
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        listOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE
                        )
                    } else {
                        listOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE
                        )
                    }

                    val permissionsState = rememberMultiplePermissionsState(permissions)

                    MainScreen(
                        permissionsState = permissionsState,
                        onRequestManageStorage = { requestManageStoragePermission() }
                    )
                }
            }
        }
    }

    private fun requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                manageStoragePermissionLauncher.launch(intent)
            } catch (_: Exception) {
                manageStoragePermissionLauncher.launch(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    VisoragalleryTheme {
//        Greeting("Android")
//    }
//}