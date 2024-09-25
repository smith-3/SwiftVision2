package com.stellaridea.swiftvision.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.stellaridea.swiftvision.ui.views.camera.CameraScreen
import com.stellaridea.swiftvision.ui.views.image.ImageDetailScreen

@Composable
fun RootNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = GraphRoot.HOME
    ) {
        homeNav(navController)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnrememberedGetBackStackEntry")
fun NavGraphBuilder.homeNav(navController: NavHostController) {
    navigation(
        startDestination = Graph.CAMERA,
        route = GraphRoot.HOME
    ) {
        composable(
            route = Graph.CAMERA
        ) {
            CameraScreen(navController = navController)
        }
        composable(
            route = Graph.IMAGE_SAM
        ) {
            // Aquí también puedes obtener el ViewModel si es necesario
        }
        composable(
            route = Graph.IMAGE_DETAIL
        ) {
            ImageDetailScreen(navController = navController)
        }
    }
}

object GraphRoot {
    const val LOGIN = "loginRoot"
    const val HOME = "homeRoot"
}
object Graph {
    const val EXIT = "exitGraph"
    const val HOME = "homeGraph"
    const val CAMERA = "camera"
    const val GALLERY = "gallery"
    const val IMAGE_SAM = "imageSam"
    const val IMAGE_DETAIL = "imageDetail"
}
