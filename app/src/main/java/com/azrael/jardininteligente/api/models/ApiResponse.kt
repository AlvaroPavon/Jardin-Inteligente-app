package com.azrael.jardininteligente.api.models

data class ApiResponse(
    val message: String,
    val error: String? = null
)
