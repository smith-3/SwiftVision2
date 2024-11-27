package com.stellaridea.swiftvision.data.project

import com.stellaridea.swiftvision.data.project.model.ProjectRequest
import com.stellaridea.swiftvision.data.project.model.ProjectResponse
import com.stellaridea.swiftvision.data.project.model.ProjectUpdateRequest
import retrofit2.Response
import retrofit2.http.*

interface ProjectService {
    @POST("projects/")
    suspend fun createProject(@Body request: ProjectRequest): Response<ProjectResponse>

    @GET("users/{user_id}/projects/")
    suspend fun getProjects(@Path("user_id") userId: Int): Response<List<ProjectResponse>>

    @PUT("projects/{project_id}")
    suspend fun updateProject(@Path("project_id") projectId: Int, @Body request: ProjectUpdateRequest): Response<ProjectResponse>

    @DELETE("projects/{project_id}")
    suspend fun deleteProject(@Path("project_id") projectId: Int): Response<Unit>
}