package com.stellaridea.swiftvision.ui.views.edition

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.views.edition.components.ConfirmNavigationDialog
import com.stellaridea.swiftvision.ui.views.edition.components.EditionDownBar
import com.stellaridea.swiftvision.ui.views.edition.components.EditionTopBar
import com.stellaridea.swiftvision.ui.views.edition.components.ImageViewer
import com.stellaridea.swiftvision.ui.views.edition.components.MasksList
import com.stellaridea.swiftvision.ui.views.edition.components.PromptBackgroundDialog
import com.stellaridea.swiftvision.ui.views.edition.components.PromptDialog
import com.stellaridea.swiftvision.ui.views.edition.components.RemoveDialog
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.ImageSaveViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.MaskViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.PredictViewModel
import com.stellaridea.swiftvision.ui.views.edition.viewmodels.ProjectViewModel

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
    // Efectos iniciales
    LaunchedEffect(selectedImage) {
        selectedImage?.let { maskViewModel.loadMasksForImage(it.id) }
    }
    LaunchedEffect(Unit) {
        projectViewModel.initializeProject(projectId, projectName)
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
                predictViewModel.masksPoints() // Manejo del prompt
            }
        )
    }
    // Diálogo para ingresar prompt
    if (showRemoveDialog) {
        RemoveDialog(
            onDismiss = { showRemoveDialog = false },
            onConfirm = { prompt ->
                showPromptDialog = false
                predictViewModel.masksPoints() // Manejo del prompt
            }
        )
    }
    // Diálogo para ingresar prompt
    if (showPromptBackgroundDialog) {
        PromptBackgroundDialog(
            onDismiss = { showPromptBackgroundDialog = false },
            onConfirm = { prompt ->
                showPromptDialog = false
                predictViewModel.masksPoints() // Manejo del prompt
            }
        )
    }


    // Pantalla principal
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
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
                        imageSaveViewModel.saveImageToGallery(context, it.bitmap) { save ->
                            // Manejar el guardado exitoso
                        }
                    }
                },
//                onTogglePredict = { predictViewModel.toggleModoPredict() },
//                isPredictMode = modoPredict
            )

            selectedImage?.let { image ->
                Box(modifier = Modifier.weight(1f)) {
                    ImageViewer(
                        image = image,
                        isPredictMode = modoPredict,
                        isImageLoading = isLoading,
                        maskViewModel = maskViewModel,
                        isMaskLoading = isLoading
                    )
                }
                if (!modoPredict) {
                    MasksList(
                        imageBitmap = image.bitmap.asImageBitmap(),
                        viewModel = maskViewModel,
                    ) {
                        predictViewModel.toggleModoPredict()
                    }
                    // Down Bar: visible solo si no está en modo predictivo
                    if (hasActiveMask) {
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
                            }
                        )
                    }
                }
            }
        }
    }
}