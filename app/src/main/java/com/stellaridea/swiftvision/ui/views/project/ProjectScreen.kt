package com.stellaridea.swiftvision.ui.views.project

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.models.projects.Project
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.navigation.GraphRoot
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun ProjectScreen(navController: NavHostController) {
    val viewModel: ProjectViewModel = hiltViewModel()
    val projects by viewModel.projects.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    var filteredProjects by remember { mutableStateOf(projects) }

    // Obtener datos reales del usuario
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
                        popUpTo(0) // Limpia el backstack
                    }
                },
                userName = userName,
                userEmail = userEmail
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Graph.CAMERA)
                }
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
                                viewModel.deleteProject(project.id, {}, {})
                            },
                            onNavigate = { navController.navigate(Graph.CAMERA) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItem(
    project: Project,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onNavigate: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(project.name) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Proyecto") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre del proyecto") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onEdit(newName)
                    showEditDialog = false
                }) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Mostrar la imagen del proyecto (si existe y es válida)
            val firstImage = project.images.firstOrNull()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (firstImage?.bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = firstImage.bitmap.asImageBitmap(),
                        contentDescription = "Imagen del proyecto",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            // Nombre del proyecto
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar proyecto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar proyecto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = { onNavigate() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Abrir proyecto",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

