package com.stellaridea.swiftvision.ui.views.project

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.data.user.UserPreferences
import com.stellaridea.swiftvision.models.images.Image
import com.stellaridea.swiftvision.models.projects.Project
import com.stellaridea.swiftvision.models.projects.ProjectUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val retrofitService: RetrofitService,
    public val userPreferences: UserPreferences
) : ViewModel() {

    private val _projects = MutableLiveData<List<Project>>()
    val projects: LiveData<List<Project>> get() = _projects

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isImageLoading = MutableLiveData(false)
    val isImageLoading: LiveData<Boolean> get() = _isImageLoading

    private val userId: Int?
        get() = userPreferences.getUserId()

    fun getProjects(onFailure: () -> Unit = {}) {
        val userId = userId ?: return onFailure()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = retrofitService.getProjects(userId)
                if (response.isSuccessful) {
                    val projects = response.body() ?: emptyList()
                    _projects.value = projects
                    fetchImagesForProjects(projects)
                } else {
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al obtener proyectos: ${e.localizedMessage}")
                onFailure()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchImagesForProjects(projects: List<Project>) {
        _isImageLoading.value = true
        viewModelScope.launch {
            try {
                val updatedProjects = projects.map { project ->
                    val imagesResponse = retrofitService.getImagesByProjectId(project.id)
                    if (imagesResponse.isSuccessful) {
                        val imageResponses = imagesResponse.body() ?: emptyList()
                        val images = imageResponses.mapNotNull { imageResponse ->
                            downloadImage(imageResponse.id)
                        }
                        project.copy(images = images)
                    } else {
                        project
                    }
                }
                _projects.value = updatedProjects
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al descargar imágenes: ${e.localizedMessage}")
            } finally {
                _isImageLoading.value = false
            }
        }
    }

    private suspend fun downloadImage(imageId: Int): Image? {
        return try {
            val response = retrofitService.downloadImage(imageId)
            if (response.isSuccessful) {
                val bitmap = BitmapFactory.decodeStream(response.body()?.byteStream())
                bitmap?.let { Image(id = imageId, bitmap = it) }
            } else {
                Log.e("ProjectViewModel", "Error al descargar imagen, código: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ProjectViewModel", "Error al descargar imagen: ${e.localizedMessage}")
            null
        }
    }

    fun updateProject(projectId: Int, name: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val projectUpdate = ProjectUpdate(name = name)
                val response = retrofitService.updateProject(projectId, projectUpdate)
                if (response.isSuccessful) {
                    getProjects()
                    onSuccess()
                } else {
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al actualizar proyecto: ${e.localizedMessage}")
                onFailure()
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun deleteProject(projectId: Int, onSuccess: () -> Unit, onFailure: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = retrofitService.deleteProject(projectId)
                if (response.isSuccessful) {
                    getProjects() // Refresca la lista
                    onSuccess()
                } else {
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al eliminar proyecto: ${e.localizedMessage}")
                onFailure()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
