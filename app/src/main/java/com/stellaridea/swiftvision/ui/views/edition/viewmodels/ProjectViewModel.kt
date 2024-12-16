package com.stellaridea.swiftvision.ui.views.edition.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.data.utils.BitmapCompressor
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.models.projects.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val retrofitService: RetrofitService
) : ViewModel() {

    private val _selectedProject = MutableLiveData<Project?>()
    private val _selectedImage = MutableLiveData<Image?>()
    val selectedImage: LiveData<Image?> = _selectedImage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Inicializa el proyecto y selecciona automáticamente la imagen más reciente.
     */
    fun initializeProject(projectId: Int, projectName: String, onComplete: (Int?) -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val imagesResponse = retrofitService.getImagesByProjectId(projectId)
                if (imagesResponse.isSuccessful) {
                    val imageIds = imagesResponse.body() ?: emptyList()

                    Log.i("ImageResponse", "Received ${imageIds.size} images from backend")

                    val images = imageIds.map {
                        Image(
                            id = it.id,
                            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                            masks = emptyList()
                        )
                    }.sortedByDescending { it.id }

                    _selectedProject.value = Project(
                        id = projectId,
                        name = projectName,
                        images = images
                    )

                    // Selecciona y carga la primera imagen
                    images.firstOrNull()?.let {
                        _selectedImage.value = it
                        selectAndDownloadImage(it.id)
                        onComplete(it.id)
                    }
                } else {
                    Log.e("ProjectViewModel", "Error loading images: ${imagesResponse.errorBody()?.string()}")
                    onComplete(null)
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error initializing project: ${e.message}")
                onComplete(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Procesa inpainting con la máscara seleccionada.
     */
    fun processInpainting(maskId: Int, onComplete: (Int?) -> Unit) {
        val project = _selectedProject.value
        val image = _selectedImage.value
        if (project == null || image == null) {
            onComplete(null)
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = retrofitService.processInpainting(project.id, image.id, maskId)
                if (response.isSuccessful) {
                    Log.i("ProjectViewModel", "Inpainting process successful")
                    // Reinicia el proyecto y selecciona la nueva imagen.
                    initializeProject(project.id, project.name, onComplete)
                } else {
                    Log.e("ProjectViewModel", "Error processing inpainting: ${response.errorBody()?.string()}")
                    onComplete(null)
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error processing inpainting: ${e.message}")
                onComplete(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Genera una nueva imagen basada en el prompt proporcionado.
     */
    fun generateImage(maskId: Int, prompt: String, onComplete: (Int?) -> Unit) {
        val project = _selectedProject.value
        val image = _selectedImage.value
        if (project == null || image == null) {
            onComplete(null)
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = retrofitService.generateImage(project.id, image.id, maskId, prompt)
                if (response.isSuccessful) {
                    Log.i("ProjectViewModel", "Image generation successful")
                    // Reinicia el proyecto y selecciona la nueva imagen.
                    initializeProject(project.id, project.name, onComplete)
                } else {
                    Log.e("ProjectViewModel", "Error generating image: ${response.errorBody()?.string()}")
                    onComplete(null)
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error generating image: ${e.message}")
                onComplete(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Genera un nuevo fondo para la imagen seleccionada.
     */
    fun generateImageBackground(maskId: Int, prompt: String, onComplete: (Int?) -> Unit) {
        val project = _selectedProject.value
        val image = _selectedImage.value
        if (project == null || image == null) {
            onComplete(null)
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = retrofitService.generateImageBackground(project.id, image.id, maskId, prompt)
                if (response.isSuccessful) {
                    Log.i("ProjectViewModel", "Background generation successful")
                    // Reinicia el proyecto y selecciona la nueva imagen.
                    initializeProject(project.id, project.name, onComplete)
                } else {
                    Log.e("ProjectViewModel", "Error generating background: ${response.errorBody()?.string()}")
                    onComplete(null)
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error generating background: ${e.message}")
                onComplete(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Selecciona y descarga la imagen correspondiente al ID.
     */
    fun selectAndDownloadImage(imageId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = retrofitService.downloadImage(imageId)
                if (response.isSuccessful) {
                    val bitmap = BitmapFactory.decodeStream(response.body()?.byteStream())
                    val project = _selectedProject.value ?: return@launch

                    // Actualiza la lista de imágenes con el bitmap descargado
                    val updatedImages = project.images.map {
                        if (it.id == imageId) it.copy(bitmap = bitmap) else it
                    }

                    _selectedProject.value = project.copy(images = updatedImages)
                    _selectedImage.value = updatedImages.find { it.id == imageId }
                } else {
                    Log.e(
                        "ProjectViewModel",
                        "Error downloading image: ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error downloading image: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun canGoPrevious(): Boolean {
        val project = _selectedProject.value ?: return false
        val currentIndex = project.images.indexOfFirst { it.id == _selectedImage.value?.id }
        return currentIndex > 0
    }

    fun canGoNext(): Boolean {
        val project = _selectedProject.value ?: return false
        val currentIndex = project.images.indexOfFirst { it.id == _selectedImage.value?.id }
        return currentIndex < project.images.lastIndex
    }

    fun selectPreviousImage() {
        val project = _selectedProject.value ?: return
        val currentIndex = project.images.indexOfFirst { it.id == _selectedImage.value?.id }
        if (currentIndex > 0) {
            val previousImage = project.images[currentIndex - 1]
            selectAndDownloadImage(previousImage.id)
        }
    }

    fun selectNextImage() {
        val project = _selectedProject.value ?: return
        val currentIndex = project.images.indexOfFirst { it.id == _selectedImage.value?.id }
        if (currentIndex < project.images.lastIndex) {
            val nextImage = project.images[currentIndex + 1]
            selectAndDownloadImage(nextImage.id)
        }
    }

    fun uploadMaskSelection(
        projectId: Int,
        imageId: Int,
        bitmap: Bitmap,
        selectionColor: Int = Color.BLUE,
        onResult: (Boolean) -> Unit
    ) {
        // Muestra que estamos cargando algo
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 1. Comprimir la selección del bitmap a JSON
                val countsJson = BitmapCompressor.compressBitmapSelectionToJson(
                    bitmap = bitmap,
                    selectionColor = selectionColor
                )

                // 2. Preparar RequestBody para cada campo
                val projectIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), projectId.toString())
                val imageIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), imageId.toString())
                val sizeBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "(${bitmap.width}, ${bitmap.height})")
                val countsBody = RequestBody.create("application/json".toMediaTypeOrNull(), countsJson)

                // 3. Llamar a tu método de Retrofit para subir la máscara
                val response = retrofitService.uploadMask(
                    projectIdBody,
                    imageIdBody,
                    countsBody,
                    sizeBody
                )

                if (response.isSuccessful) {
                    Log.i("ViewModel", "Mask uploaded successfully.")
                    onResult(true)
                } else {
                    Log.e("ViewModel", "Error uploading mask: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Exception uploading mask: ${e.message}")
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

}
