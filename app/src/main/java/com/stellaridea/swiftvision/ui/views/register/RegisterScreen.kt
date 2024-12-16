package com.stellaridea.swiftvision.ui.views.register


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.R
import com.stellaridea.swiftvision.ui.common.AccessibleCircularProgressIndicator

@Composable
fun RegisterScreen(navController: NavHostController) {
    val viewModel: RegisterViewModel = hiltViewModel()
    val username by viewModel.username.observeAsState("")
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val confirmPassword by viewModel.confirmPassword.observeAsState("")

    val usernameError by viewModel.usernameError.observeAsState()
    val emailError by viewModel.emailError.observeAsState()
    val passwordError by viewModel.passwordError.observeAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.observeAsState()

    val isLoading by viewModel.isLoading.observeAsState(false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo de la aplicación
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo de la app",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,

                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de nombre de usuario
            CustomInputField(
                value = username,
                onValueChange = { viewModel.username.value = it },
                label = "Nombre de usuario",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                isError = usernameError != null,
                errorMessage = usernameError,
                isValid = usernameError == null && username.isNotBlank()
            )

            // Campo de email
            CustomInputField(
                value = email,
                onValueChange = { viewModel.email.value = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = emailError != null,
                errorMessage = emailError,
                isValid = emailError == null && viewModel.isEmailValid(email)
            )

            // Campo de contraseña
            CustomPasswordInputField(
                value = password,
                onValueChange = { viewModel.password.value = it },
                label = "Contraseña",
                imeAction = ImeAction.Next,
                isError = passwordError != null,
                errorMessage = passwordError,
                isValid = passwordError == null && viewModel.isPasswordValid(password)
            )

            // Mostrar el indicador de seguridad solo si la contraseña no está vacía
            if (password.isNotEmpty()) {
                PasswordStrengthIndicator(password = password)
            }

            // Campo de confirmación de contraseña
            CustomPasswordInputField(
                value = confirmPassword,
                onValueChange = { viewModel.confirmPassword.value = it },
                label = "Confirmar Contraseña",
                imeAction = ImeAction.Done,
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                isValid = confirmPasswordError == null && viewModel.doPasswordsMatch(password, confirmPassword)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de registro
            Button(
                onClick = {
                    viewModel.register(
                        username = username,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        onSuccess = {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        },
                        onFailure = {
                            Log.d("RegisterScreen", "Error en el registro")
                        }
                    )
                },
                enabled = !isLoading && viewModel.canRegister(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    AccessibleCircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        contentDescription = "Cargando, por favor espere"
                    )
                } else {
                    Text(text = "Registrarse", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para volver al inicio de sesión
            TextButton(
                onClick = {
                    navController.navigate("login")
                }
            ) {
                Text(text = "Volver al inicio de sesión", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isSingleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    isValid: Boolean = false
) {
    val naturalGreen = Color(0xFF4CAF50) // Verde más natural
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isValid -> naturalGreen
        else -> MaterialTheme.colorScheme.outline
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = isSingleLine,
        isError = isError,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        trailingIcon = {
            when {
                isValid -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Válido",
                        tint = naturalGreen
                    )
                }
                isError -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Inválido",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            errorBorderColor = MaterialTheme.colorScheme.error
        ),
        keyboardActions = keyboardActions
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    isValid: Boolean = false
) {
    val naturalGreen = Color(0xFF4CAF50) // Verde más natural
    var passwordVisibility by remember { mutableStateOf(false) }

    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isValid -> naturalGreen
        else -> MaterialTheme.colorScheme.outline
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        isError = isError,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            when {
                isValid -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Válido",
                        tint = naturalGreen
                    )
                }
                isError -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Inválido",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        val icon = if (passwordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        Icon(
                            imageVector = icon,
                            contentDescription = if (passwordVisibility) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            errorBorderColor = MaterialTheme.colorScheme.error
        ),
        keyboardActions = keyboardActions
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

@Composable
fun PasswordStrengthIndicator(password: String) {
    val strengthColor = when {
        password.length >= 12 && password.any { it.isUpperCase() }
                && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> Color(0xFF4CAF50) // Verde natural
        password.length >= 8 -> Color(0xFFFFC107) // Amarillo
        else -> Color(0xFFF44336) // Rojo
    }
    val strengthText = when {
        password.length >= 12 && password.any { it.isUpperCase() }
                && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> "Seguridad Alta"
        password.length >= 8 -> "Seguridad Media"
        else -> "Seguridad Baja"
    }

    LinearProgressIndicator(
        progress = when {
            password.length >= 12 && password.any { it.isUpperCase() }
                    && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 1f
            password.length >= 8 -> 0.6f
            else -> 0.3f
        },
        color = strengthColor,
        trackColor = strengthColor.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Text(
        text = strengthText,
        color = strengthColor,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 16.dp)
    )
}
