package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RemoveDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textValue by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true) // Permite cerrar al tocar fuera
    ) {
        // Contenedor principal del diálogo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(16.dp)) // Fondo blanco con bordes redondeados
                .padding( 24.dp) // Reducir padding superior
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                // Botón central: Generar con IA (resaltado y sobresaliente con borde blanco)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(y = (-50).dp) // Hace que sobresalga

                ) {
                    // Círculo blanco (borde)
                    Box(
                        modifier = Modifier
                            .size(85.dp) // Tamaño ligeramente mayor al círculo púrpura
                            .background(MaterialTheme.colorScheme.onPrimary, CircleShape)

                    )

                    // Círculo púrpura (contenido principal)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp) // Tamaño del círculo púrpura
                            .background(MaterialTheme.colorScheme.primary, CircleShape) // Color púrpura
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Generar con IA",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

// Mensaje para el usuario
                Text(
                    text = "¡Remueve el objeto!",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold, // Aplica negrita
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = Color.White,
                    modifier = Modifier
                        .offset(y = (-30).dp) // Ajusta la posición hacia arriba
                        .padding(10.dp) // Espaciado interno
                        .align(Alignment.CenterHorizontally) // Alinea al centro horizontalmente
                )

                Text(
                    text = "Elimina el objeto que no deseas en la imagen con AI",
                    style = TextStyle(
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = Color.White,
                    modifier = Modifier
                        .offset(y = (-20).dp) // Ajusta la posición hacia arriba
                        .align(Alignment.CenterHorizontally) // Alinea al centro horizontalmente
                )


                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón cancelar
                    Button(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    // Botón generar
                    Button(onClick = { onConfirm(textValue) }) {
                        Text("Remover")
                    }
                }
            }
        }
    }
}
