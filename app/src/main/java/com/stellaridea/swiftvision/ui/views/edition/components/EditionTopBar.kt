package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.MaskViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.PredictViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.ProjectViewModel

@Composable
fun EditionTopBar(
    projectViewModel: ProjectViewModel,
    maskViewModel: MaskViewModel,
    predictViewModel: PredictViewModel,
    onGeneratePrompt: () -> Unit,
    onSave: () -> Unit,
    onDownload: () -> Unit,
    onDeleteMask: () -> Unit // Nuevo callback para eliminar máscaras
) {
    val canGoPrev = projectViewModel.canGoPrevious()
    val canGoNext = projectViewModel.canGoNext()
    val modoPredict by predictViewModel.modoPredict.observeAsState(false)
    val masks by maskViewModel.masks.observeAsState(emptyList())
    val hasActiveMask = masks.any { it.active }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Botones de Navegación
        NavigationButtons(
            canGoPrev = canGoPrev,
            canGoNext = canGoNext,
            onPrevious = { projectViewModel.selectPreviousImage() },
            onNext = { projectViewModel.selectNextImage() }
        )

        // Botón de Lápiz para activar modo predictivo
        ActionButton(
            icon = Icons.Filled.Create,
            description = "Toggle Predict Mode",
            enabled = true,
            onClick = { predictViewModel.toggleModoPredict() }
        )

        // Botón para eliminar máscara (visible solo si hay una máscara activa)
        if (hasActiveMask && !modoPredict) {
            ActionButton(
                icon = Icons.Filled.Delete,
                description = "Delete Mask",
                enabled = true,
                onClick = onDeleteMask
            )
        }

        // Botón para generar prompt (visible solo si hay una máscara activa)
        if (hasActiveMask) {
            ActionButton(
                icon = Icons.Filled.AutoAwesome,
                description = "Generate Prompt",
                enabled = true,
                onClick = onGeneratePrompt
            )
        }

        // Botón para guardar (visible solo si no está en modo predictivo)
        if (!modoPredict) {
            ActionButton(
                icon = Icons.Filled.Save,
                description = "Save",
                enabled = true,
                onClick = onSave
            )
        }

        // Botón para descargar (visible solo si no está en modo predictivo)
        if (!modoPredict) {
            ActionButton(
                icon = Icons.Filled.Download,
                description = "Download",
                enabled = true,
                onClick = onDownload
            )
        }
    }
}

// Componente: Botones de Navegación
@Composable
fun NavigationButtons(
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onPrevious, enabled = canGoPrev) {
            Icon(
                imageVector = Icons.Filled.KeyboardDoubleArrowLeft,
                contentDescription = "Previous",
                tint = if (canGoPrev) Color.White else Color.Gray
            )
        }
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                imageVector = Icons.Filled.KeyboardDoubleArrowRight,
                contentDescription = "Next",
                tint = if (canGoNext) Color.White else Color.Gray
            )
        }
    }
}

// Componente: Botón Genérico de Acción
@Composable
fun ActionButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (enabled) Color.White else Color.Gray
        )
    }
}

// Componente: Botón de Lápiz con Herramientas
@Composable
fun PencilButton(
    isActive: Boolean,
    showTools: Boolean,
    selectedTool: Tool?,
    onPencilClick: () -> Unit,
    onToolSelected: (Tool) -> Unit
) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        IconButton(onClick = onPencilClick) {
            Icon(
                imageVector = Icons.Filled.Create,
                contentDescription = "Pencil",
                tint = if (isActive) Color.Cyan else Color.White
            )
        }
        if (showTools) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolButton(
                    tool = Tool.Star,
                    isSelected = selectedTool == Tool.Star,
                    onToolSelected = onToolSelected
                )
                ToolButton(
                    tool = Tool.Square,
                    isSelected = selectedTool == Tool.Square,
                    onToolSelected = onToolSelected
                )
                ToolButton(
                    tool = Tool.Finger,
                    isSelected = selectedTool == Tool.Finger,
                    onToolSelected = onToolSelected
                )
            }
        }
    }
}

// Componente: Botón de Herramienta Específica
@Composable
fun ToolButton(
    tool: Tool,
    isSelected: Boolean,
    onToolSelected: (Tool) -> Unit
) {
    IconButton(
        onClick = { onToolSelected(tool) },
        modifier = Modifier.background(
            color = if (isSelected) Color.Yellow else Color.Transparent,
            shape = CircleShape
        )
    ) {
        Icon(
            imageVector = tool.icon,
            contentDescription = tool.description,
            tint = if (isSelected) Color.Black else Color.White
        )
    }
}


// Enum para herramientas del lápiz
enum class Tool(val icon: ImageVector, val description: String) {
    Star(Icons.Filled.AutoAwesome, "Star Tool"),
    Square(Icons.Filled.CheckBoxOutlineBlank, "Square Tool"),
    Finger(Icons.Filled.Gesture, "Manual Selection Tool")
}
