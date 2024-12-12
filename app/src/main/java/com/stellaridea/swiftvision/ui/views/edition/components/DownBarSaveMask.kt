package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cancel

@Composable
fun DownBarSaveMask(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Botón Cancelar
        IconButton(
            onClick = onCancelClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = "Cancelar",
                tint = Color.Red,
                modifier = Modifier.size(40.dp)
            )
        }

        // Botón Guardar
        IconButton(
            onClick = onSaveClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Guardar",
                tint = Color.Green,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
