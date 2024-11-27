package com.stellaridea.swiftvision.data.sam

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class UserResponse(val id: Int, val username: String, val email: String?)

interface AuthService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<UserResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>
}
