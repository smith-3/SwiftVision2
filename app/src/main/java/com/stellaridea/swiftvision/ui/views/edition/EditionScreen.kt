package com.stellaridea.swiftvision.ui.views.edition

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.views.edition.components.ConfirmNavigationDialog
import com.stellaridea.swiftvision.ui.views.edition.components.EditionDownBar
import com.stellaridea.swiftvision.ui.views.edition.components.EditionTopBar
import com.stellaridea.swiftvision.ui.views.edition.components.ImageViewer
import com.stellaridea.swiftvision.ui.views.edition.components.MasksList
import com.stellaridea.swiftvision.ui.views.edition.components.PromptBackgroundDialog
import com.stellaridea.swiftvision.ui.views.edition.components.PromptDialog
import com.stellaridea.swiftvision.ui.views.edition.components.RemoveMaskDialog
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.ImageSaveViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.MaskViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.PredictViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.ProjectViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button


@SuppressLint("UnrememberedGetBackStackEntry", "UnusedBoxWithConstraintsScope")
@Composable
fun EditionScreen(
    navController: NavHostController,
    projectId: Int,
    projectName: String,
    projectViewModel: ProjectViewModel = hiltViewModel(),
    maskViewModel: MaskViewModel = hiltViewModel(),
    predictViewModel: PredictViewModel = hiltViewModel(),
    imageSaveViewModel: ImageSaveViewModel = hiltViewModel()
) {
    val selectedImage by projectViewModel.selectedImage.observeAsState()
    val isLoading by projectViewModel.isLoading.observeAsState(false)
    val isMaskLoading by maskViewModel.isMaskLoading.observeAsState(false)
    val modoPredict by predictViewModel.modoPredict.observeAsState(false)

    var showDialog by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }
    var showPromptBackgroundDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val canGoPrev = projectViewModel.canGoPrevious()
    val canGoNext = projectViewModel.canGoNext()
    val masks by maskViewModel.masks.observeAsState(emptyList())

    val hasActiveMask by remember(masks) {
        derivedStateOf { masks.any { it.active } }
    }

    LaunchedEffect(Unit) {
        projectViewModel.initializeProject(projectId, projectName) {
            if (it != null) {
                maskViewModel.loadMasksForImage(it)
            }
        }
    }

    BackHandler {
        showDialog = true
    }

    ConfirmNavigationDialog(showDialog, navController) { dialogState ->
        showDialog = dialogState
    }

    // Diálogo para ingresar prompt
    if (showPromptDialog) {
        PromptDialog(
            onDismiss = { showPromptDialog = false },
            onConfirm = { prompt ->
                showPromptDialog = false
                val maskId: Int = maskViewModel.getActiveMasks().get(0).id.toInt()
                projectViewModel.generateImage(maskId, prompt) {
                    if (it != null) {
                        maskViewModel.loadMasksForImage(imageId = it)
                    }                }
            }
        )
    }
    // Diálogo para ingresar prompt
    if (showRemoveDialog) {
        RemoveMaskDialog(
            onDismiss = { showRemoveDialog = false },
            onConfirm = {
                showRemoveDialog = false
                val maskId: Int = maskViewModel.getActiveMasks().get(0).id.toInt()
                projectViewModel.processRemove(maskId) {
                    if (it != null) {
                        maskViewModel.loadMasksForImage(imageId = it)
                    }
                }
            }
        )
    }
    // Diálogo para ingresar prompt
    if (showPromptBackgroundDialog) {
        PromptBackgroundDialog(
            onDismiss = { showPromptBackgroundDialog = false },
            onConfirm = { prompt ->
                showPromptBackgroundDialog = false
                val maskId: Int = maskViewModel.getActiveMasks().get(0).id.toInt()
                projectViewModel.generateImageBackground(maskId, prompt) {
                    if (it != null) {
                        maskViewModel.loadMasksForImage(imageId = it)
                    }
                }
            }
        )
    }


    // Pantalla principal
// Agregar el estado para mostrar el diálogo
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        EditionTopBar(
            projectName = projectName,
            canGoPrev = canGoPrev,
            canGoNext = canGoNext,
            onPrevious = {
                projectViewModel.selectPreviousImage()
                selectedImage?.let { maskViewModel.loadMasksForImage(imageId = it.id) }
            },
            onNext = {
                projectViewModel.selectNextImage()
                selectedImage?.let { maskViewModel.loadMasksForImage(imageId = it.id) }
            },
            onSave = {
                selectedImage?.let {
                    it.bitmap?.let { bitmap ->
                        imageSaveViewModel.saveImageToGallery(context, bitmap) { success ->
                            saveSuccess = success // Guardar si fue exitoso
                            showSaveDialog = true // Mostrar el modal
                        }
                    }
                }
            }
//                onTogglePredict = { predictViewModel.toggleModoPredict() },
//                isPredictMode = modoPredict
        )

        selectedImage?.let { image ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                ImageViewer(
                    image = image,
                    isPredictMode = modoPredict,
                    maskViewModel = maskViewModel,
                    isLoading = isLoading,
                    isMaskLoading = isMaskLoading,
                    onCancelPredict = { predictViewModel.toggleModoPredict() }
                )
            }


            if (!modoPredict) {
                MasksList(
                    imageBitmap = image.bitmap?.asImageBitmap(),
                    viewModel = maskViewModel,
                ) {
                    predictViewModel.toggleModoPredict()
                }
                // Down Bar: visible solo si no está en modo predictivo

                EditionDownBar(
                    onChangeBackground = {
                        // Implementa la lógica para cambiar el fondo
                        println("Cambiar fondo accionado")
                        showPromptBackgroundDialog = true
                    },
                    onGenerateAI = {
                        showPromptDialog = true // Mostrar el diálogo de generación de IA
                    },
                    onDeleteObject = {
                        /*masks.filter { it.active }
                            .forEach { maskViewModel.toggleMaskSelection(it.id) } // Eliminar objeto activo*/
                        showRemoveDialog = true
                    },
                    isEnabled=hasActiveMask
                )

            }
        }
        // Mostrar el diálogo cuando sea necesario
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text(text = if (saveSuccess) "Éxito" else "Error") },
                text = {
                    Text(
                        text = if (saveSuccess) {
                            "La imagen se guardó correctamente en la galería."
                        } else {
                            "Hubo un error al guardar la imagen. Intenta nuevamente."
                        }
                    )
                },
                confirmButton = {
                    Button(onClick = { showSaveDialog = false }) {
                        Text(text = "Aceptar")
                    }
                }
            )
        }
    }
}