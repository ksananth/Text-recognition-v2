package com.text.recognition.scanner

sealed interface ScanCardScreenEvents {
    data class OnCardScanned(val cardDetails: CardDetails) : ScanCardScreenEvents
    data object OnBackRequested : ScanCardScreenEvents
}
