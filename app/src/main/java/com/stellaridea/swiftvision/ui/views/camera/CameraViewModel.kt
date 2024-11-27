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
import com.stellaridea.swiftvision.camera.usecase.DetectMaskTap
import com.stellaridea.swiftvision.camera.usecase.getPixelPositionFromByteArray
import com.stellaridea.swiftvision.camera.usecase.takePicture
import com.stellaridea.swiftvision.data.image.model.ImageModel
import com.stellaridea.swiftvision.data.image.model.Predict
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.data.sam.RetrofitService
import com.stellaridea.swiftvision.data.sam.model.MasksResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException
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

    private val _predict = MutableLiveData(Predict(emptyList(), emptyList()))
    val predict: LiveData<Predict> = _predict
    private val _modoPredict = MutableLiveData(false)
    val modoPredict: LiveData<Boolean> = _modoPredict

    private val _newMask = MutableLiveData(false)
    val newMask: LiveData<Boolean> = _newMask

    private val _newImage = MutableLiveData(false)
    val newImage: LiveData<Boolean> = _newImage

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
        onPhotoTaken: (ImageModel) -> Unit
    ) {
        viewModelScope.launch {
            takePicture(
                context,
                captureImage,
                flashMode.value ?: ImageCapture.FLASH_MODE_OFF
            ) { imageModel ->
                _lastImage.value = imageModel
                processImage(_lastImage.value!!.bitmap)
                onPhotoTaken(imageModel)
            }
        }
    }

    fun toggleCamera() {
        _cameraSelector.value = !_cameraSelector.value!!
    }

    fun capturePicture(
        context: Context,
        imageUri: Uri,
        onPhotoTaken: (ImageModel?) -> Unit
    ) {
        viewModelScope.launch {
            val bitmap = loadBitmapFromUri(context, imageUri)
            val imageModel = bitmap?.let {
                ImageModel(
                    id = Date().time.toInt(), // ID temporal basado en timestamp
                    project_id = 0,
                    bitmap = it,
                    created_at = Date().time,
                    masks = emptyList()
                )
            }

            if (imageModel != null) {
                _lastImage.value = imageModel
                processImage(imageModel.bitmap)
            }

            onPhotoTaken(imageModel)
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



    private fun loadByteArrayFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val maxRetries = 3
            var currentAttempt = 0
            var success = false

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            while (currentAttempt < maxRetries && !success) {
                try {
                    val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), byteArray)
                    val body = MultipartBody.Part.createFormData("file", "image.png", requestFile)

                    Log.i(
                        "SAM",
                        "Enviando solicitud para cargar la imagen... (Intento ${currentAttempt + 1})"
                    )

                    val masksResponse: MasksResponse = retrofitService.uploadImage(body)
                    val updatedImageModel = _lastImage.value?.copy(masks = masksResponse.masks)
                    _lastImage.value = updatedImageModel

                    Log.i("SAM", "Máscaras recibidas")
                    success = true

                } catch (e: HttpException) {
                    Log.e("SAM", "Error HTTP: ${e.code()} - ${e.message()}")
                    break

                } catch (e: IOException) {
                    Log.e("SAM", "Error de IO: ${e.message}")
                    currentAttempt++
                    if (currentAttempt < maxRetries) {
                        delay(2000L)
                    } else {
                        Log.e("SAM", "Falló después de $maxRetries intentos.")
                    }

                } catch (e: Exception) {
                    Log.e("SAM", "Error al procesar la imagen: ${e.message}")
                    break
                }
            }
        }
    }

    fun detectMaskTap(tapPosition: Offset, maskSize: IntSize) {
        _lastImage.value?.let { image ->
            Log.d("selectMask", "Detecting mask for tap position: $tapPosition")
            var mask: Mask = image.masks[0]
            DetectMaskTap(tapPosition, image.masks, maskSize) { mask = it }
            run {
                Log.d("selectMask", "Mask detected: $mask")
                toggleMaskSelection(mask)
            }
        }
    }

    fun detectPointsNewMask(
        tapPosition: Offset,
        size: IntSize,
        select: Boolean = true
    ) {
        Log.i("detectPointsNewMask", "Tap Position: $tapPosition")
        val point = lastImage.value?.let { lastImg ->
            val result = getPixelPositionFromByteArray(
                lastImg.bitmap,
                tapPosition,
                size
            )
            Log.i("detectPointsNewMask", "Pixel Position: $result")
            result
        }

        point?.let {
            Log.i("detectPointsNewMask", "Detected Point: $it")

            val currentPoints = predict.value?.points?.toMutableList() ?: mutableListOf()
            val currentLabels = predict.value?.labels?.toMutableList() ?: mutableListOf()

            Log.i("detectPointsNewMask", "Current Points before addition: $currentPoints")
            Log.i("detectPointsNewMask", "Current Labels before addition: $currentLabels")

            currentPoints.add(it)
            currentLabels.add(if (select) 1 else 0)

            Log.i("detectPointsNewMask", "Updated Points: $currentPoints")
            Log.i("detectPointsNewMask", "Updated Labels: $currentLabels")

            _predict.value = Predict(
                points = currentPoints,
                labels = currentLabels
            )

            Log.i("detectPointsNewMask", "Predict updated successfully")
        } ?: Log.e("detectPointsNewMask", "No point detected from the tap position")
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

    fun hairColorMasks(color: Color, action: () -> Unit) {
        viewModelScope.launch {
            _newImage.value = true

            _lastImage.value?.let { imageModel ->
                if (imageModel.masks.isEmpty()) {
                    Log.e("hairColorMasks", "Masks are null or empty")
                    _newImage.value = false
                    action()
                    return@launch
                }
                val activeMasks = imageModel.masks.filter { it.active }
                if (activeMasks.isNotEmpty()) {
                    val masksJsonArray =
                        activeMasks.map { mask -> imageModel.masks.indexOf(mask) }
                    val bitmap = imageModel.bitmap
                    run {
                        val maskJsonPart = RequestBody.create(
                            "application/json".toMediaTypeOrNull(),
                            masksJsonArray.toString()
                        )
                        val rgbColor =
                            "${(color.red * 255).toInt()},${(color.green * 255).toInt()},${(color.blue * 255).toInt()}"
                        val rgbPart = RequestBody.create("text/plain".toMediaTypeOrNull(), rgbColor)
                        var attempt = 0
                        var success = false
                        while (attempt < 3 && !success) {
                            try {
                                val responseBody = retrofitService.colorizeImage(maskJsonPart, rgbPart)
                                responseBody.byteStream().use { inputStream ->
                                    val newBitmap = BitmapFactory.decodeStream(inputStream)
                                    if (newBitmap != null) {
                                        val updatedImageModel = imageModel.copy(
                                            bitmap = newBitmap
                                        )
                                        updatedImageModel.masks.forEach { mask ->
                                            if (mask.active) {
                                                mask.active = false
                                            }
                                        }
                                        _lastImage.value = updatedImageModel
                                        success = true
                                    } else {
                                        Log.e("hairColorMasks", "Failed to decode stream to Bitmap")
                                    }
                                }
                            } catch (e: Exception) {
                                attempt++
                                Log.e("hairColorMasks", "Attempt $attempt failed: ${e.message}")
                                if (attempt >= 3) {
                                    Log.e("hairColorMasks", "All attempts failed.")
                                }
                            }
                        }
                    }
                } else {
                    Log.e("hairColorMasks", "No active masks found")
                }
            } ?: run {
                Log.e("hairColorMasks", "_lastImage.value is null or ImageModel is null")
            }
            action()
            _newImage.value = false
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveImageToGallery(context: Context, action: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageData = lastImage.value?.bitmap
                val savedUri = imageData?.let { saveBitmapToGallery(context, it) }
                if (savedUri != null) {
                    Log.d("MyViewModel", "Imagen guardada en la galería: $savedUri")
                } else {
                    Log.e("MyViewModel", "Error al guardar la imagen")
                }
                action()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
        val contentResolver = context.contentResolver
        val imageCollection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/MyApp"
            ) // Carpeta dentro de la galería
        }
        return try {
            val imageUri = contentResolver.insert(imageCollection, contentValues)

            imageUri?.let { uri ->
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun toogleModoPredict() {
        _modoPredict.value = !_modoPredict.value!!
        if (!_modoPredict.value!!){
            _predict.value = Predict(emptyList(), emptyList())
        }
    }

    fun undoLastPoint() {
        val currentPoints = _predict.value?.points?.toMutableList()
        val currentLabels = _predict.value?.labels?.toMutableList()

        if (!currentPoints.isNullOrEmpty() && !currentLabels.isNullOrEmpty()) {
            // Remover el último punto y label
            currentPoints.removeAt(currentPoints.size - 1)
            currentLabels.removeAt(currentLabels.size - 1)

            // Actualizar el LiveData _predict con las listas modificadas
            _predict.value = Predict(
                points = currentPoints,
                labels = currentLabels
            )
            Log.i("undoLastPoint", "Último punto y label eliminados")
        } else {
            Log.i("undoLastPoint", "No hay puntos o labels para eliminar")
        }
    }

    fun masksPoints(){
        viewModelScope.launch {
            _newMask.value = true
            _modoPredict.value = false
            val maxRetries = 3
            var currentAttempt = 0
            var success = false
            while (currentAttempt < maxRetries && !success) {
                try {
                    Log.i(
                        "SAM",
                        "Enviando solicitud para cargar la imagen... (Intento ${currentAttempt + 1})"
                    )
                    val points = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        predict.value?.points.toString()
                    )
                    val labels = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        predict.value?.labels.toString()
                    )
                    val masksResponse: MasksResponse = retrofitService.maskPoints(points, labels)
                    val currentMasks = _lastImage.value?.masks ?: listOf()
                    val updatedMasks = currentMasks + masksResponse.masks
                    val updatedImageModel = _lastImage.value?.copy(masks = updatedMasks)
                    _lastImage.value = updatedImageModel
                    Log.i("SAM", "Máscaras recibidas")
                    _predict.value = Predict(emptyList(), emptyList())
                    success = true
                } catch (e: HttpException) {
                    Log.e("SAM", "Error HTTP: ${e.code()} - ${e.message()}")
                    break
                } catch (e: IOException) {
                    Log.e("SAM", "Error de IO: ${e.message}")
                    currentAttempt++
                    if (currentAttempt < maxRetries) {
                        delay(2000L)
                    } else {
                        Log.e("SAM", "Falló después de $maxRetries intentos.")
                    }
                } catch (e: Exception) {
                    Log.e("SAM", "Error al procesar la imagen: ${e.message}")
                    break
                }
            }
            _newMask.value = false
        }
    }
}
