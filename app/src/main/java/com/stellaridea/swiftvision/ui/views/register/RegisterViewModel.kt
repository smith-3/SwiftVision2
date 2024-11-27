package com.stellaridea.swiftvision.ui.views.register

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.sam.AuthService
import com.stellaridea.swiftvision.data.sam.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    val isLoading = MutableLiveData(false)

    val username = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val confirmPassword = MutableLiveData("")

    val usernameError = MutableLiveData<String?>(null)
    val emailError = MutableLiveData<String?>(null)
    val passwordError = MutableLiveData<String?>(null)
    val confirmPasswordError = MutableLiveData<String?>(null)

    fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        var isValid = true

        if (username.isBlank()) {
            usernameError.value = "El nombre de usuario no puede estar vacío"
            isValid = false
        } else {
            usernameError.value = null
        }

        if (!isEmailValid(email)) {
            emailError.value = "Formato de email inválido"
            isValid = false
        } else {
            emailError.value = null
        }

        if (!isPasswordValid(password)) {
            passwordError.value = "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, números y símbolos"
            isValid = false
        } else {
            passwordError.value = null
        }

        if (password != confirmPassword) {
            confirmPasswordError.value = "Las contraseñas no coinciden"
            isValid = false
        } else {
            confirmPasswordError.value = null
        }

        if (!isValid) {
            Log.e("RegisterViewModel", "Error en la validación: Corrige los campos resaltados en rojo.")
            return
        }

        isLoading.value = true
        performRegisterRequest(username, email, password, onSuccess, onFailure)
    }

    private fun performRegisterRequest(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = authService.register(RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    Log.e("RegisterViewModel", "Error en registro: ${response.message()}")
                    onFailure()
                }
            } catch (ex: Exception) {
                Log.e("RegisterViewModel", "Excepción: ${ex.localizedMessage}", ex)
                onFailure()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        // Al menos 8 caracteres, una mayúscula, un número y un símbolo
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}\$")
        return passwordPattern.matches(password)
    }

    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword
    }

    fun canRegister(): Boolean {
        return username.value.isNullOrBlank().not()
                && email.value.isNullOrBlank().not()
                && password.value.isNullOrBlank().not()
                && confirmPassword.value.isNullOrBlank().not()
                && isEmailValid(email.value ?: "")
                && isPasswordValid(password.value ?: "")
                && doPasswordsMatch(password.value ?: "", confirmPassword.value ?: "")
    }
}