package com.azrael.jardininteligente.api.models

data class PlantIdentifierResponse(
    val suggestions: List<Suggestion>?
)

data class Suggestion(
    val plant_name: String?,
    val probability: Double?,
    val plant_details: PlantDetails?
)

data class PlantDetails(
    val common_names: List<String>?,
    val taxonomy: Map<String, String>?
)
