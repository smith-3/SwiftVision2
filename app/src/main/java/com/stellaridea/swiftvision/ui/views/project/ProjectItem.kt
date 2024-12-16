package com.stellaridea.swiftvision.ui.views.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stellaridea.swiftvision.models.projects.Project

@Composable
fun ProjectItem(
    project: Project,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onNavigate: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(project.name) }
    var isLoading by remember { mutableStateOf(false) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showEditDialog = false },
            title = { Text("Editar Proyecto") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre del proyecto") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            isLoading = true
                            onEdit(newName)
                            isLoading = false
                            showEditDialog = false
                        }
                    },
                    enabled = !isLoading && newName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Actualizar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isLoading) showEditDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Proyecto") },
            text = { Text("¿Estás seguro de que deseas eliminar este proyecto?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen adaptativa
            val firstImage = project.images.firstOrNull()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                if (firstImage?.bitmap != null) {
                    Image(
                        bitmap = firstImage.bitmap!!.asImageBitmap(),
                        contentDescription = "Imagen del proyecto",
                        contentScale = ContentScale.Crop, // Ajusta sin deformar
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            // Título del proyecto
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Iconos con texto descriptivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWithText(
                    icon = Icons.Default.Edit,
                    text = "Editar",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { showEditDialog = true }
                )
                IconWithText(
                    icon = Icons.Default.Delete,
                    text = "Eliminar",
                    color = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteDialog = true }
                )
                IconWithText(
                    icon = Icons.Default.ArrowForward,
                    text = "Abrir",
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { onNavigate() }
                )
            }
        }
    }
}

@Composable
fun IconWithText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
