package com.stellaridea.swiftvision.ui.views.camera

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.stellaridea.swiftvision.ui.common.IconButton
import com.stellaridea.swiftvision.ui.views.camera.CameraViewModel

@Composable
fun CameraPreview(
    previewView: PreviewView,
    cameraSelectorCurrent: Boolean?,
    cameraViewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(15.dp))
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        if (cameraSelectorCurrent == true) {
            Box(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                IconButton(imageVector = cameraViewModel.flashIcon) {
                    cameraViewModel.toggleFlashMode()
                }
            }
        }
    }
}
