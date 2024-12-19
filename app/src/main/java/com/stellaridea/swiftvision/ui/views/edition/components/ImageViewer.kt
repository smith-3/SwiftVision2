package com.stellaridea.swiftvision.ui.views.edition.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import androidx.compose.ui.unit.Density

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.ui.common.MaskImageOverlay
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.MaskViewModel
import kotlin.math.pow

@Composable
fun ImageViewer(
    image: Image,
    maskViewModel: MaskViewModel,
    isPredictMode: Boolean,
    isLoading: Boolean,
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
                    .fillMaxWidth()
                    .transformable(state)

            )

            if (isPredictMode) {
                val lines = remember {
                    mutableStateListOf<Line>()
                }
                Canvas(
                    modifier = Modifier
                        .size(
                            with(LocalDensity.current) { (imageSize.width).toDp() },
                            with(LocalDensity.current) { (imageSize.height).toDp() }
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
                                val canvasBounds =
                                    Rect(
                                        0f,
                                        0f,
                                        imageSize.width.toFloat(),
                                        imageSize.height.toFloat()
                                    )
                                if (!canvasBounds.contains(change.position)) {
                                    return@detectDragGestures // Si está fuera del Canvas, no hacemos nada
                                }

                                if (lines.isNotEmpty()) {
                                    val lastPoint = lines.last()
                                    val distance = kotlin.math.sqrt(
                                        (change.position.x - lastPoint.end.x).pow(2) +
                                                (change.position.y - lastPoint.end.y).pow(2)
                                    )
                                    if (distance > 150f) { // Umbral de 150 píxeles
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
                        color = Color.Red, // Color de los bordes
                        size = size,
                        style = Stroke(width = 4f) // Grosor del borde
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
                        val maskBitmap =
                            generateCanvasBitmap(imageSize, scale, lines, density)
                        val maskSize = intArrayOf(maskBitmap.width, maskBitmap.height)
                        maskViewModel.saveMask(maskBitmap, maskSize) { success ->
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
                // Modo normal: toques para seleccionar máscaras
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
        if (isLoading || image.bitmap == null){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black)
                    .graphicsLayer {
                        alpha = 0.5f
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

fun generateCanvasBitmap(
    imageSize: IntSize,
    scale: Float,
    lines: List<Line>,
    density: Density
): Bitmap {
    // Crear un Bitmap con las dimensiones del Canvas
    val bitmap = Bitmap.createBitmap(imageSize.width, imageSize.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Dibujar fondo blanco
    canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    // Dibujar las líneas
    val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = android.graphics.Color.RED
        strokeCap = Paint.Cap.ROUND
        strokeWidth = with(density) { lines.get(0).strokeWidth.toPx() } / scale
    }

    lines.forEach { line ->
        paint.color = line.color.toArgb() // Convertir Color de Compose a Android
        // Convertir strokeWidth de Dp a píxeles usando la densidad actual

        //paint.strokeWidth = with(density) { line.strokeWidth.toPx() } / scale
        canvas.drawLine(
            line.start.x,
            line.start.y,
            line.end.x,
            line.end.y,
            paint
        )
    }


    return bitmap
}

data class Line(
    val start: Offset,
    val end: Offset,
    val color: Color = Color.Blue,
    val strokeWidth: Dp = 40.dp
)

