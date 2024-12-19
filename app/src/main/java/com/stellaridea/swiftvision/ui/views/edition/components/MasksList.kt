package com.stellaridea.swiftvision.ui.views.edition.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.MaskViewModel

@Composable
fun MasksList(
    imageBitmap: ImageBitmap?,
    state: LazyListState = rememberLazyListState(),
    viewModel: MaskViewModel,
    onAddMaskClick: () -> Unit // Callback para agregar una nueva máscara
) {
    val masks by viewModel.masks.observeAsState(emptyList())
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Color.Black),
        state = state
    ) {
        // Agregar el botón de "nueva máscara" como el primer elemento
        item {
            AddMaskButton(onClick = onAddMaskClick)
        }

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
                    onClick = { viewModel.toggleMaskSelection(mask.id) }
                )
            }
        }
    }
}

@Composable
fun AddMaskButton(onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .size(80.dp)
            .padding(8.dp)
            .clickable(onClick = onClick),
        border = BorderStroke(2.dp, Color.Black),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Gray
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Mask",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}