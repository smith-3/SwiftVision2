package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Down Bar
@Composable
fun EditionDownBar(
    onDeleteMask: () -> Unit,
    onGeneratePrompt: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDeleteMask,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Eliminar Máscara",
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Eliminar Máscara")
        }

        Button(
            onClick = onGeneratePrompt,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "Generar Prompt",
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Generar Prompt")
        }
    }
}