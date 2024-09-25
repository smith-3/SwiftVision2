package com.stellaridea.swiftvision.ui.common.menu

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.common.ButtonIconX
import com.stellaridea.swiftvision.ui.navigation.Graph
import com.stellaridea.swiftvision.ui.views.camera.CameraViewModel

@Composable
fun Menu(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    imageCapture: ImageCapture
) {
    val cameraViewModel: CameraViewModel = hiltViewModel()
    val context = LocalContext.current
    val itemList = listOf(
        Items.Switch,
        Items.Capture,
        Items.Gallery
    )
    var camera_value by remember { mutableStateOf(false) }


    val launcherGallery = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        it?.let{
            camera_value = false
            cameraViewModel.capturePicture(context,it){
                navController.navigate(Graph.IMAGE_DETAIL)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            itemList.forEach { item ->
                val icon = item.icon
                ButtonIconX(
                    modifier = if (item == Items.Capture) Modifier.size(60.dp) else Modifier.size(50.dp),
                    imageVector = icon,
                    color = if (item == Items.Capture) Color.Black else MaterialTheme.colorScheme.background,
                    colorIcon = if (item == Items.Capture) Color.White else Color.Black,
                    isLoading = if(item ==Items.Capture) camera_value else false
                ) {
                    when (item) {
                        Items.Switch -> {
                            cameraViewModel.toggleCamera();
                        }
                        Items.Capture -> {
                            camera_value = true
                            cameraViewModel.capturePicture(
                                context = context,
                                captureImage = imageCapture
                            ) {it ->
                                Log.i("Image", "Imagen capturada :${it.bitmap}")
                                navController.navigate(Graph.IMAGE_DETAIL)
                            }
                        }
                        Items.Gallery -> {
                            launcherGallery.launch("image/*")
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
