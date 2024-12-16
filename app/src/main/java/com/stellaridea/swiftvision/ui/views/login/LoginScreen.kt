package com.stellaridea.swiftvision.ui.views.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.R
import com.stellaridea.swiftvision.ui.common.AccessibleCircularProgressIndicator
import com.stellaridea.swiftvision.ui.common.InputField
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.navigation.GraphRoot

@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: LoginViewModel = hiltViewModel()

    HandleUserLoggedIn(viewModel, navController)

    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val email by viewModel.email.observeAsState(initial = "")
    val password by viewModel.password.observeAsState(initial = "")

    val emailError by viewModel.emailError.observeAsState()
    val passwordError by viewModel.passwordError.observeAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo de la app",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,

                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de email
            EmailInput(
                emailState = viewModel.email,
                emailError = emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            PasswordInput(
                passwordState = viewModel.password,
                passwordError = passwordError
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.login(
                        email = email,
                        password = password,
                        onSuccess = {
                            navController.navigate(GraphRoot.HOME) {
                                popUpTo(GraphRoot.LOGIN) { inclusive = true }
                            }
                        },
                        onFailure = {
                            Log.d("LoginScreen", "Error en el inicio de sesión")
                        }
                    )
                },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && emailError == null && passwordError == null,
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
                    Text(text = "Iniciar Sesión", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para registrarse
            TextButton(
                onClick = {
                    navController.navigate(Graph.REGISTER)
                }
            ) {
                Text(text = "Registrarse", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun HandleUserLoggedIn(viewModel: LoginViewModel, navController: NavHostController) {
    if (viewModel.isUserLoggedIn()) {
        navController.navigate(GraphRoot.HOME) {
            popUpTo(GraphRoot.LOGIN) { inclusive = true }
        }
    }
}

@Composable
fun EmailInput(
    emailState: MutableLiveData<String>,
    labelId: String = "Email",
    emailError: String? = null
) {
    emailState.value?.let {
        InputField(
            value = it,
            onValueChange = { emailState.value = it },
            labelId = labelId,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { /* Manejar el siguiente campo */ }),
            isError = emailError != null,
            errorMessage = emailError
        )
    }
}

@Composable
fun PasswordInput(
    passwordState: MutableLiveData<String>,
    labelId: String = "Contraseña",
    passwordError: String? = null
) {
    passwordState.value?.let {
        InputField(
            value = it,
            onValueChange = { passwordState.value = it },
            labelId = labelId,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { /* Manejar el login aquí */ }),
            isError = passwordError != null,
            errorMessage = passwordError
        )
    }
}
