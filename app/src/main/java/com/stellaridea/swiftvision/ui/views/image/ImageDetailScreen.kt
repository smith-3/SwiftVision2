package com.stellaridea.swiftvision.ui.views.image

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.common.ButtonIconX
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.views.camera.CameraViewModel

@SuppressLint("UnrememberedGetBackStackEntry", "UnusedBoxWithConstraintsScope")
@Composable
fun ImageDetailScreen(navController: NavHostController) {
    val cameraViewModel: CameraViewModel = hiltViewModel(navController.getBackStackEntry(Graph.CAMERA))
    val predict by cameraViewModel.predict.observeAsState()
    var modePredict by remember { mutableStateOf(false) }  // Cambiado a un estado variable

    val imageModel by cameraViewModel.lastImage.observeAsState()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var value by remember { mutableStateOf(true) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var showDialog by remember { mutableStateOf(false) }
    val newImage by cameraViewModel.newImage.observeAsState()

    BackHandler {
        showDialog = true
    }

    if (showDialog) {
        ConfirmBackDialog(
            onConfirm = {
                showDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        imageModel?.let { imageModel ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                                if (modePredict) {
                                    cameraViewModel.detectPointsNewMask(
                                        tapOffset,
                                        imageSize,  // Usar imageSize en lugar de size
                                        value
                                    )
                                } else {
                                    cameraViewModel.detectMaskTap(
                                        tapOffset,
                                        imageSize  // Usar imageSize en lugar de size
                                    )
                                }
                            }
                        }
                )

                if (modePredict) {
                    predict?.points?.forEachIndexed { index, point ->
                        PointOverlay(
                            point = point,
                            label = predict!!.labels[index],
                            scale = scale,
                            offset = offset,
                            imageSize = IntSize(imageModel.bitmap.width, imageModel.bitmap.height),
                            imageSizeDelay = imageSize,
                        )
                    }
                } else {
                    imageModel.masks?.forEach { mask ->
                        if (mask.active) {
                            val maskBitmap = mask.bitmap.asImageBitmap()
                            MaskImageOverlay(
                                maskBitmap = maskBitmap,
                                scale = scale,
                                offset = offset
                            )
                        }
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

            if (!modePredict) {
                imageModel.masks?.let { masks ->
                    MasksList(
                        imageBitmap = imageModel.bitmap.asImageBitmap(),
                        masks = masks,
                        viewModel = cameraViewModel
                    )
                }
            }
        }

        if (modePredict) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ButtonIconX(imageVector = Icons.AutoMirrored.Filled.Reply) {
                    cameraViewModel.undoLastPoint()
                }
                Spacer(modifier = Modifier.size(5.dp))
                ButtonIconX(
                    imageVector = if (value) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    colorIcon = if (value) Color(0xFF4CAF50) else Color(0xFFF44336),
                    color = Color(0xFF2E2E2E)
                ) {
                    value = !value
                }
                Spacer(modifier = Modifier.size(5.dp))

                Button(
                    onClick = { cameraViewModel.toogleModoPredict() },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF424242),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Cancelar")
                }
                Spacer(modifier = Modifier.size(5.dp))
                Button(
                    onClick = { cameraViewModel.masksPoints() },
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Solicitar")
                }
            }
        }
    }
}


@Composable
fun MaskImageOverlay(
    maskBitmap: ImageBitmap,
    scale: Float,
    offset: Offset
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            bitmap = maskBitmap,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

@Composable
fun PointOverlay(
    point: Pair<Int, Int>,
    label: Int,
    offset: Offset,
    imageSize: IntSize,
    imageSizeDelay: IntSize,
    scale: Float,
) {
    val medioX = imageSizeDelay.width/2*scale
    val medioY = imageSizeDelay.height/2*scale
    val translationX = offset.x-medioX + (point.first.toFloat()) * scale
    val translationY = offset.y-medioY + (point.second.toFloat()) * scale

    // Imprime los valores para depuración
    Log.d("PointOverlay", "Point: $point")
    Log.d("PointOverlay", "Offset: $offset")
    Log.d("PointOverlay", "ImageSize: $imageSize")
    Log.d("PointOverlay", "Scale: $scale")
    Log.d("PointOverlay", "TranslationX: $translationX, TranslationY: $translationY")

    Box(
        modifier = Modifier
            .size(10.dp)
            .graphicsLayer {
                this.translationX = translationX
                this.translationY = translationY
            }
    ) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = "Point",
            tint = if(label==1) Color.Green else Color.Red,
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun ConfirmBackDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Confirmación") },
        text = { Text("¿Estás seguro de que deseas volver atrás y perder tu progreso?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sí")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}