package com.stellaridea.swiftvision.ui.views.edition

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.camera.usecase.DetectMaskTap
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.models.masks.Predict
import com.stellaridea.swiftvision.models.projects.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EditionViewModel @Inject constructor(
    private val retrofitService: RetrofitService
) : ViewModel() {

    private val _selectedProject = MutableLiveData<Project?>()
    val selectedProject: LiveData<Project?> = _selectedProject

    private val _selectedImage = MutableLiveData<Image?>()
    val selectedImage: LiveData<Image?> = _selectedImage

    private val _predict = MutableLiveData(Predict(emptyList(), emptyList()))
    val predict: LiveData<Predict> = _predict

    private val _modoPredict = MutableLiveData(false)
    val modoPredict: LiveData<Boolean> = _modoPredict

    private val _newMask = MutableLiveData(false)
    val newMask: LiveData<Boolean> = _newMask

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isImageLoading = MutableLiveData(false)
    val isImageLoading: LiveData<Boolean> = _isImageLoading

    private val _isMaskLoading = MutableLiveData(false)
    val isMaskLoading: LiveData<Boolean> = _isMaskLoading

    // Inicializar el proyecto con sus imágenes y máscaras
    fun initializeProject(projectId: Int, projectName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val imagesResponse = retrofitService.getImagesByProjectId(projectId)
                if (imagesResponse.isSuccessful) {
                    val imageResponses = imagesResponse.body() ?: emptyList()
                    val images = imageResponses.mapNotNull { imageResponse ->
                        downloadImage(imageResponse.id)?.also { image ->
                            image.masks = fetchMasks(image.id)
                        }
                    }
                    val project = Project(id = projectId, name = projectName, images = images)
                    _selectedProject.value = project
                }
            } catch (e: Exception) {
                Log.e("EditionViewModel", "Error al inicializar proyecto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Descargar imagen por ID
    private suspend fun downloadImage(imageId: Int): Image? {
        _isImageLoading.value = true
        return try {
            val response = retrofitService.downloadImage(imageId)
            if (response.isSuccessful) {
                val bitmap = BitmapFactory.decodeStream(response.body()?.byteStream())
                bitmap?.let { Image(id = imageId, bitmap = it) }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("EditionViewModel", "Error al descargar imagen: ${e.message}")
            null
        } finally {
            _isImageLoading.value = false
        }
    }

    // Recuperar máscaras por ID de imagen
    private suspend fun fetchMasks(imageId: Int): List<Mask> {
        _isMaskLoading.value = true
        return try {
            val response = retrofitService.getMasksByImageId(imageId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("EditionViewModel", "Error al recuperar máscaras: ${e.message}")
            emptyList()
        } finally {
            _isMaskLoading.value = false
        }
    }

    // Seleccionar proyecto
    fun selectProject(project: Project) {
        _selectedProject.value = project
        clearSelections()
    }

    // Seleccionar imagen dentro del proyecto
    fun selectImage(image: Image) {
        _selectedImage.value = image
        clearSelections()
    }

    // Limpiar selecciones de predicciones o máscaras
    private fun clearSelections() {
        _predict.value = Predict(emptyList(), emptyList())
    }

    // Alternar modo de predicción
    fun toggleModoPredict() {
        _modoPredict.value = !_modoPredict.value!!
        if (!_modoPredict.value!!) {
            clearSelections()
        }
    }

    // Detección de máscaras en una posición
    fun detectMaskTap(tapPosition: Offset, maskSize: IntSize) {
        _selectedImage.value?.let { image ->
            DetectMaskTap(tapPosition, image.masks, maskSize) { mask ->
                toggleMaskSelection(mask)
            }
        }
    }

    // Alternar la selección de una máscara
    private fun toggleMaskSelection(mask: Mask) {
        _selectedImage.value?.let { image ->
            val updatedMasks = image.masks.map {
                if (it.id == mask.id) it.copy(active = !it.active)
                else it
            }
            val updatedImage = image.copy(masks = updatedMasks)
            _selectedImage.value = updatedImage
        }
    }

    // Guardar imagen con las máscaras seleccionadas en la galería
    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveImageToGallery(context: Context, action: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = _selectedImage.value
                if (image != null) {
                    val savedUri = saveBitmapToGallery(context, image.bitmap)
                    if (savedUri != null) {
                        Log.d("EditionViewModel", "Imagen guardada en la galería: $savedUri")
                    } else {
                        Log.e("EditionViewModel", "Error al guardar la imagen")
                    }
                }
                action()
            } catch (e: Exception) {
                Log.e("EditionViewModel", "Error al guardar la imagen: ${e.message}")
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
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
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
            Log.e("EditionViewModel", "Error al guardar en la galería: ${e.message}")
            null
        }
    }

    // Enviar puntos y etiquetas para predecir máscaras
    fun masksPoints() {
        viewModelScope.launch {
            _newMask.value = true
            _modoPredict.value = false
            val points = _predict.value?.points ?: emptyList()
            val labels = _predict.value?.labels ?: emptyList()

            try {
                val pointsBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    points.toString()
                )
                val labelsBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    labels.toString()
                )
                val response = retrofitService.maskPoints(pointsBody, labelsBody)
                val currentImage = _selectedImage.value
                if (currentImage != null) {
                    val updatedMasks = currentImage.masks + response
                    val updatedImage = currentImage.copy(masks = updatedMasks)
                    _selectedImage.value = updatedImage
                }
            } catch (e: Exception) {
                Log.e("EditionViewModel", "Error al procesar las máscaras: ${e.message}")
            } finally {
                _newMask.value = false
            }
        }
    }

    // Deshacer el último punto en modo de predicción
    fun undoLastPoint() {
        val currentPoints = _predict.value?.points?.toMutableList()
        val currentLabels = _predict.value?.labels?.toMutableList()

        if (!currentPoints.isNullOrEmpty() && !currentLabels.isNullOrEmpty()) {
            currentPoints.removeAt(currentPoints.size - 1)
            currentLabels.removeAt(currentLabels.size - 1)

            _predict.value = Predict(
                points = currentPoints,
                labels = currentLabels
            )
        }
    }
}

