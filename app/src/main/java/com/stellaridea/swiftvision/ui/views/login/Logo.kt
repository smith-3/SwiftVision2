package com.stellaridea.swiftvision.ui.views.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.stellaridea.swiftvision.R

@Composable
fun Logo(modifier: Modifier) {
    Image(
        modifier = modifier
            .size(120.dp)
            .padding(10.dp),
        painter = painterResource(id = R.mipmap.ic_launcher_round), // Usa el nombre que seleccionaste
        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
        contentDescription = "contentDescription",
        )
}