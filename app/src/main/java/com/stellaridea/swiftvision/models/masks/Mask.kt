package com.stellaridea.swiftvision.models.masks

import android.graphics.Bitmap
import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "masks")
data class Mask(
    val id: Long,
    val image_id: Int,
    val bitmap: Bitmap,
    val size: IntArray,
    var active: Boolean = false,
    val created_at: Long,
    ) : Serializable
