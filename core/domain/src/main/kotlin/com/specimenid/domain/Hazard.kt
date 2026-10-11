package com.specimenid.domain

import android.graphics.Rect

data class Hazard(
    val ingredient: String,
    val matchedAllergen: String,
    val severity: Int,
    val boundingBox: Rect? = null
)
