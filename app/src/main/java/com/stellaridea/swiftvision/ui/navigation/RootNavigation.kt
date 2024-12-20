package com.stellaridea.swiftvision.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.stellaridea.swiftvision.ui.views.camera.CameraScreen
import com.stellaridea.swiftvision.ui.views.edition.EditionScreen
import com.stellaridea.swiftvision.ui.views.login.LoginScreen
import com.stellaridea.swiftvision.ui.views.project.ProjectScreen
import com.stellaridea.swiftvision.ui.views.register.RegisterScreen

@Composable
fun RootNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = GraphRoot.LOGIN
    ) {
        loginNav(navController)
        homeNav(navController)
    }
}

fun NavGraphBuilder.loginNav(navController: NavHostController) {
    navigation(
        startDestination = Graph.LOGIN,
        route = GraphRoot.LOGIN
    ) {
        composable(route = Graph.LOGIN) {
            LoginScreen(navController = navController)
        }
        composable(route = Graph.REGISTER) {
            RegisterScreen(navController = navController)
        }
    }
}

@SuppressLint("UnrememberedGetBackStackEntry")
fun NavGraphBuilder.homeNav(navController: NavHostController) {
    navigation(
        startDestination = Graph.PROJECTS,
        route = GraphRoot.HOME
    ) {
        composable(route = Graph.PROJECTS) {
            ProjectScreen(navController = navController)
        }
        composable(route = Graph.CAMERA) {
            CameraScreen(navController = navController)
        }
        composable(
            route = Graph.EDITION,
            arguments = listOf(
                navArgument("projectId") { type = NavType.IntType },
                navArgument("projectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
            val projectName = backStackEntry.arguments?.getString("projectName") ?: ""
            EditionScreen(navController = navController, projectId = projectId, projectName = projectName)
        }
    }
}


object GraphRoot {
    const val LOGIN = "loginRoot"
    const val HOME = "homeRoot"
}

object Graph {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PROJECTS = "projects"
    const val CAMERA = "camera"
    const val EDITION = "edition/{projectId}/{projectName}"
}
