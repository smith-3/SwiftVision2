package com.stellaridea.swiftvision.data.user

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Guardar el ID del usuario
    fun saveUserId(userId: Int) {
        preferences.edit().putInt("user_id", userId).apply()
    }

    // Obtener el ID del usuario
    fun getUserId(): Int? {
        val userId = preferences.getInt("user_id", -1)
        return if (userId != -1) userId else null
    }

    // Guardar el nombre del usuario
    fun saveUserName(name: String) {
        preferences.edit().putString("user_name", name).apply()
    }

    // Obtener el nombre del usuario
    fun getUserName(): String? {
        return preferences.getString("user_name", null)
    }

    // Guardar el correo del usuario
    fun saveUserEmail(email: String) {
        preferences.edit().putString("user_email", email).apply()
    }

    // Obtener el correo del usuario
    fun getUserEmail(): String? {
        return preferences.getString("user_email", null)
    }

    // Borrar todos los datos del usuario
    fun clearUserData() {
        preferences.edit().clear().apply()
    }
}
