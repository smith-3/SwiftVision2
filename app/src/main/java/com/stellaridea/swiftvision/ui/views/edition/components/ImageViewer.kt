package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.ui.common.MaskImageOverlay
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.MaskViewModel

@Composable
fun ImageViewer(
    image: Image,
    maskViewModel: MaskViewModel,
    isPredictMode: Boolean,
    isImageLoading: Boolean,
    isMaskLoading: Boolean,
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val masks by maskViewModel.masks.observeAsState(emptyList())

    // Estado para almacenar trazos/dibujos cuando isPredictMode = true
    val drawPaths = remember { mutableStateListOf<Offset>() }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val state = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 5f)

            val containerWidth = constraints.maxWidth
            val containerHeight = constraints.maxHeight

            val imageWidth = containerWidth * scale
            val imageHeight = containerHeight * scale

            val maxOffsetX = (imageWidth - containerWidth) / 2
            val maxOffsetY = (imageHeight - containerHeight) / 2

            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
            )
        }


        // Muestra la imagen de fondo
        Image(
            bitmap = image.bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state)
                .onGloballyPositioned {
                    imageSize = it.size
                }
        )

        // Detección de taps y dibujo cuando isPredictMode = true
        if (isPredictMode) {
            Canvas(modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        // Cada movimiento se registra como un punto
                        val localPos = change.position
                        drawPaths.add(localPos)
                        // Podríamos llamar a editionViewModel.detectPointsNewMask(...)
                    }
                }
            ) {
                // Dibujar los trazos guardados en drawPaths
                drawPoints(
                    points = drawPaths.toList(),
                    pointMode = PointMode.Points,
                    color = Color.Red,
                    strokeWidth = 10f
                )
            }
        } else {
            // Modo normal: toques para seleccionar máscaras
            // Uso detectTapGestures en la capa superior
            Image(
                bitmap = image.bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .onGloballyPositioned { coordinates ->
                        imageSize = coordinates.size
                    }
                    .transformable(state)
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            maskViewModel.detectMaskTap(
                                tapOffset,
                                imageSize
                            )
                        }
                    }
            )

            // Display masks
            masks.forEach { mask ->
                if (mask.active) {
                    val maskBitmap = mask.bitmap.asImageBitmap()
                    MaskImageOverlay(
                        maskBitmap = maskBitmap,
                        scale = scale,
                        offset = offset
                    )
                }
            }
        }
    }
}