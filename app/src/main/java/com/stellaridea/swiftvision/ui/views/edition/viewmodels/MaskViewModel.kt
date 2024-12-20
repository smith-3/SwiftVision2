package com.stellaridea.swiftvision.ui.views.edition.viewmodels

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.graphics.Color
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
import com.google.gson.Gson
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.data.utils.BitmapCompressor
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.ui.views.edition.DetectMaskTap
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
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

    fun saveMask(bitmap: Bitmap, size: IntArray, imageId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Comprimir el bitmap y obtener el JSON para "counts"
                val countsJson = BitmapCompressor.compressBitmapSelectionToJson(bitmap, Color.BLUE)
                Log.d("saveMask", "Counts JSON: $countsJson")

// Crear los datos necesarios para enviar al backend
                val countsBody = countsJson.toRequestBody("application/json".toMediaTypeOrNull())
                val sizeBody = Gson().toJson(size).toRequestBody("application/json".toMediaTypeOrNull())
                val bboxBody = Gson().toJson(listOf(0, 0, size[0], size[1])).toRequestBody("application/json".toMediaTypeOrNull())
                val pointCoordsBody = Gson().toJson(listOf(listOf(0, 0))).toRequestBody("application/json".toMediaTypeOrNull())

// Usar un Buffer para obtener el contenido del RequestBody como String
                val countsJsonString = countsBodyToString(countsBody)
                val sizeJsonString = countsBodyToString(sizeBody)
                val bboxJsonString = countsBodyToString(bboxBody)
                val pointCoordsJsonString = countsBodyToString(pointCoordsBody)

// Loggear los datos
                Log.d("saveMask", "Counts JSON: $countsJsonString")
                Log.d("saveMask", "Size JSON: $sizeJsonString")
                Log.d("saveMask", "BBox JSON: $bboxJsonString")
                Log.d("saveMask", "PointCoords JSON: $pointCoordsJsonString")
                // Enviar la máscara al backend para crearla en la base de datos
                val response = retrofitService.createMask(
                    imageId = imageId, // Pasar el imageId correcto
                    counts = countsBody,
                    size = sizeBody,
                    bbox = bboxBody,
                    pointCoords = pointCoordsBody
                )

                if (response.isSuccessful) {
                    // La respuesta debe contener la máscara recién creada, con el id generado en la base de datos
                    val createdMask = response.body()

                    if (createdMask != null) {
                        Log.d("saveMask", "createdMask: ${createdMask.size}")

                        // Actualizar la lista local de máscaras
                        val updatedMasks = (_masks.value.orEmpty().toMutableList()).apply {
                            add(0, createdMask) // Insertar al inicio
                        }
                        _masks.postValue(updatedMasks)

                        Log.d("MaskViewModel", "Mask successfully saved to backend and local list.")
                        onComplete(true)
                    } else {
                        Log.e("MaskViewModel", "Error: Mask response body is null.")
                        onComplete(false)
                    }
                } else {
                    Log.e("MaskViewModel", "Error saving mask to backend: ${response.errorBody()?.string()}")
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("MaskViewModel", "Exception saving mask: ${e.message}")
                onComplete(false)
            }
        }
    }



    fun getActiveMasks(): List<Mask> {
        return _masks.value?.filter { it.active } ?: emptyList()
    }
    fun countsBodyToString(requestBody: RequestBody): String {
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }
}
