package com.specimenid.domain

import android.graphics.Rect

data class ScannedBlock(
    val text: String,
    val boundingBox: Rect?
)
