package com.stellaridea.swiftvision.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Sí",
    dismissText: String = "No"
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { DialogTitle(title) },
        text = { DialogText(message) },
        confirmButton = {
            DialogButtons(onConfirm = onConfirm, onDismiss = onDismiss, confirmText = confirmText, dismissText = dismissText)
        },
    )
}

@Composable
fun DialogTitle(title: String) {
    Text(text = title)
}

@Composable
fun DialogText(message: String) {
    Text(text = message)
}

@Composable
fun DialogButtons(onConfirm: () -> Unit, onDismiss: () -> Unit, confirmText: String = "Sí", dismissText: String = "No") {
    TextButton(onClick = onConfirm) {
        Text(confirmText)
    }
    TextButton(onClick = onDismiss) {
        Text(dismissText)
    }
}