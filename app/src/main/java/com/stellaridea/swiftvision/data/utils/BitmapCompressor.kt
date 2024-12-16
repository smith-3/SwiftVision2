package com.stellaridea.swiftvision.data.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.gson.Gson
import com.stellaridea.swiftvision.models.encode.CompressedRow

object BitmapCompressor {

    /**
     * Convierte un Bitmap a una matriz booleana, donde `true` indica "selección" (p.e. píxel blanco)
     * y `false` indica "no selección". Puedes ajustar la condición a tu criterio.
     */
    private fun bitmapToBooleanMatrix(
        bitmap: Bitmap,
        selectionColor: Int = Color.WHITE
    ): Array<BooleanArray> {
        val width = bitmap.width
        val height = bitmap.height
        val matrix = Array(height) { BooleanArray(width) { false } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                // Ajusta la condición: podría ser pixel == selectionColor, alpha != 0, etc.
                matrix[y][x] = (pixel == selectionColor)
            }
        }
        return matrix
    }

    /**
     * Aplica Run-Length Encoding (RLE) a una sola fila booleana.
     */
    private fun runLengthEncodeRow(row: BooleanArray): List<EncodedSegment> {
        if (row.isEmpty()) return emptyList()

        val segments = mutableListOf<EncodedSegment>()
        var currentValue = row[0]
        var count = 1

        for (i in 1 until row.size) {
            if (row[i] == currentValue) {
                count++
            } else {
                segments.add(EncodedSegment(length = count, value = if (currentValue) 1 else 0))
                currentValue = row[i]
                count = 1
            }
        }
        // Añadimos el último segmento
        segments.add(EncodedSegment(length = count, value = if (currentValue) 1 else 0))
        return segments
    }

    /**
     * Aplica RLE a cada fila de una matriz booleana.
     */
    private fun runLengthEncodeMatrix(matrix: Array<BooleanArray>): List<List<EncodedSegment>> {
        val encodedMatrix = mutableListOf<List<EncodedSegment>>()
        for (row in matrix) {
            encodedMatrix.add(runLengthEncodeRow(row))
        }
        return encodedMatrix
    }

    /**
     * Comprime filas repetidas consecutivamente.
     * Si la misma secuencia de segmentos se repite N veces, creamos un solo registro con count = N.
     */
    private fun compressEncodedMatrix(
        encodedMatrix: List<List<EncodedSegment>>
    ): List<CompressedRow> {
        if (encodedMatrix.isEmpty()) return emptyList()

        val compressed = mutableListOf<CompressedRow>()
        var currentRow = encodedMatrix[0]
        var currentCount = 1

        for (i in 1 until encodedMatrix.size) {
            val row = encodedMatrix[i]
            if (row == currentRow) {
                currentCount++
            } else {
                compressed.add(CompressedRow(currentRow, currentCount))
                currentRow = row
                currentCount = 1
            }
        }
        // Añadimos la última fila comprimida
        compressed.add(CompressedRow(currentRow, currentCount))
        return compressed
    }

    /**
     * Función principal: convierte un Bitmap en un JSON con la matriz comprimida RLE.
     * [selectionColor] define qué color consideras "seleccionado".
     */
    fun compressBitmapSelectionToJson(bitmap: Bitmap, selectionColor: Int = Color.WHITE): String {
        // 1. Convertimos el Bitmap en matriz booleana
        val matrix = bitmapToBooleanMatrix(bitmap, selectionColor)

        // 2. RLE por fila
        val encodedMatrix = runLengthEncodeMatrix(matrix)

        // 3. Compresión de filas
        val compressedData = compressEncodedMatrix(encodedMatrix)

        // 4. Convertimos a JSON
        return Gson().toJson(compressedData)
    }
}
