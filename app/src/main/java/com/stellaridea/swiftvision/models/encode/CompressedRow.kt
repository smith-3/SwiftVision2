package com.stellaridea.swiftvision.models.encode

import com.stellaridea.swiftvision.data.utils.EncodedSegment

/**
 * Representa una fila codificada, junto con el número de repeticiones consecutivas (para compresión adicional).
 * rowSegments: lista de segmentos [(l1, v1), (l2, v2), ...] que describen la fila
 * count: cuántas veces seguidas se repite esta fila
 */
data class CompressedRow(
    val rowSegments: List<EncodedSegment>,
    val count: Int
)