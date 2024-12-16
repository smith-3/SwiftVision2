package com.stellaridea.swiftvision.ui.views.edition.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.stellaridea.swiftvision.ui.common.ConfirmDialog

@Composable
fun ConfirmNavigationDialog(
    showDialog: Boolean,
    navController: NavHostController,
    onDialogStateChange: (Boolean) -> Unit
) {
    if (showDialog) {
        ConfirmDialog(
            onConfirm = {
                onDialogStateChange(false)
                navController.popBackStack()
            },
            onDismiss = { onDialogStateChange(false) },
            title = "Confirmación",
            message = "¿Estás seguro de que deseas volver atrás y perder tu progreso?"
        )
    }
}
