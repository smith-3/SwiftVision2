package com.stellaridea.swiftvision.ui.views.camera.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class Items(
    val icon: ImageVector,
    val title: String?,
    val size: Dp = 50.dp,
) {
    object Switch : Items(Icons.Filled.Cameraswitch, "Switch Camera")
    object Capture : Items(Icons.Filled.Camera, "Capture Image", 60.dp)
    object Flash : Items(Icons.Filled.FlashOff, "Toggle Flash")
    object Gallery : Items(Icons.Filled.Image, "Open Gallery")
}
