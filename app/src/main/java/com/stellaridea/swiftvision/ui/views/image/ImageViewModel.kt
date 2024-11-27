package com.stellaridea.swiftvision.ui.views.image

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.image.ImageService
import com.stellaridea.swiftvision.data.image.model.ImageModel
import com.stellaridea.swiftvision.data.mask.model.Mask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val imageService: ImageService
) : ViewModel() {

    private val _images = MutableLiveData<List<ImageModel>>()
    val images: LiveData<List<ImageModel>> get() = _images

    private val _masks = MutableLiveData<List<Mask>>()
    val masks: LiveData<List<Mask>> get() = _masks

    fun fetchImages(projectId: Int, onFailure: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = imageService.getImages(projectId)
                if (response.isSuccessful) {
                    _images.value = response.body() ?: emptyList()
                } else {
                    Log.e("ImageViewModel", "Error al obtener imágenes: ${response.message()}")
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("ImageViewModel", "Excepción al obtener imágenes: ${e.localizedMessage}", e)
                onFailure()
            }
        }
    }

    fun fetchMasks(imageId: Int, onFailure: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = imageService.getMasks(imageId)
                if (response.isSuccessful) {
                    _masks.value = emptyList()
                } else {
                    Log.e("ImageViewModel", "Error al obtener máscaras: ${response.message()}")
                    onFailure()
                }
            } catch (e: Exception) {
                Log.e("ImageViewModel", "Excepción al obtener máscaras: ${e.localizedMessage}", e)
                onFailure()
            }
        }
    }
}
