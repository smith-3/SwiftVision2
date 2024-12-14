package com.stellaridea.swiftvision.ui.views.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.common.IconButton
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.navigation.GraphRoot
import com.stellaridea.swiftvision.ui.views.camera.menu.Menu
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CameraScreen(navController: NavHostController) {
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraController = remember { LifecycleCameraController(context) }
    val cameraSelectorCurrent by cameraViewModel.cameraSelector.observeAsState()
    val imageCapture = remember {
        ImageCapture.Builder()
            .build()
    }

    var showDialog by remember { mutableStateOf(false) }
    var imageName by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(cameraSelectorCurrent) {
        val cameraProvider = context.cameraProvider()
        val lensFacing =
            if (cameraSelectorCurrent == false) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val preview =
            Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        cameraProvider.let {
            it.unbindAll()
            it.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, preview)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(15.dp))
            ) {
                AndroidView(
                    factory = { previewView }, modifier = Modifier
                        .fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (cameraSelectorCurrent == true) {
                            IconButton(
                                imageVector = cameraViewModel.flashIcon
                            ) {
                                cameraViewModel.toggleFlashMode()
                            }
                        }
                    }
                }
            }

            Menu(
                modifier = Modifier.fillMaxWidth(),
                navController = navController,
                imageCapture = imageCapture,
                onImageCaptured = { bitmap ->
                    capturedImage = bitmap
                    showDialog = true
                }
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Crear Proyecto") },
                text = {
                    Column {
                        TextField(
                            value = imageName,
                            onValueChange = { imageName = it },
                            label = { Text("Nombre del Proyecto") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            capturedImage?.let {
                                cameraViewModel.createProject(
                                    context = context,
                                    imageName = imageName,
                                    bitmap = it,
                                    onSuccess = {it ->
                                        showDialog = false
                                        navController.navigate(Graph.IMAGE_DETAIL)
                                    },
                                    onFailure = {
                                        showDialog = false
                                        Toast.makeText(
                                            context,
                                            "Error al crear el proyecto",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}


suspend fun Context.cameraProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            cont.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}