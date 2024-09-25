package com.stellaridea.swiftvision.ui.views.camera

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.common.AppName
import com.stellaridea.swiftvision.ui.common.ButtonIconX
import com.stellaridea.swiftvision.ui.common.IconHome
import com.stellaridea.swiftvision.ui.common.menu.Menu
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

    LaunchedEffect(cameraSelectorCurrent) {
        val cameraProvider = context.cameraProvider()
        val lensFacing =
            if (cameraSelectorCurrent == false) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val preview =
            Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        cameraProvider.let {
            it.unbindAll()
            val camera = it.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, preview)
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
                    .clip(RoundedCornerShape(15.dp)) // Ajusta el valor dp según tus necesidades
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
                        IconHome()
                        AppName(Modifier.weight(1f))
                        if (cameraSelectorCurrent == true) {
                            ButtonIconX(
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
                imageCapture = imageCapture
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