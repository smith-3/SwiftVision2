package com.stellaridea.swiftvision.data

import SegmentationDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.stellaridea.swiftvision.data.image.ImageService
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.data.project.ProjectService
import com.stellaridea.swiftvision.data.sam.AuthService
import com.stellaridea.swiftvision.data.sam.RetrofitService
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
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Mask::class.java, SegmentationDeserializer())
            .create()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.100.62:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Provisión de AuthService
    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    // Provisión de RetrofitService
    @Provides
    @Singleton
    fun provideRetrofitService(retrofit: Retrofit): RetrofitService {
        return retrofit.create(RetrofitService::class.java)
    }

    @Provides
    @Singleton
    fun provideProjectService(retrofit: Retrofit): ProjectService {
        return retrofit.create(ProjectService::class.java)
    }

    // Provisión de ImageService
    @Provides
    @Singleton
    fun provideImageService(retrofit: Retrofit): ImageService {
        return retrofit.create(ImageService::class.java)
    }
}
