package com.stellaridea.swiftvision.data.image.model

import android.graphics.Bitmap
import com.stellaridea.swiftvision.data.mask.model.Mask

data class ImageModel(
    val id: Int,
    val project_id: Int,
    val bitmap: Bitmap,
    val created_at: Long,
    var masks: List<Mask> = emptyList(),
)