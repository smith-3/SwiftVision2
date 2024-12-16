package com.stellaridea.swiftvision.ui.views.edition.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stellaridea.swiftvision.data.RetrofitService
import com.stellaridea.swiftvision.models.masks.Predict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class PredictViewModel @Inject constructor(
    private val retrofitService: RetrofitService
) : ViewModel() {

    private val _predict = MutableLiveData(Predict(emptyList(), emptyList()))
    val predict: LiveData<Predict> = _predict

    private val _modoPredict = MutableLiveData(false)
    val modoPredict: LiveData<Boolean> = _modoPredict

    fun toggleModoPredict() {
        _modoPredict.value = !_modoPredict.value!!
        if (_modoPredict.value == false) {
            clearSelections()
        }
    }

    private fun clearSelections() {
        _predict.value = Predict(emptyList(), emptyList())
    }

    fun masksPoints() {
        viewModelScope.launch {
            val points = _predict.value?.points ?: return@launch
            val labels = _predict.value?.labels ?: return@launch

            try {
                val pointsBody = RequestBody.create("application/json".toMediaTypeOrNull(), points.toString())
                val labelsBody = RequestBody.create("application/json".toMediaTypeOrNull(), labels.toString())
                retrofitService.maskPoints(pointsBody, labelsBody)
            } catch (e: Exception) {
                Log.e("PredictViewModel", "Error processing mask points: ${e.message}")
            }
        }
    }
}
