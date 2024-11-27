package com.stellaridea.swiftvision.ui.views.project

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.models.projects.ProjectResponse
import com.stellaridea.swiftvision.ui.navigation.Graph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(navController: NavHostController) {
    val viewModel: ProjectViewModel = hiltViewModel()
    val projects by viewModel.projects.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    var showDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getProjects(onFailure = {
            Toast.makeText(navController.context, "Error al cargar proyectos", Toast.LENGTH_SHORT).show()
        })
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
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
                Text("Proyectos", style = MaterialTheme.typography.titleLarge)
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(projects) { project ->
                        ProjectItem(
                            project = project,
                            onEdit = { newName -> viewModel.updateProject(project.id, newName, {}, {}) },
                            onDelete = { viewModel.deleteProject(project.id, {}, {}) },
                            onNavigate = { navController.navigate(Graph.CAMERA) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Crear Proyecto") },
            text = {
                Column {
                    TextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Nombre del proyecto") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.createProject(
                        name = projectName,
                        onSuccess = {
                            showDialog = false
                            projectName = ""
                            navController.navigate(Graph.CAMERA)
                        },
                        onFailure = {
                            showDialog = false
                            Toast.makeText(navController.context, "Error al crear proyecto", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text("Siguiente")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectItem(
    project: ProjectResponse,
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = project.name, style = MaterialTheme.typography.bodyLarge)
        }
        Button(onClick = { showEditDialog = true }) { Text("Editar") }
        Button(onClick = { onDelete() }) { Text("Eliminar") }
        Button(onClick = { onNavigate() }) { Text("Abrir") }
    }
}
