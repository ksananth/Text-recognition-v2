package com.text.recognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.text.recognition.scanner.ScanCardScreenEvents
import com.text.recognition.ui.theme.ScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanTheme {
                ScanCardScreen(
                    onEvent = { event ->
                        when (event) {
                            is ScanCardScreenEvents.OnCardScanned -> {
                                // TODO: navigate to result screen with event.cardDetails
                            }
                            ScanCardScreenEvents.OnBackRequested -> finish()
                        }
                    }
                )
            }
        }
    }
}
