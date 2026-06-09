package com.text.recognition.ui.components

import androidx.compose.runtime.Composable
import com.text.recognition.R

@Composable
fun CameraErrorScreen(
    error: Throwable,
    onPrimaryButtonClicked: () -> Unit,
) {
    InfoScreen(
        imageRes = R.drawable.ic_camera_error,
        title = "Camera will not start up",
        description = "Make sure that the camera is functioning correctly and try again.",
        primaryButtonText = "Close",
        onPrimaryButtonClicked = onPrimaryButtonClicked
    )
}
