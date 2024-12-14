package com.stellaridea.swiftvision.data

import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.models.images.ImageResponse
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.models.projects.Project
import com.stellaridea.swiftvision.models.projects.ProjectCreateResponse
import com.stellaridea.swiftvision.models.projects.ProjectUpdate
import com.stellaridea.swiftvision.models.projects.ProjectUpdateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface RetrofitService {

    @Multipart
    @POST("/projects")
    suspend fun createProject(
        @Part("user_id") userId: RequestBody,
        @Part("project_name") projectName: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ProjectCreateResponse> // Cambiado a Response<>

    @GET("projects/{user_id}/")
    suspend fun getProjects(@Path("user_id") userId: Int): Response<List<Project>>

    @PUT("projects/{project_id}")
    suspend fun updateProject(
        @Path("project_id") projectId: Int,
        @Body projectUpdate: ProjectUpdate
    ): Response<ProjectUpdateResponse>

    @DELETE("projects/{project_id}")
    suspend fun deleteProject(@Path("project_id") projectId: Int): Response<Unit>

    // generamos una nueva mascara
    @Multipart
    @POST("/masks_points")
    suspend fun maskPoints(
        @Part("points") points: RequestBody,
        @Part("labels") labels: RequestBody
    ): Mask

    // Recuperamos las mascaras
    @GET("/images/{image_id}/masks/")
    suspend fun getMasksByImageId(@Path("image_id") imageId: Int): Response<List<Mask>>
    //Recuperamos la imagen
    @GET("/images/{image_id}/download")
    suspend fun downloadImage(@Path("image_id") imageId: Int): Response<ResponseBody>

    // Recuperar Imagenes IDs
    @GET("/images/{project_id}/")
    suspend fun getImagesByProjectId(@Path("project_id") projectId: Int): Response<List<ImageResponse>>
}