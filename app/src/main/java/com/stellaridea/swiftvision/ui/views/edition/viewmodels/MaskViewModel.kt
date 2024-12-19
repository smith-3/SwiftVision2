package com.stellaridea.swiftvision.ui.views.edition.viewmodels

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

import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.camera.usecase.getPixelPositionFromByteArray
import com.stellaridea.swiftvision.camera.usecase.takePicture

import com.stellaridea.swiftvision.data.user.UserPreferences
import com.stellaridea.swiftvision.models.images.ImageModel

import com.stellaridea.swiftvision.models.masks.Predict

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

import java.io.ByteArrayOutputStream
import java.util.Date



import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.ui.views.edition.DetectMaskTap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MaskViewModel @Inject constructor(
    private val retrofitService: RetrofitService,
) : ViewModel() {

    private val _masks = MutableLiveData<List<Mask>>()
    val masks: LiveData<List<Mask>> = _masks

    private val _isMaskLoading = MutableLiveData(false)
    val isMaskLoading: LiveData<Boolean> = _isMaskLoading

    fun loadMasksForImage(imageId: Int) {
        _isMaskLoading.value = true
        viewModelScope.launch {
            try {
                val response = retrofitService.getMasksByImageId(imageId)
                if (response.isSuccessful) {
                    _masks.value = response.body() ?: emptyList()
                    Log.i("Mask", "${masks.value?.size}")
                } else {
                    Log.e("MaskViewModel", "Failed to fetch masks: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MaskViewModel", "Error fetching masks: ${e.message}")
            } finally {
                _isMaskLoading.value = false
            }
        }
    }

    fun toggleMaskSelection(maskId: Long) {
        val updatedMasks = _masks.value?.map {
            if (it.id == maskId) it.copy(active = !it.active) else it
        } ?: return
        _masks.value = updatedMasks
    }

    fun detectMaskTap(tapPosition: Offset, maskSize: IntSize) {
        val currentMasks = _masks.value ?: return
        Log.d("EditionViewModel", "Detecting mask tap at $tapPosition")

        DetectMaskTap(tapPosition, currentMasks, maskSize) { mask ->
            Log.d("EditionViewModel", "Mask tapped: ${mask.id}")
            toggleMaskSelection(mask.id)
        }
    }

    fun isAnyMaskSelected(): Boolean {
        return _masks.value?.any { it.active } == true
    }

    fun saveMask(bitmap: Bitmap, size: IntArray, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val newMask = Mask(
                    id = System.currentTimeMillis(), // ID único basado en la hora actual
                    bitmap = bitmap,
                    size = size,
                    active = true
                )
                // Insertar la nueva máscara al inicio de la lista
                val updatedMasks = (_masks.value.orEmpty().toMutableList()).apply {
                    add(0, newMask) // Añadir en la posición 0
                }
                _masks.postValue(updatedMasks)
                onComplete(true)
            } catch (e: Exception) {
                Log.e("MaskViewModel", "Error saving mask: ${e.message}")
                onComplete(false)
            }
        }
    }

    fun getActiveMasks(): List<Mask> {
        return _masks.value?.filter { it.active } ?: emptyList()
    }
}
