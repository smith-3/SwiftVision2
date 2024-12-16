package com.stellaridea.swiftvision.ui.views.edition.viewmodels

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.models.masks.Mask
import com.stellaridea.swiftvision.ui.views.edition.DetectMaskTap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
}
