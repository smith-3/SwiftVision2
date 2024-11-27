package com.stellaridea.swiftvision.data.user

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) {
        preferences.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int? {
        val userId = preferences.getInt("user_id", -1)
        return if (userId != -1) userId else null
    }

    fun clearUserId() {
        preferences.edit().remove("user_id").apply()
    }
}
