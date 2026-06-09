package com.text.recognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.text.recognition.scanner.Analyzer
import com.text.recognition.scanner.ImageAnalyzer
import com.text.recognition.scanner.ScanCardScreenEvents
import com.text.recognition.scanner.ScanCardViewModel
import com.text.recognition.ui.components.CameraErrorScreen
import com.text.recognition.ui.components.DialogBox
import com.text.recognition.ui.components.DialogButton
import com.text.recognition.ui.components.Gap
import com.text.recognition.ui.components.AppScaffold
import com.text.recognition.ui.preview.DarkModeProvider
import com.text.recognition.ui.theme.ScanTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executors

@Composable
internal fun ScanCardScreen(
    onEvent: (ScanCardScreenEvents) -> Unit,
    viewModel: ScanCardViewModel = koinViewModel(),
) {
    val currentActivity = LocalContext.current as? Activity

    val cameraError by viewModel.cameraError.collectAsState()
    val imageAnalyzer = remember {
        ImageAnalyzer(
            onDetect = viewModel::onDetect,
            onCameraError = viewModel::onError
        )
    }

    LaunchedEffect(Unit) {
        imageAnalyzer.enable()
        viewModel.scannedCard.collect {
            onEvent(ScanCardScreenEvents.OnCardScanned(cardDetails = it))
        }
    }

    when (val error = cameraError) {
        null -> {
            AppScaffold(
                screenTitle = "Scan my debit card",
                onBackPress = { onEvent(ScanCardScreenEvents.OnBackRequested) }
            ) {
                CardScannerScreenContent(
                    imageAnalyzer = imageAnalyzer,
                    onRationalPositiveClicked = { onEvent(ScanCardScreenEvents.OnBackRequested) },
                    onRationalNegativeClicked = {
                        onEvent(ScanCardScreenEvents.OnBackRequested)
                        currentActivity?.goToSettings()
                    }
                )
            }
        }
        else -> {
            AppScaffold(
                screenTitle = "Scan my debit card"
            ) {
                CameraErrorScreen(
                    error = error,
                    onPrimaryButtonClicked = { onEvent(ScanCardScreenEvents.OnBackRequested) }
                )
            }
        }
    }
}

private fun Activity.goToSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setData(Uri.fromParts("package", packageName, null))
    })
}

private const val OCR_TIMER_DURATION: Long = 10000

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun CardScannerScreenContent(
    imageAnalyzer: Analyzer,
    onRationalPositiveClicked: () -> Unit,
    onRationalNegativeClicked: () -> Unit,
) {
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    when {
        permissionState.status.isGranted -> {
            CardScanner(
                imageAnalyzer = imageAnalyzer,
            )
        }

        permissionState.status.shouldShowRationale -> {
            var closeDialog by remember { mutableStateOf(value = false) }
            if (!closeDialog) {
                DialogBox(
                    title = "Authorise access",
                    message = "The camera is required in order to allow the app to scan your card.",
                    positiveButton = "Close",
                    negativeButton = "Settings",
                    positiveListener = {
                        closeDialog = true
                        onRationalPositiveClicked()
                    },
                    negativeListener = {
                        closeDialog = true
                        onRationalNegativeClicked()
                    }
                )
            }
        }

        else -> {
            LaunchedEffect(key1 = permissionState) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}

@Composable
internal fun CardScanner(
    imageAnalyzer: Analyzer,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember(calculation = Executors::newSingleThreadExecutor)

    var showTimeoutDialog by remember { mutableStateOf(value = false) }
    LaunchedEffect(key1 = showTimeoutDialog) {
        if (!showTimeoutDialog) {
            delay(timeMillis = OCR_TIMER_DURATION)
            showTimeoutDialog = true
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                controller = LifecycleCameraController(ctx).apply {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    setImageAnalysisAnalyzer(cameraExecutor, imageAnalyzer)
                    bindToLifecycle(lifecycleOwner)
                }
            }
        }
    )

    if (showTimeoutDialog) {
        ScanCardTimeoutDialog(
            onDismissed = { showTimeoutDialog = false },
            onRetry = { showTimeoutDialog = false }
        )
    }
}

@Composable
internal fun ScanCardTimeoutDialog(
    onDismissed: () -> Unit,
    onRetry: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissed,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Gap(gap = 24.dp)
            Image(
                painter = painterResource(id = R.drawable.ic_scan_document),
                contentDescription = ""
            )
            Text(
                text = "Having trouble scanning? Make sure the card is well-lit and fully visible.",
                modifier = Modifier.padding(all = 24.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            DialogButton(
                text = "Retry",
                onClick = onRetry,
            )
        }
    }
}

@Preview
@Composable
private fun Preview(
    @PreviewParameter(provider = DarkModeProvider::class) darkMode: Boolean
) = ScanTheme(darkTheme = darkMode) {
    ScanCardTimeoutDialog(onDismissed = {}, onRetry = {})
}
