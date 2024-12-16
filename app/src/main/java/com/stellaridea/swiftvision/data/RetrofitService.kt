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
import retrofit2.http.*

interface RetrofitService {
    @Multipart
    @POST("/projects")
    suspend fun createProject(
        @Part("user_id") userId: RequestBody,
        @Part("project_name") projectName: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ProjectCreateResponse>

    @GET("projects/{user_id}/")
    suspend fun getProjects(@Path("user_id") userId: Int): Response<List<Project>>

    @PUT("projects/{project_id}")
    suspend fun updateProject(
        @Path("project_id") projectId: Int,
        @Body projectUpdate: ProjectUpdate
    ): Response<ProjectUpdateResponse>

    @DELETE("projects/{project_id}")
    suspend fun deleteProject(@Path("project_id") projectId: Int): Response<Unit>

    @Multipart
    @POST("/masks_points")
    suspend fun maskPoints(
        @Part("points") points: RequestBody,
        @Part("labels") labels: RequestBody
    ): Mask

    @GET("/images/{image_id}/masks/")
    suspend fun getMasksByImageId(@Path("image_id") imageId: Int): Response<List<Mask>>

    @GET("/images/{image_id}/download")
    suspend fun downloadImage(@Path("image_id") imageId: Int): Response<ResponseBody>

    @GET("/images/{project_id}/")
    suspend fun getImagesByProjectId(@Path("project_id") projectId: Int): Response<List<ImageResponse>>

    @DELETE("/images/{image_id}")
    suspend fun deleteImage(@Path("image_id") imageId: Int): Response<Unit>

    // Nuevo endpoint: Procesar para eliminar máscara de la imagen
    @GET("/process_inpainting_with_mask")
    suspend fun processInpainting(
        @Query("project_id") projectId: Int,
        @Query("image_id") imageId: Int,
        @Query("mask_id") maskId: Int
    ): Response<ResponseBody> // Ajusta el tipo de respuesta según el backend

    // Nuevo endpoint: Generar imagen con un promp
    @GET("/generate_image")
    suspend fun generateImage(
        @Query("project_id") projectId: Int,
        @Query("image_id") imageId: Int,
        @Query("mask_id") maskId: Int,
        @Query("promt") prompt: String
    ): Response<ResponseBody> // Ajusta el tipo de respuesta según el backend

    // Nuevo endpoint: Generar fondo de imagen con un promp
    @GET("/generate_image_backgraund")
    suspend fun generateImageBackground(
        @Query("project_id") projectId: Int,
        @Query("image_id") imageId: Int,
        @Query("mask_id") maskId: Int,
        @Query("promt") prompt: String
    ): Response<ResponseBody> // Ajusta el tipo de respuesta según el backend

    @Multipart
    @POST("/upload_mask")
    suspend fun uploadMask(
        @Part("image_id") imageId: RequestBody,
        @Part("project_id") projectId: RequestBody,
        @Part("counts") countsJson: RequestBody,   // El JSON comprimido
        @Part("size") size: RequestBody           // Ancho y alto
    ): Response<Unit>

}
