package com.stellaridea.swiftvision.data.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.stellaridea.swiftvision.models.masks.Mask
import java.lang.reflect.Type
import java.util.Date

class SegmentationDeserializer : JsonDeserializer<Mask> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Mask {
        val jsonObject = json.asJsonObject

        val id = jsonObject.get("id")?.asLong ?: Date().time
        val imageId = jsonObject.get("image_id")?.asInt ?: 0
        val active = jsonObject.get("active")?.asBoolean ?: false

        // El campo "size" es un string como "(width, height)"
        val sizeString = jsonObject.get("size")?.asString ?: "(0, 0)"
        val size = parseSize(sizeString)
        val width = size[0]
        val height = size[1]

        // "counts" es un string complejo. Tenemos que parsearlo.
        val countsString = jsonObject.get("counts")?.asString ?: "[]"
        val maskBitmap = decodeCounts(countsString, width, height)

        return Mask(
            id = id,
            bitmap = maskBitmap,
            size = size,
            active = active
        )
    }

    /**
     * Parsea el campo `size` desde el formato "(width, height)".
     */
    private fun parseSize(sizeString: String): IntArray {
        val numbers = sizeString.removeSurrounding("(", ")").split(",")
            .map { it.trim().toInt() }
        return intArrayOf(numbers[0], numbers[1])
    }

    /**
     * Decodifica el campo "counts" en su formato comprimido, similar a la lógica Python.
     * Formato: "[([(l1, v1), (l2, v2), ...], count), ([(...)], count), ...]"
     */
    private fun decodeCounts(countsString: String, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height) { Color.TRANSPARENT }

        // Removemos los corchetes del principio y del final
        val trimmed = countsString.trim().removePrefix("[").removeSuffix("]")

        // Cada elemento se ajusta al patrón: "([(x,y), (x,y), ...], count)"
        // Usaremos una regex para extraer row_tuples y count.
        // Patrón para extraer cada bloque: \(\[\((.*?)\)\], *(\d+)\)
        // (.*?) captura todo el contenido entre "((...))"
        // (\d+) captura el count
        val pattern = """\(\[\((.*?)\)\],\s*(\d+)\)""".toRegex()
        val matches = pattern.findAll(trimmed)

        var y = 0
        val color = Color.BLUE

        for (match in matches) {
            val rowData = match.groupValues[1] // Ej: "683, 0), (7, 1), (206, 0"
            val count = match.groupValues[2].toInt()

            val pairs = parsePairs(rowData) // Lista de (length, value)
            val rowPixels = IntArray(width) { Color.TRANSPARENT }

            var x = 0
            for ((length, value) in pairs) {
                val pixelColor = if (value == 1) color else Color.TRANSPARENT
                for (i in 0 until length) {
                    if (x < width) {
                        rowPixels[x++] = pixelColor
                    } else {
                        break
                    }
                }
            }

            // Repetimos esta fila 'count' veces
            for (i in 0 until count) {
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

    /**
     * Parsea los pares (length, value) de una cadena como "683, 0), (7, 1), (206, 0"
     * Dividimos por "), (" para separar cada par.
     */
    private fun parsePairs(data: String): List<Pair<Int, Int>> {
        // Separar por "), (" para aislar cada (length, value)
        val pairStrings = data.split("),")
            .map { it.replace("(", "").replace(")", "").trim() }
            .filter { it.isNotBlank() }

        val pairs = mutableListOf<Pair<Int, Int>>()
        for (ps in pairStrings) {
            val parts = ps.split(",").map { it.trim() }
            val length = parts[0].toInt()
            val value = parts[1].toInt()
            pairs.add(length to value)
        }
        return pairs
    }
}
