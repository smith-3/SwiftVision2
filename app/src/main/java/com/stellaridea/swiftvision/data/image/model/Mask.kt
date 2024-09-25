package com.stellaridea.swiftvision.data.image.model

import android.graphics.Bitmap
import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "masks")
data class Mask(
    val id: Int,
    val bitmap: Bitmap,
    val size: IntArray,
    var active: Boolean = false
) : Serializable
