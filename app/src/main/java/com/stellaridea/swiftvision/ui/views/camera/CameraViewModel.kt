package com.stellaridea.swiftvision.ui.views.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.camera.usecase.getPixelPositionFromByteArray
import com.stellaridea.swiftvision.camera.usecase.takePicture
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.models.images.ImageModel
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.models.masks.Predict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class CameraViewModel @Inject constructor(
    private val retrofitService: RetrofitService
) : ViewModel() {
    private val _flashMode = MutableLiveData(ImageCapture.FLASH_MODE_OFF)
    private val flashMode: LiveData<Int> = _flashMode
    private val _cameraSelector = MutableLiveData(true)
    val cameraSelector: LiveData<Boolean> = _cameraSelector
    var flashIcon by mutableStateOf(Icons.Filled.FlashOff)
    private val _lastImage = MutableLiveData<ImageModel?>()
    val lastImage: LiveData<ImageModel?> = _lastImage

    fun toggleFlashMode() {
        _flashMode.value = when (flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> {
                flashIcon = Icons.Filled.FlashOn
                ImageCapture.FLASH_MODE_ON
            }

            ImageCapture.FLASH_MODE_ON -> {
                flashIcon = Icons.Filled.FlashAuto
                ImageCapture.FLASH_MODE_AUTO
            }

            ImageCapture.FLASH_MODE_AUTO -> {
                flashIcon = Icons.Filled.FlashOff
                ImageCapture.FLASH_MODE_OFF
            }

            else -> ImageCapture.FLASH_MODE_OFF
        }
    }

    fun capturePicture(
        context: Context,
        captureImage: ImageCapture,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        viewModelScope.launch {
            takePicture(
                context,
                captureImage,
                flashMode.value ?: ImageCapture.FLASH_MODE_OFF
            ) { bitmap ->
                onPhotoTaken(bitmap)
            }
        }
    }

    fun toggleCamera() {
        _cameraSelector.value = !_cameraSelector.value!!
    }

    fun capturePicture(
        context: Context,
        imageUri: Uri,
        onPhotoTaken: (bitmap: Bitmap) -> Unit
    ) {
        viewModelScope.launch {
            val bitmap = loadBitmapFromUri(context, imageUri)
            if (bitmap != null) {
                onPhotoTaken(bitmap)
            }
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // Utiliza ContentResolver para abrir InputStream y decodificarlo en un Bitmap
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream) // Convierte el InputStream a Bitmap
            }
        } catch (e: Exception) {
            Log.e("SAM", "Error al cargar Bitmap desde Uri: ${e.message}")
            null
        }
    }

    fun toggleMaskSelection(mask: Mask) {
        _lastImage.value?.let { imageModel ->
            val updatedMasks = imageModel.masks.map {
                if (it.id == mask.id) it.copy(active = !it.active)
                else it
            }
            val updatedImageModel = imageModel.copy(masks = updatedMasks)
            _lastImage.value = updatedImageModel
        }
    }

    fun createProject(
        context: Context,
        imageName: String,
        bitmap: Bitmap,
        onSuccess: (id: Int) -> Unit,
        onFailure: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = RequestBody.create("text/plain".toMediaTypeOrNull(), "1") // Ajusta el ID real
                val name = RequestBody.create("text/plain".toMediaTypeOrNull(), imageName)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val requestBody = byteArrayOutputStream.toByteArray()
                val imagePart = MultipartBody.Part.createFormData(
                    "file", "image.jpg",
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), requestBody)
                )

                val response = retrofitService.createProject(userId, name, imagePart)
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it.id) }
                } else {
                    Log.e("CameraViewModel", "Error al crear proyecto: ${response.errorBody()?.string()}")
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error al crear proyecto: ${e.message}")
                onFailure()
            }
        }
    }
}
