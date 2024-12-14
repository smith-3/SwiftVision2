package com.stellaridea.swiftvision.data.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.stellaridea.swiftvision.models.masks.Mask
import java.lang.reflect.Type
import java.util.Date

class SegmentationDeserializer : JsonDeserializer<Mask> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Mask {
        val jsonObject = json.asJsonObject

        // Extrae los nuevos parámetros de Mask
        val id = jsonObject.get("id")?.asLong ?: Date().time  // Genera una ID única si no está presente
        val imageId = jsonObject.get("image_id")?.asInt ?: 0
        val active = jsonObject.get("active")?.asBoolean ?: false
        val createdAt = jsonObject.get("created_at")?.asLong ?: System.currentTimeMillis()

        // Extrae y decodifica el tamaño y el bitmap
        val encodedCounts = jsonObject.getAsJsonArray("counts")
        val size = context.deserialize<IntArray>(jsonObject.get("size"), IntArray::class.java)
        val width = size[0]
        val height = size[1]
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height) { Color.TRANSPARENT } // Inicializa con color transparente

        var y = 0
        val redBlue = Color.BLUE
        for (element in encodedCounts) {
            val rowArray = element.asJsonArray
            val row = decodeRow(rowArray)
            val count = rowArray.get(1).asInt  // Obtén el conteo de la segunda posición
            val rowLength = row.size
            val rowPixels = IntArray(width) { i ->
                if (i < rowLength && row[i]) redBlue else Color.TRANSPARENT
            }
            for (i in 0 until count) {
                System.arraycopy(rowPixels, 0, pixels, y * width, rowPixels.size)
                y++
            }
        }

        maskBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        // Devuelve la instancia de Mask con todos los parámetros incluidos
        return Mask(
            id = id,
            bitmap = maskBitmap,
            size = size,
            active = active,
        )
    }

    private fun decodeRow(rowArray: JsonArray): BooleanArray {
        val countsArray = rowArray.get(0).asJsonArray
        val rowArrayResult = mutableListOf<Boolean>()
        for (i in 0 until countsArray.size()) {
            val count = countsArray[i].asJsonArray.get(0).asInt
            val value = countsArray[i].asJsonArray.get(1).asInt
            repeat(count) {
                rowArrayResult.add(value == 1)
            }
        }
        return rowArrayResult.toBooleanArray()
    }
}
