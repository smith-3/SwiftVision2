package com.stellaridea.swiftvision.ui.views.edition

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.stellaridea.swiftvision.models.masks.Mask

/**
 * Función para detectar qué máscara se toca al hacer tap sobre la imagen.
 * Ajusta la posición del tap según el tamaño de la imagen mostrada y
 * verifica si el pixel no es transparente en el bitmap de la máscara.
 */
fun DetectMaskTap(
    tapPosition: Offset,
    masks: List<Mask>,
    maskSize: IntSize,
    highlightMask: (Mask) -> Unit
) {
    masks.forEach { mask ->
        val maskBitmap = mask.bitmap

        // Ajustamos la posición de toque a las coordenadas de la máscara original
        val adjustedTapPosition = adjustTapPosition(
            tapPosition,
            maskBitmap.width,
            maskBitmap.height,
            maskSize.width,
            maskSize.height
        )

        if (isPointInMask(adjustedTapPosition, maskBitmap)) {
            highlightMask(mask)
            return
        }
    }
}

/**
 * Ajusta la posición del tap para mapear de las coordenadas de la vista (maskSize)
 * a las coordenadas reales del bitmap de la máscara.
 */
private fun adjustTapPosition(
    tapPosition: Offset,
    bitmapWidth: Int,
    bitmapHeight: Int,
    maskWidth: Int,
    maskHeight: Int
): Offset {
    val adjustedX = (tapPosition.x * bitmapWidth / maskWidth).toInt()
    val adjustedY = (tapPosition.y * bitmapHeight / maskHeight).toInt()
    return Offset(adjustedX.toFloat(), adjustedY.toFloat())
}

/**
 * Verifica si el pixel en la posición dada en la máscara es diferente de transparente.
 */
private fun isPointInMask(tapPosition: Offset, maskBitmap: Bitmap): Boolean {
    val x = tapPosition.x.toInt()
    val y = tapPosition.y.toInt()

    if (x in 0 until maskBitmap.width && y in 0 until maskBitmap.height) {
        val pixelColor = maskBitmap.getPixel(x, y)
        // Si no es transparente, lo consideramos parte de la máscara
        return pixelColor != Color.TRANSPARENT
    }
    return false
}
