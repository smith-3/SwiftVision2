package com.stellaridea.swiftvision.ui.views.image

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.stellaridea.swiftvision.data.mask.model.Mask
import com.stellaridea.swiftvision.ui.views.camera.CameraViewModel
@Composable
fun MasksList(
    imageBitmap: ImageBitmap,
    masks: List<Mask>,
    state: LazyListState = rememberLazyListState(),
    viewModel: CameraViewModel
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color.Black),
        state = state
    ) {
        if (masks.isEmpty()) {
            // Mostrar placeholders con el círculo de carga
            items(5) { // Puedes cambiar el número de placeholders
                PlaceholderMask()
            }
        } else {
            items(masks.size) { index ->
                val mask = masks[index]
                ItemMask(
                    mask = mask,
                    imageBitmap = imageBitmap,
                    isSelected = mask.active,
                    onClick = { viewModel.toggleMaskSelection(mask) }
                )
            }
        }
    }
}

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

@Composable
fun ItemMask(
    mask: Mask,
    imageBitmap: ImageBitmap,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val maskBitmap = mask.bitmap
    val maskImageBitmap = maskBitmap.asImageBitmap()

    OutlinedCard(
        modifier = Modifier
            .size(width = 90.dp, height = 90.dp)
            .padding(4.dp)
            .clickable { onClick() },
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF4BE08F) else Color.Transparent),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(bitmap = imageBitmap, contentDescription = "Image")
            Image(bitmap = maskImageBitmap, contentDescription = "Mask")

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    tint = Color(0xFF4BE08F)
                )
            }
        }
    }
}
