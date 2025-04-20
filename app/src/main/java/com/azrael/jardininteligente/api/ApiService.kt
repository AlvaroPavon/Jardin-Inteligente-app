package com.azrael.jardininteligente.api

import com.azrael.jardininteligente.api.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    // Endpoints existentes...
    @POST("register")
    fun registerUser(@Body request: RegisterRequest): Call<ApiResponse>

    @POST("login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @GET("plants")
    fun getPlants(@Header("Authorization") token: String): Call<List<Plant>>

    @POST("plant-identifier")
    fun identifyPlant(
        @Header("Authorization") token: String,
        @Body request: PlantIdentifierRequest
    ): Call<PlantIdentifierResponse>

    // Nuevos endpoints para el foro:
    @GET("foro/temas")
    fun getForumTopics(@Header("Authorization") token: String): Call<List<ForumTopic>>

    @POST("foro/temas")
    fun createForumTopic(
        @Header("Authorization") token: String,
        @Body request: NewForumTopicRequest
    ): Call<ApiResponse>

    @GET("me")
    fun getUserInfo(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    data class UserResponse(val correo: String)

}
