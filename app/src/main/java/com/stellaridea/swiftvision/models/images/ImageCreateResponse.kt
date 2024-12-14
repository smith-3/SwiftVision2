package com.stellaridea.swiftvision.models.images

import android.graphics.Bitmap
import com.stellaridea.swiftvision.models.masks.Mask

data class ImageCreateResponse(
    val id: Int,
    val bitmap: Bitmap,
    val masks: List<Mask>
)
