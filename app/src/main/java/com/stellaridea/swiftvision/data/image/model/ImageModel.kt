package com.stellaridea.swiftvision.data.image.model

import android.graphics.Bitmap
data class ImageModel(
    val timestamp: Long,
    val bitmap: Bitmap,
    var masks: List<Mask>? = emptyList(),
    var ids: Int = 0
)