package com.stellaridea.swiftvision.camera.usecase

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.stellaridea.swiftvision.models.masks.Mask
import kotlin.math.roundToInt

fun DetectMaskTap(
    tapPosition: Offset,
    masks: List<Mask>,
    maskSize: IntSize,
    highlightMask: (Mask) -> Unit
) {
    masks.forEachIndexed { index, mask ->
        Log.d("selectMask", "Comparing Mask $index: $mask")
        val maskBitmap = mask.bitmap
        val adjustedTapPosition = adjustTapPosition(
            tapPosition,
            maskBitmap.width,
            maskBitmap.height,
            maskSize.width,
            maskSize.height
        )
        if (isPointInMask(adjustedTapPosition, maskBitmap)) {
            Log.d("selectMask", "Mask Selected: $mask")
            highlightMask(mask)
            return
        }
    }
    Log.d("selectMask", "No mask selected.")
}

private fun adjustTapPosition(
    tapPosition: Offset,
    bitmapWidth: Int,
    bitmapHeight: Int,
    maskWidth: Int,
    maskHeight: Int
): Offset {
    val adjustedX = (tapPosition.x * bitmapWidth / maskWidth).roundToInt()
    val adjustedY = (tapPosition.y * bitmapHeight / maskHeight).roundToInt()
    return Offset(adjustedX.toFloat(), adjustedY.toFloat())
}

private fun isPointInMask(tapPosition: Offset, maskBitmap: Bitmap): Boolean {
    val x = tapPosition.x.roundToInt()
    val y = tapPosition.y.roundToInt()
    if (x in 0 until maskBitmap.width && y in 0 until maskBitmap.height) {
        val pixelColor = maskBitmap.getPixel(x, y)
        return pixelColor == Color.BLUE
    }
    return false
}


fun getPixelPositionFromByteArray(
    bitmap: Bitmap,
    offset: Offset,
    imageSize: IntSize,
): Pair<Int, Int>? {
    val tag = "detectPoints"
    Log.i(tag, "Original Image Size: ${imageSize.width}x${imageSize.height}")
    // Verificar si la posición ajustada está dentro de los límites
    if (offset.x < 0 || offset.y < 0 || offset.x >= imageSize.width || offset.y >= imageSize.height) {
        Log.e(tag, "Adjusted position out of bounds: ($offset.x, $offset.y)")
        return null
    }
    val x = offset.x.roundToInt()
    val y = offset.y.roundToInt()
    val pixelIndex = bitmap.getPixel(x, y)
    Log.i(tag, "Pixel Index: $pixelIndex")
    Log.i(tag, "Returning pixel position: ($x, $y)")
    return Pair(x, y)
}
