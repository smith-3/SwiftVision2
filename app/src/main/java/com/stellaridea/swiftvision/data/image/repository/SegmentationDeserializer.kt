package com.stellaridea.swiftvision.data.image.repository

import android.graphics.Bitmap
import android.graphics.Color
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.stellaridea.swiftvision.data.image.model.Mask
import java.lang.reflect.Type

class SegmentationDeserializer : JsonDeserializer<Mask> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Mask {
        val jsonObject = json.asJsonObject
        val encodedCounts = jsonObject.getAsJsonArray("counts")
        val size = context.deserialize<IntArray>(jsonObject.get("size"), IntArray::class.java)
        val width = size[0]
        val height = size[1]
        val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height) { Color.TRANSPARENT } // Initialize with transparent color
        var y = 0
        val redColor = Color.BLUE
        for (element in encodedCounts) {
            val rowArray = element.asJsonArray
            val row = decodeRow(rowArray)
            val count = rowArray.get(1).asInt // Obtén el conteo de la segunda posición
            val rowLength = row.size
            val rowPixels = IntArray(width) { i ->
                if (i < rowLength && row[i]) redColor else Color.TRANSPARENT
            }
            for (i in 0 until count) {
                System.arraycopy(rowPixels, 0, pixels, y * width, rowPixels.size)
                y++
            }
        }
        maskBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return Mask(0,maskBitmap, size)
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
