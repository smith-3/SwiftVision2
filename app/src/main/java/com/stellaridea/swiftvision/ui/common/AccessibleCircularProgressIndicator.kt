package com.stellaridea.swiftvision.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AccessibleCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    strokeWidth: Dp = 2.dp,
    contentDescription: String
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(24.dp)
            .semantics { this.contentDescription = contentDescription },
        color = color,
        strokeWidth = strokeWidth
    )
}
