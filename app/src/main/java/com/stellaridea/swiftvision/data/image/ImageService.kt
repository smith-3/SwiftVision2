package com.stellaridea.swiftvision.data.image

import com.stellaridea.swiftvision.data.image.model.ImageModel
import com.stellaridea.swiftvision.data.sam.model.MasksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ImageService {
    @GET("projects/{project_id}/images/")
    suspend fun getImages(@Path("project_id") projectId: Int): Response<List<ImageModel>>
    @GET("images/{image_id}/masks/")
    suspend fun getMasks(@Path("image_id") imageId: Int): Response<MasksResponse>
}
