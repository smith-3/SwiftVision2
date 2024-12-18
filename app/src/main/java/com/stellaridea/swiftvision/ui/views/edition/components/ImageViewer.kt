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
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
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
    isImageLoading: Boolean,
    isMaskLoading: Boolean,
    onCancelPredict: () -> Unit
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
            val lines = remember { mutableStateListOf<Line>() }

            Canvas(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .onGloballyPositioned { /* El canvas usará el tamaño de la imagen */ }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            // Limitar el dibujo al área de la imagen
                            val start = change.position - dragAmount
                            val end = change.position
                            val constrainedStart = constrainToImageBounds(start, imageSize, scale, offset)
                            val constrainedEnd = constrainToImageBounds(end, imageSize, scale, offset)

                            if (constrainedStart != null && constrainedEnd != null) {
                                lines.add(Line(start = constrainedStart, end = constrainedEnd))
                            }
                        }
                    }
            ) {
                // Dibuja las líneas dentro del canvas
                lines.forEach { line ->
                    drawLine(
                        color = line.color,
                        start = line.start,
                        end = line.end,
                        strokeWidth = line.strokeWidth.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Barra inferior para Confirmar o Cancelar
            Bar(
                onCancel = { onCancelPredict() },
                onConfirm = { /* Acción confirmar */ },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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


data class Line(
    val start: Offset,
    val end: Offset,
    val color: Color = Color.Blue,
    val strokeWidth: Dp = 40.dp
)
fun constrainToImageBounds(
    point: Offset,
    imageSize: IntSize,
    scale: Float,
    offset: Offset
): Offset? {
    // Escala el tamaño de la imagen
    val scaledWidth = imageSize.width * scale
    val scaledHeight = imageSize.height * scale

    // Calcula los límites visibles de la imagen
    val leftBound = -offset.x
    val topBound = -offset.y
    val rightBound = scaledWidth - offset.x
    val bottomBound = scaledHeight - offset.y

    // Limita las coordenadas dentro de los límites
    val constrainedX = point.x.coerceIn(leftBound, rightBound)
    val constrainedY = point.y.coerceIn(topBound, bottomBound)

    // Verifica si el punto está dentro de los límites
    return if (point.x in leftBound..rightBound && point.y in topBound..bottomBound) {
        Offset(constrainedX, constrainedY)
    } else {
        null // Si el punto está fuera de los límites, no lo retorna
    }
}


//            val lines = remember { mutableStateListOf<Offset>() }
//
//            Canvas(
//                modifier = Modifier
//                    .matchParentSize()
//                    .pointerInput(true) {
//                        detectDragGestures { change, dragAmount ->
//                            change.consume()
//                            // Verificar si la distancia entre el último punto y el nuevo es mayor a un umbral
//                            if (lines.isNotEmpty()) {
//                                val lastPoint = lines.last()
//                                val distance = kotlin.math.sqrt(
//                                    (change.position.x - lastPoint.x).pow(2) +
//                                            (change.position.y - lastPoint.y).pow(2)
//                                )
//
//                                // Si la distancia es mayor que un umbral, vaciar la lista
////                                if (distance > 150f) { // Umbral de 100 píxeles (puedes ajustar este valor)
////                                    lines.clear()
////                                }
//                            }
//
//                            // Agregar la nueva posición al trazo
//                            lines.add(change.position)
//                        }
//                    }
//            ) {
//                val path = Path().apply {
//                    if (lines.isNotEmpty()) {
//                        moveTo(lines.first().x, lines.first().y)
//                        // Crear una curva suave entre los puntos
//                        lines.forEach { point ->
//                            // Puedes usar curvas Bézier aquí para suavizar el trazo
//                            lineTo(point.x, point.y)
//                        }
//                    }
//                }
//
//                drawPath(
//                    path = path,
//                    color = Color.Blue.copy(alpha = 0.7f),  // Cambia el color que deseas
//                    style = Stroke(
//                        width = 60f,  // Grosor del trazo
//                        cap = StrokeCap.Round
//                    )
//                )
//            }
