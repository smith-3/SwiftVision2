package com.stellaridea.swiftvision.data.sam

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.stellaridea.swiftvision.data.image.model.Mask
import com.stellaridea.swiftvision.data.image.repository.SegmentationDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.MINUTES)
            .writeTimeout(15, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.HOURS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitService(okHttpClient: OkHttpClient): RetrofitService {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Mask::class.java, SegmentationDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl("http://192.168.7.39:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Usa Gson personalizado
            .build()
            .create(RetrofitService::class.java)
    }
}
