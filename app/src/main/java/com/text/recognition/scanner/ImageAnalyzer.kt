package com.text.recognition.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

private val CARD_NUMBER_REGEX = Regex("""(\d{4}[\s\-]?\d{4}[\s\-]?\d{4}[\s\-]?\d{4})""")
private val EXPIRY_REGEX = Regex("""(0[1-9]|1[0-2])[/\-](\d{2}|\d{4})""")

class ImageAnalyzer(
    private val onDetect: (CardDetails) -> Unit,
    private val onCameraError: (Throwable) -> Unit,
) : Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val enabled = AtomicBoolean(false)

    override fun enable() { enabled.set(true) }
    private fun disable() { enabled.set(false) }

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (!enabled.get()) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val raw = visionText.text
                val cardNumber = CARD_NUMBER_REGEX.find(raw)?.value?.replace(Regex("[\\s\\-]"), "") ?: ""
                val expiry = EXPIRY_REGEX.find(raw)?.value ?: ""
                if (cardNumber.length == 16 && expiry.isNotEmpty()) {
                    disable()
                    onDetect(CardDetails(cardNumber = cardNumber, expiryDate = expiry))
                }
            }
            .addOnFailureListener(onCameraError)
            .addOnCompleteListener { imageProxy.close() }
    }
}
