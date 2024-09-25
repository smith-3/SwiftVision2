package com.stellaridea.swiftvision.data.sam

import com.stellaridea.swiftvision.data.sam.model.MasksResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitService {

    @Multipart
    @POST("/masks")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): MasksResponse

    @Multipart
    @POST("/colorize")
    suspend fun colorizeImage(
        @Part("masks_json") masksJson: RequestBody,
        @Part("rgb") rgb: RequestBody
    ): ResponseBody

    @Multipart
    @POST("/masks_points")
    suspend fun maskPoints(
        @Part("points") points: RequestBody,
        @Part("labels") labels: RequestBody
    ): MasksResponse
}
