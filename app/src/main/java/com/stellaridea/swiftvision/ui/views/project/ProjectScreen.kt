// ProjectScreen.kt
package com.stellaridea.swiftvision.ui.views.project

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.navigation.GraphRoot


@Composable
fun ProjectScreen(navController: NavHostController) {
    val viewModel: ProjectViewModel = hiltViewModel()
    val projects by viewModel.projects.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    var filteredProjects by remember { mutableStateOf(projects) }
    val isRefreshing by remember { mutableStateOf(false) }

    val userName = viewModel.userPreferences.getUserName() ?: "Usuario"
    val userEmail = viewModel.userPreferences.getUserEmail() ?: "Correo no disponible"

    LaunchedEffect(Unit) {
        viewModel.getProjects {
            Toast.makeText(navController.context, "Error al cargar proyectos", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(projects) {
        filteredProjects = projects
    }

    fun handleSearch(query: String) {
        filteredProjects = if (query.isEmpty()) {
            projects
        } else {
            projects.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBarSample(
                onSearch = { query -> handleSearch(query) },
                onLogout = {
                    viewModel.userPreferences.clearUserData()
                    navController.navigate(GraphRoot.LOGIN) {
                        popUpTo(0)
                    }
                },
                userName = userName,
                userEmail = userEmail
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Graph.CAMERA) }
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Abrir cámara"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SwipeRefresh(
                state = SwipeRefreshState(isRefreshing),
                onRefresh = {
                    viewModel.getProjects {
                        Toast.makeText(
                            navController.context,
                            "Error al recargar proyectos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProjects) { project ->
                            ProjectItem(
                                project = project,
                                onEdit = { newName ->
                                    viewModel.updateProject(
                                        project.id,
                                        newName,
                                        {},
                                        {}
                                    )
                                },
                                onDelete = {
                                    viewModel.deleteProject(
                                        project.id,
                                        {},
                                        {}
                                    )
                                },
                                onNavigate = { navController.navigate("edition/${project.id}/${project.name}")
                                }
                            )
                        }
                        // Agregamos un Spacer al final
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }
}