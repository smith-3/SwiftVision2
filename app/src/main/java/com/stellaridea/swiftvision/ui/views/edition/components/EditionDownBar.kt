package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun EditionDownBar(
    onChangeBackground: () -> Unit,
    onGenerateAI: () -> Unit,
    onDeleteObject: () -> Unit,
    isEnabled: Boolean // Nuevo parámetro para habilitar/deshabilitar
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón izquierdo: Cambiar Fondo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Photo,
                    contentDescription = "Cambiar Fondo",
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = isEnabled) { onChangeBackground() }
                )
                Text(
                    text = "Cambiar fondo",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            // Botón central: Generar con IA (resaltado y sobresaliente con borde blanco)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.offset(y = (-20).dp) // Hace que sobresalga
            ) {
                // Círculo blanco (borde)
                Box(
                    modifier = Modifier
                        .size(75.dp) // Tamaño ligeramente mayor al círculo púrpura
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                )

                // Círculo púrpura (contenido principal)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp) // Tamaño del círculo púrpura
                        .background(
                            if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                        )
                        .clickable(enabled = isEnabled) { onGenerateAI() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Generar con IA",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Botón derecho: Eliminar Objeto
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar Objeto",
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = isEnabled) { onDeleteObject() }
                )
                Text(
                    text = "Eliminar objeto",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}
