package com.stellaridea.swiftvision.data.image.model

import android.graphics.Bitmap
import com.stellaridea.swiftvision.models.masks.Mask

data class ImageModel(
    val id: Int,
    val project_id: Int,
    val bitmap: Bitmap,
    val created_at: Long,
    var masks: List<Mask> = emptyList(),
)