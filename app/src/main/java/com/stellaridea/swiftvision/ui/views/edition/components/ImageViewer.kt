package com.stellaridea.swiftvision.ui.views.edition.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.Log
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.ui.common.MaskImageOverlay
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.MaskViewModel
import kotlin.math.pow
import kotlin.math.sqrt


@Composable
fun ImageViewer(
    image: Image,
    maskViewModel: MaskViewModel,
    isPredictMode: Boolean,
    isLoading: Boolean,
    isMaskLoading: Boolean,
    onCancelPredict: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val masks by maskViewModel.masks.observeAsState(emptyList())

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
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

        if (image.bitmap != null) {
            // Obtener dimensiones originales de la imagen
            val originalWidth = image.bitmap!!.width
            val originalHeight = image.bitmap!!.height

            Image(
                bitmap = image.bitmap!!.asImageBitmap(),
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
                    .wrapContentSize()
                    .transformable(state)
            )
            if (isMaskLoading) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black.copy(alpha = 0.7f))
                        .zIndex(1f), // Mayor prioridad en el eje Z
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF34D399))
                }
            }
            if (isPredictMode) {
                val lines = remember {
                    mutableStateListOf<Line>()
                }

                // Detectar doble tap para restaurar escala y offset en modo predict
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            )
                        }
                )

                Canvas(
                    modifier = Modifier
                        .size(
                            with(LocalDensity.current) { imageSize.width.toDp() },
                            with(LocalDensity.current) { imageSize.height.toDp() }
                        )
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .align(Alignment.Center)
                        .pointerInput(true) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val canvasBounds = Rect(
                                    0f,
                                    0f,
                                    imageSize.width.toFloat(),
                                    imageSize.height.toFloat()
                                )
                                if (!canvasBounds.contains(change.position)) {
                                    return@detectDragGestures
                                }

                                if (lines.isNotEmpty()) {
                                    val lastPoint = lines.last()
                                    val distance = sqrt(
                                        (change.position.x - lastPoint.end.x).pow(2) +
                                                (change.position.y - lastPoint.end.y).pow(2)
                                    )
                                    if (distance > 150f) {
                                        lines.clear()
                                    }
                                }

                                val line = Line(
                                    start = change.position - dragAmount,
                                    end = change.position
                                )
                                lines.add(line)
                            }
                        }
                ) {
                    drawRect(
                        color =  Color(0xFF34D399),
                        size = size,
                        style = Stroke(width = 5f)
                    )

                    lines.forEach { line ->
                        drawLine(
                            color = line.color,
                            start = line.start,
                            end = line.end,
                            strokeWidth = line.strokeWidth.toPx() / scale,
                            cap = StrokeCap.Round
                        )
                    }
                }

                CreateMaskDownBar(
                    onCancel = { onCancelPredict() },
                    onConfirm = {

                        val maskBitmap = generateCanvasBitmap(
                            originalWidth,
                            originalHeight,
                            imageSize,
                            scale,
                            lines,
                            density
                        )
                        val maskSize = intArrayOf(maskBitmap.width, maskBitmap.height)
                        val selectedImage = image.id

                        maskViewModel.saveMask(maskBitmap, maskSize, selectedImage) { success ->
                            if (success) {
                                println("Máscara guardada exitosamente.")
                            } else {
                                println("Error al guardar la máscara.")
                            }
                        }
                        onCancelPredict()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                )




            } else {

                // Doble tap en modo normal para restaurar zoom/posicion
                Image(
                    bitmap = image.bitmap!!.asImageBitmap(),
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
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = 1f
                                    offset = Offset.Zero
                                },
                                onTap = { tapOffset ->
                                    maskViewModel.detectMaskTap(
                                        tapOffset,
                                        imageSize
                                    )
                                }
                            )
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

        if (isLoading || image.bitmap == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF34D399))
            }
        }

    }

}



fun generateCanvasBitmap(
    originalWidth: Int,
    originalHeight: Int,
    imageSize: IntSize,
    scale: Float,
    lines: List<Line>,
    density: Density
): Bitmap {
    // Crear el bitmap con las dimensiones originales de la imagen
    val bitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    if (lines.isNotEmpty()) {
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            color = android.graphics.Color.RED
            strokeCap = Paint.Cap.ROUND
            strokeWidth = with(density) { lines[0].strokeWidth.toPx() /scale} // Grosor en espacio original
        }

        // Escalar las coordenadas de las líneas a las dimensiones originales
        val scaleX = originalWidth.toFloat() / imageSize.width.toFloat()
        val scaleY = originalHeight.toFloat() / imageSize.height.toFloat()

        lines.forEach { line ->
            paint.color = line.color.toArgb()
            val startX = line.start.x * scaleX
            val startY = line.start.y * scaleY
            val endX = line.end.x * scaleX
            val endY = line.end.y * scaleY

            canvas.drawLine(
                startX,
                startY,
                endX,
                endY,
                paint
            )
        }
    }

    return bitmap
}

data class Line(
    val start: Offset,
    val end: Offset,
    val color: Color = Color.Blue,
    val strokeWidth: Dp = 40.dp
)
