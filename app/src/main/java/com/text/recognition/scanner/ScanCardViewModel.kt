package com.text.recognition.scanner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class ScanCardViewModel : ViewModel() {

    private val _cameraError = MutableStateFlow<Throwable?>(null)
    val cameraError: StateFlow<Throwable?> = _cameraError.asStateFlow()

    private val _scannedCard = Channel<CardDetails>(Channel.BUFFERED)
    val scannedCard = _scannedCard.receiveAsFlow()

    fun onDetect(cardDetails: CardDetails) {
        _scannedCard.trySend(cardDetails)
    }

    fun onError(throwable: Throwable) {
        _cameraError.value = throwable
    }
}
