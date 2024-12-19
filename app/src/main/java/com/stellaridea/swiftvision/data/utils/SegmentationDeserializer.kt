package com.stellaridea.swiftvision.data.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.google.gson.*
import com.stellaridea.swiftvision.models.masks.Mask
import java.lang.reflect.Type
import java.util.Date

class SegmentationDeserializer : JsonDeserializer<Mask> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Mask {
        val jsonObject = json.asJsonObject

        // Obtener campos básicos con valores por defecto
        val id = jsonObject.get("id")?.asLong ?: Date().time
        val imageId = jsonObject.get("image_id")?.asInt ?: 0

        // Parsear el campo "size" como lista de enteros
        val sizeJsonArray = jsonObject.getAsJsonArray("size") ?: JsonArray()
        val size = parseSize(sizeJsonArray)
        val width = size[0]
        val height = size[1]

        // Parsear el campo "counts" como JsonArray
        val countsJsonElement = jsonObject.get("counts") ?: JsonArray()
        if (!countsJsonElement.isJsonArray) {
            throw JsonParseException("El campo 'counts' no es un JsonArray.")
        }
        val countsJsonArray = countsJsonElement.asJsonArray

        // Decodificar counts para obtener la máscara Bitmap
        val maskBitmap = decodeCounts(countsJsonArray, width, height)

        return Mask(
            id = id,
            bitmap = maskBitmap,
            size = size,
            active = false
        )
    }

    /**
     * Parsea el campo `size` desde un JsonArray.
     */
    private fun parseSize(sizeJsonArray: JsonArray): IntArray {
        return if (sizeJsonArray.size() >= 2) {
            intArrayOf(
                sizeJsonArray[0].asInt,
                sizeJsonArray[1].asInt
            )
        } else {
            intArrayOf(0, 0)
        }
    }

    /**
     * Decodifica el campo "counts" en su formato comprimido.
     * Formato esperado: [
     *   [[length, value], count],
     *   ...
     * ]
     */
    private fun decodeCounts(countsJsonArray: JsonArray, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height) { Color.TRANSPARENT }

        var y = 0
        val color = ColorUtils.setAlphaComponent(Color.BLUE, 128) // 128 es el valor alfa

        for (row in countsJsonArray) {
            val rowJsonArray = row.asJsonArray
            val rowPairsJsonArray = rowJsonArray[0].asJsonArray
            val repeatCount = rowJsonArray[1].asInt

            val rowPixels = IntArray(width) { Color.TRANSPARENT }
            var x = 0

            for (pairJsonArray in rowPairsJsonArray) {
                val length = pairJsonArray.asJsonArray[0].asInt
                val value = pairJsonArray.asJsonArray[1].asInt
                val pixelColor = if (value == 1) color else Color.TRANSPARENT

                for (i in 0 until length) {
                    if (x < width) {
                        rowPixels[x++] = pixelColor
                    } else {
                        break
                    }
                }
            }

            // Repetir la fila el número de veces indicado
            for (i in 0 until repeatCount) {
                if (y < height) {
                    System.arraycopy(rowPixels, 0, pixels, y * width, width)
                    y++
                } else {
                    break
                }
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
