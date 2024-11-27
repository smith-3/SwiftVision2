package com.stellaridea.swiftvision.camera.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.stellaridea.swiftvision.data.image.model.ImageModel
import java.util.Date

fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    flashMode: Int,
    onPhotoTaken: (ImageModel) -> Unit
) {
    imageCapture.flashMode = flashMode
    Log.i("Image", "Captura solicitada")
    try {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    Log.i("Image", "Captura exitosa")
                    try {
                        val matrix = Matrix().apply {
                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                        }
                        val bitmap = image.toBitmap()
                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.width,
                            bitmap.height,
                            matrix,
                            true
                        )

                        // Crear una instancia de ImageModel con los nuevos parámetros
                        val imageModel = ImageModel(
                            id = 0, // ID temporal
                            project_id = 0, // ID de proyecto predeterminado
                            bitmap = rotatedBitmap,
                            created_at = Date().time, // Fecha de creación actual
                            masks = emptyList() // Lista vacía de máscaras por defecto
                        )
                        onPhotoTaken(imageModel)
                    } catch (e: Exception) {
                        Log.e("Image", "Error al procesar la imagen: ${e.message}")
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Image", "Error en la captura de imagen: ${exception.message}")
                }
            }
        )
    } catch (e: Exception) {
        Log.e("Image", "Excepción durante la captura de imagen: ${e.message}")
    }
}
