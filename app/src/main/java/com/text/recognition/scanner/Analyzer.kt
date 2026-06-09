package com.text.recognition.scanner

import androidx.camera.core.ImageAnalysis

interface Analyzer : ImageAnalysis.Analyzer {
    fun enable()
}
