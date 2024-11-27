package com.stellaridea.swiftvision.ui.views.project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.project.ProjectService
import com.stellaridea.swiftvision.models.projects.ProjectRequest
import com.stellaridea.swiftvision.models.projects.ProjectResponse
import com.stellaridea.swiftvision.models.projects.ProjectUpdateRequest
import com.stellaridea.swiftvision.data.user.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectService: ProjectService,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _projects = MutableLiveData<List<ProjectResponse>>()
    val projects: LiveData<List<ProjectResponse>> get() = _projects

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val userId: Int?
        get() = userPreferences.getUserId() // Obtiene el userId desde las preferencias

    fun createProject(name: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val userId = userId ?: return onFailure() // Verifica si userId es nulo
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = projectService.createProject(ProjectRequest(name, userId))
                if (response.isSuccessful) {
                    getProjects() // Actualiza la lista después de crear el proyecto
                    onSuccess()
                } else {
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("ProjectViewModel", "Error al crear proyecto: ${e.localizedMessage}")
                onFailure()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProjects(onFailure: () -> Unit = {}) {
        val userId = userId ?: return onFailure() // Verifica si userId es nulo
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = projectService.getProjects(userId)
                if (response.isSuccessful) {
                    _projects.value = response.body() ?: emptyList()
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

    fun updateProject(projectId: Int, name: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = projectService.updateProject(projectId, ProjectUpdateRequest(name))
                if (response.isSuccessful) {
                    getProjects() // Refresca la lista
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
                val response = projectService.deleteProject(projectId)
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
