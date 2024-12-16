package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun PlaceholderMask() {
    OutlinedCard(
        modifier = Modifier
            .size(width = 90.dp, height = 90.dp)
            .padding(4.dp),
        border = BorderStroke(1.dp, Color.Transparent),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Círculo de carga
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp), // Ajusta el tamaño del indicador de carga
                color = Color.Gray,
                strokeWidth = 2.dp
            )
        }
    }
}

