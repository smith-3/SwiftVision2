package com.stellaridea.swiftvision.ui.views.edition

import com.stellaridea.swiftvision.ui.views.image.MasksList
import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.models.images.ImageModel
import com.stellaridea.swiftvision.ui.common.ConfirmDialog
import com.stellaridea.swiftvision.ui.common.MaskImageOverlay
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.views.camera.CameraViewModel

@SuppressLint("UnrememberedGetBackStackEntry", "UnusedBoxWithConstraintsScope")
@Composable
fun EditionScreen(
    navController: NavHostController,
    projectId: Int,
    projectName: String
) {
    val editionViewModel: EditionViewModel = hiltViewModel()
    val selectedProject by editionViewModel.selectedProject.observeAsState()
    val selectedImage by editionViewModel.selectedImage.observeAsState()
    val isLoading by editionViewModel.isLoading.observeAsState(false)
    val isImageLoading by editionViewModel.isImageLoading.observeAsState(false)
    val isMaskLoading by editionViewModel.isMaskLoading.observeAsState(false)
    var showDialog by remember { mutableStateOf(false) }

    // Initialize project and images
    LaunchedEffect(Unit) {
        editionViewModel.initializeProject(projectId, projectName)
    }

    BackHandler {
        showDialog = true
    }

    ConfirmNavigationDialog(showDialog, navController) { dialogState ->
        showDialog = dialogState
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            selectedProject?.let { project ->
                selectedImage?.let { image ->
                    ImageViewer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        image = image,
                        editionViewModel = editionViewModel,
                        isImageLoading = isImageLoading,
                        isMaskLoading = isMaskLoading
                    )
                }
                // Optionally display a list of images for selection
                MasksList(
                    images = project.images,
                    selectedImage = selectedImage,
                    onImageSelected = { editionViewModel.selectImage(it) }
                )
            }
        }
    }
}

@Composable
fun ImageViewer(
    modifier: Modifier,
    image: Image,
    editionViewModel: EditionViewModel,
    isImageLoading: Boolean,
    isMaskLoading: Boolean
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val state = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 5f)

            val containerWidth = constraints.maxWidth
            val containerHeight = constraints.maxHeight

            val imageWidth = containerWidth * scale
            val imageHeight = containerHeight * scale

            val maxOffsetX = (imageWidth - containerWidth) / 2
            val maxOffsetY = (imageHeight - containerHeight) / 2

            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
            )
        }

        Image(
            bitmap = image.bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .onGloballyPositioned { coordinates ->
                    imageSize = coordinates.size
                }
                .transformable(state)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        editionViewModel.detectMaskTap(
                            tapOffset,
                            imageSize
                        )
                    }
                }
        )

        // Display masks
        image.masks.forEach { mask ->
            if (mask.active) {
                val maskBitmap = mask.bitmap.asImageBitmap()
                MaskImageOverlay(
                    maskBitmap = maskBitmap,
                    scale = scale,
                    offset = offset
                )
            }
        }

        // Show loading indicators
        if (isImageLoading || isMaskLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
            }
        }
    }
}

@Composable
fun ConfirmNavigationDialog(
    showDialog: Boolean,
    navController: NavHostController,
    onDialogStateChange: (Boolean) -> Unit
) {
    if (showDialog) {
        ConfirmDialog(
            onConfirm = {
                onDialogStateChange(false)
                navController.popBackStack()
            },
            onDismiss = { onDialogStateChange(false) },
            title = "Confirmación",
            message = "¿Estás seguro de que deseas volver atrás y perder tu progreso?"
        )
    }
}

@Composable
fun MasksList(
    images: List<Image>,
    selectedImage: Image?,
    onImageSelected: (Image) -> Unit
) {
    Column {
        images.forEach { image ->
            // Highlight selected image
            val isSelected = image.id == selectedImage?.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) Color.Gray else Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onImageSelected(image)
                        }
                    }
            ) {
                Image(
                    bitmap = image.bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
