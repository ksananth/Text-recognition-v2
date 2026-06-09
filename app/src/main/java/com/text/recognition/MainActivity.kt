package com.text.recognition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.text.recognition.scanner.CardDetails
import com.text.recognition.scanner.ScanCardScreenEvents
import com.text.recognition.ui.theme.ScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanTheme {
                var scannedCard by remember { mutableStateOf<CardDetails?>(null) }

                ScanCardScreen(
                    onEvent = { event ->
                        when (event) {
                            is ScanCardScreenEvents.OnCardScanned -> scannedCard = event.cardDetails
                            ScanCardScreenEvents.OnBackRequested -> finish()
                        }
                    }
                )

                scannedCard?.let { card ->
                    AlertDialog(
                        onDismissRequest = { scannedCard = null },
                        title = { Text("Card Scanned") },
                        text = {
                            Text(
                                "Card Number: ${card.cardNumber}\n" +
                                "Expiry: ${card.expiryDate}"
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { scannedCard = null }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}
