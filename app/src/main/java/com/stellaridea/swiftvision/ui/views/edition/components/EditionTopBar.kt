package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EditionTopBar(
    projectName: String,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botones de navegación (izquierda)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onNext, enabled = canGoNext) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    tint = if (canGoNext) Color.White else Color.Gray
                )
            }
            IconButton(onClick = onPrevious, enabled = canGoPrev) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = if (canGoPrev) Color.White else Color.Gray
                )
            }
        }

        // Nombre del proyecto (centro)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = projectName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))

        // Botón de guardar (derecha)
        IconButton(onClick = onSave) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "Save",
                tint = Color.White
            )
        }
    }
}
