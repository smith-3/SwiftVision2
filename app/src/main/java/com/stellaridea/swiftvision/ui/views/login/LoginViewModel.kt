package com.stellaridea.swiftvision.ui.views.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.AuthService
import com.stellaridea.swiftvision.data.LoginRequest
import com.stellaridea.swiftvision.data.user.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val userPreferences: UserPreferences // Inyecta UserPreferences
) : ViewModel() {

    var loginStatus = MutableLiveData<Boolean>()
    var isLoading = MutableLiveData(false)
    var email = MutableLiveData("")
    var password = MutableLiveData("")

    var emailError = MutableLiveData<String?>(null)
    var passwordError = MutableLiveData<String?>(null)

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        // Validar entradas
        var isValid = true

        if (!isEmailValid(email)) {
            emailError.value = "Formato de email inválido"
            isValid = false
        } else {
            emailError.value = null
        }

        if (!isPasswordValid(password)) {
            passwordError.value = "La contraseña debe tener al menos 8 caracteres"
            isValid = false
        } else {
            passwordError.value = null
        }

        if (!isValid) return

        isLoading.value = true
        viewModelScope.launch {
            try {
                val response = authService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val user = response.body()  // Obtén el objeto de usuario del backend
                    if (user != null) {
                        // Guarda todos los datos del usuario en SharedPreferences
                        userPreferences.saveUserId(user.id)
                        userPreferences.saveUserName(user.username)
                        userPreferences.saveUserEmail(user.email ?: "")

                        loginStatus.value = true
                        onSuccess()
                    } else {
                        onFailure()
                    }
                } else {
                    Log.e("LoginViewModel", "Error en login: ${response.message()}")
                    loginStatus.value = false
                    onFailure()
                }
            } catch (ex: Exception) {
                Log.e("LoginViewModel", "Excepción: ${ex.localizedMessage}", ex)
                loginStatus.value = false
                onFailure()
            } finally {
                isLoading.value = false
            }
        }
    }

    // Validación de correo electrónico
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validación de contraseña
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    fun isUserLoggedIn(): Boolean {
        // Verifica si el ID de usuario ya está almacenado en preferencias
        return userPreferences.getUserId() != null
    }
}
