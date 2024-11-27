package com.stellaridea.swiftvision.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    labelId: String,
    isSingleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(

    ),
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val passwordVisibility = remember { mutableStateOf(false) }

    // Configurar visualTransformation dependiendo de si es un campo de contraseña y la visibilidad
    val visualTransformation = if (keyboardType == KeyboardType.Password && !passwordVisibility.value) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = labelId) },
        singleLine = isSingleLine,
        isError = isError,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        visualTransformation = visualTransformation,
        trailingIcon = {
            // Mostrar el botón de visibilidad solo si es un campo de contraseña
            if (keyboardType == KeyboardType.Password) {
                IconButton(onClick = { passwordVisibility.value = !passwordVisibility.value }) {
                    val icon = if (passwordVisibility.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    Icon(
                        imageVector = icon,
                        contentDescription = if (passwordVisibility.value) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        },
        keyboardActions = keyboardActions,
        colors = colors
    )
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
