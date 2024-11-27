package com.stellaridea.swiftvision.ui.views.image

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
import com.stellaridea.swiftvision.data.image.model.ImageModel
import com.stellaridea.swiftvision.ui.common.ConfirmDialog
import com.stellaridea.swiftvision.ui.common.MaskImageOverlay
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.views.camera.CameraViewModel

@SuppressLint("UnrememberedGetBackStackEntry", "UnusedBoxWithConstraintsScope")
@Composable
fun ImageDetailScreen(navController: NavHostController) {
    val cameraViewModel: CameraViewModel =
        hiltViewModel(navController.getBackStackEntry(Graph.CAMERA))

    val imageModel by cameraViewModel.lastImage.observeAsState()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val newImage by cameraViewModel.newImage.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

    BackHandler {
        showDialog = true
    }

    ConfirmNavigationDialog(showDialog, navController) { dialogState ->
        showDialog = dialogState
    }
    Column(modifier = Modifier.fillMaxSize()) {
        imageModel?.let { imageModel ->
            ImageViewer(modifier = Modifier
                .fillMaxWidth()
                .weight(1f), imageModel, cameraViewModel)
        }
    }
}

@Composable
fun ImageViewer(modifier: Modifier, imageModel: ImageModel, cameraViewModel: CameraViewModel) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val newImage by cameraViewModel.newImage.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

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
            bitmap = imageModel.bitmap.asImageBitmap(),
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
                        Log.d("selectMask", "Tap Offset: $tapOffset")
                        cameraViewModel.detectMaskTap(
                            tapOffset,
                            imageSize  // Usar imageSize en lugar de size
                        )
                    }
                }
        )

        imageModel.masks.forEach { mask ->
            if (mask.active) {
                val maskBitmap = mask.bitmap.asImageBitmap()
                MaskImageOverlay(
                    maskBitmap = maskBitmap,
                    scale = scale,
                    offset = offset
                )
            }

        }
        if (newImage == true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp
                )
            }
        }
    }
    MasksList(
        imageBitmap = imageModel.bitmap.asImageBitmap(),
        masks = imageModel.masks,
        viewModel = cameraViewModel
    )
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