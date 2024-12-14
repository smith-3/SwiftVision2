package com.stellaridea.swiftvision.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun MaskImageOverlay(
    maskBitmap: ImageBitmap,
    scale: Float,
    offset: Offset
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            bitmap = maskBitmap,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}