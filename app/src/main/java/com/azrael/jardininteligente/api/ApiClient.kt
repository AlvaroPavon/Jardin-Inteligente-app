package com.azrael.jardininteligente.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // URL base de la API alojada en XAMPP. Nota: en el emulador localhost se accede mediante 10.0.2.2.
    private const val BASE_URL = "http://192.168.1.168/index.php/"

    // Interceptor para añadir el header "X-API-KEY" a cada petición
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithApiKey = originalRequest.newBuilder()
            .header("X-API-KEY", "ApM13021998")  // Debe coincidir con lo definido en tu config.php
            .build()
        chain.proceed(requestWithApiKey)
    }

    // Configurar el cliente OkHttp con el interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .build()

    // Configurar Retrofit con la URL base, cliente OkHttp y el convertidor de Gson para JSON.
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Instancia de la interfaz ApiService para ser utilizada en la app.
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
