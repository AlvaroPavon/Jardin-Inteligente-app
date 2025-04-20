package com.azrael.jardininteligente.api.models

data class ForumTopic(
    val id: Int,
    val titulo: String,
    val contenido: String,
    val fecha_creacion: String,
    val autor: String
)