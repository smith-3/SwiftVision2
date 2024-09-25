package com.stellaridea.swiftvision

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.stellaridea.swiftvision.ui.navigation.RootNavigationGraph
import com.stellaridea.swiftvision.ui.theme.SwiftVisionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                println("$permissionName granted")
            } else {
                println("$permissionName denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )

        permissionLauncher.launch(permissions)

        setContent {
            SwiftVisionTheme(darkTheme = true) {
                Surface() {
                    val navController = rememberNavController()
                    RootNavigationGraph(navController)
                }
            }
        }
    }
}