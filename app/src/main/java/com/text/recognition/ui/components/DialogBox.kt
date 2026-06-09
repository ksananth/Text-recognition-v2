package com.text.recognition.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DialogBox(
    title: String,
    message: String,
    positiveButton: String,
    negativeButton: String,
    positiveListener: () -> Unit,
    negativeListener: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = positiveListener,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = positiveListener) { Text(text = positiveButton) }
        },
        dismissButton = {
            TextButton(onClick = negativeListener) { Text(text = negativeButton) }
        }
    )
}
