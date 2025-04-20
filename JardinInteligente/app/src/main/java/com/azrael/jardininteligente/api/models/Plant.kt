package com.azrael.jardininteligente.api.models

data class Plant(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val riego: String,
    val salud: String,
    val fecha_registro: String
)
