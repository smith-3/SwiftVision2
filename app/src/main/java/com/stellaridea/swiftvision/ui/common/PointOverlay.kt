package com.stellaridea.swiftvision.ui.common

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun PointOverlay(
    point: Pair<Int, Int>,
    label: Int,
    offset: Offset,
    imageSize: IntSize,
    imageSizeDelay: IntSize,
    scale: Float,
) {
    val medioX = imageSizeDelay.width / 2 * scale
    val medioY = imageSizeDelay.height / 2 * scale
    val translationX = offset.x - medioX + (point.first.toFloat()) * scale
    val translationY = offset.y - medioY + (point.second.toFloat()) * scale

    // Imprime los valores para depuración
    Log.d("PointOverlay", "Point: $point")
    Log.d("PointOverlay", "Offset: $offset")
    Log.d("PointOverlay", "ImageSize: $imageSize")
    Log.d("PointOverlay", "Scale: $scale")
    Log.d("PointOverlay", "TranslationX: $translationX, TranslationY: $translationY")

    Box(
        modifier = Modifier
            .size(10.dp)
            .graphicsLayer {
                this.translationX = translationX
                this.translationY = translationY
            }
    ) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = "Point",
            tint = if (label == 1) Color.Green else Color.Red,
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.Center)
        )
    }
}