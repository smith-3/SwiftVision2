package com.stellaridea.swiftvision.models.projects

data class ProjectResponse(
    val id: Int,
    val name: String,
    val user_id: Int,
    val created_at: String
)