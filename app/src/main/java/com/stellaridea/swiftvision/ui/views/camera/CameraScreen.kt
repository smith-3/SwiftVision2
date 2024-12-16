package com.stellaridea.swiftvision.ui.views.camera

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.views.camera.menu.Menu

@Composable
fun CameraScreen(navController: NavHostController) {
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraSelectorCurrent by cameraViewModel.cameraSelector.observeAsState()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    var showDialog by remember { mutableStateOf(false) }
    var imageName by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    SetupCamera(previewView, cameraSelectorCurrent, lifecycleOwner, imageCapture)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            CameraPreview(previewView, cameraSelectorCurrent, cameraViewModel, Modifier.weight(1f))

            Menu(
                modifier = Modifier.fillMaxWidth(),
                imageCapture = imageCapture,
                onImageCaptured = { bitmap ->
                    capturedImage = bitmap
                    showDialog = true
                },
            )
        }

        if (showDialog) {
            ProjectCreationDialog(
                imageName = imageName,
                capturedImage = capturedImage,
                isLoading = isLoading,
                onImageNameChange = { imageName = it },
                onConfirm = {
                    capturedImage?.let {
                        isLoading = true
                        cameraViewModel.createProject(
                            imageName = imageName,
                            bitmap = it,
                            onSuccess = { id ->
                                navController.navigate("edition/$id/$imageName")
                                isLoading = false
                                showDialog = false
                            },
                            onFailure = {
                                Toast.makeText(context, "Error al crear el proyecto", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                showDialog = false
                            }
                        )

                    }
                },
                onDismiss = {
                    isLoading = false
                    showDialog = false
                }
            )
        }
    }
}
