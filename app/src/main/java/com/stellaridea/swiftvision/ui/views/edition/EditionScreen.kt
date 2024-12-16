package com.stellaridea.swiftvision.ui.views.edition

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.stellaridea.swiftvision.ui.views.edition.components.EditionTopBar
import com.stellaridea.swiftvision.ui.views.edition.components.ImageViewer
import com.stellaridea.swiftvision.ui.views.edition.components.MasksList
import com.stellaridea.swiftvision.ui.views.edition.components.PromptDialog
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

    var showDialog by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {


            // Contenido principal
            selectedImage?.let { image ->
                Box(modifier = Modifier.weight(1f)) {
                    val modoPredict by predictViewModel.modoPredict.observeAsState(false)

                    ImageViewer(
                        image = image,
                        isPredictMode = modoPredict,
                        isImageLoading = isLoading,
                        maskViewModel = maskViewModel,
                        isMaskLoading = isLoading
                    )

                    if (!modoPredict) {
                        MasksList(
                            imageBitmap = image.bitmap.asImageBitmap(),
                            viewModel = maskViewModel,
                        )
                    }
                }
            }

            // TOP BAR
            EditionTopBar(
                projectViewModel = projectViewModel,
                predictViewModel = predictViewModel,
                maskViewModel = maskViewModel,
                onGeneratePrompt = { showPromptDialog = true },
                onSave = {
                    selectedImage?.bitmap?.let { bitmap ->
                        imageSaveViewModel.saveImageToGallery(context, bitmap) {
                            Log.d("EditionScreen", "Image saved successfully")
                        }
                    }
                },
                onDownload = {
                    selectedImage?.bitmap?.let { bitmap ->
                        imageSaveViewModel.saveImageToGallery(context, bitmap) {
                            Log.d("EditionScreen", "Image downloaded successfully")
                        }
                    }
                },
                onDeleteMask = {}
            )
        }
    }
}
