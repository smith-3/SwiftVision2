package com.stellaridea.swiftvision.models.images

import android.graphics.Bitmap
import com.stellaridea.swiftvision.models.masks.Mask

data class Image(
    var id: Int,
    var bitmap: Bitmap,
    var active: Boolean = false,
    var masks: List<Mask> = emptyList()
)
