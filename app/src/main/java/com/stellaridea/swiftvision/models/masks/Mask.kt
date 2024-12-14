package com.stellaridea.swiftvision.models.masks

import android.graphics.Bitmap
import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "masks")
data class Mask(
    val id: Long,
    val bitmap: Bitmap,
    val size: IntArray,
    var active: Boolean = false,
    ) : Serializable
