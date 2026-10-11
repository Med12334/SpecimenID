package com.specimenid.domain

data class Hazard(
    val ingredient: String,
    val matchedAllergen: String,
    val severity: Int
)
