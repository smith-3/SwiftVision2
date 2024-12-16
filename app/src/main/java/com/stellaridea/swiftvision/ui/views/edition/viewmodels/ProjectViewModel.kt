package com.stellaridea.swiftvision.ui.views.edition.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.models.projects.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val retrofitService: RetrofitService
) : ViewModel() {

    private val _selectedProject = MutableLiveData<Project?>()
    val selectedProject: LiveData<Project?> = _selectedProject

    private val _selectedImage = MutableLiveData<Image?>()
    val selectedImage: LiveData<Image?> = _selectedImage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Inicializa el proyecto y selecciona automáticamente la imagen más reciente.
     */
    fun initializeProject(projectId: Int, projectName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val imagesResponse = retrofitService.getImagesByProjectId(projectId)
                if (imagesResponse.isSuccessful) {
                    val imageIds = imagesResponse.body() ?: emptyList()

                    val images = imageIds.map {
                        Log.i("Image Id:", "${it.id}")
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
                    images.firstOrNull()?.let { selectAndDownloadImage(it.id) }
                } else {
                    Log.e("ProjectViewModel", "Error loading images: ${imagesResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error initializing project: ${e.message}")
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
                    Log.e("ProjectViewModel", "Error downloading image: ${response.errorBody()?.string()}")
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
}
