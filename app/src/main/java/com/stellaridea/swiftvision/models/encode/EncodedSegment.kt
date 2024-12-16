package com.stellaridea.swiftvision.data.utils

/**
 * Representa un "segmento" en la codificación RLE de una fila.
 * length: número de píxeles consecutivos con el mismo valor
 * value: 0 o 1 (falso/verdadero)
 */
data class EncodedSegment(
    val length: Int,
    val value: Int
)