package com.text.recognition.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

private val CARD_NUMBER_INLINE_REGEX = Regex("""\d{4}[\s\-]?\d{4}[\s\-]?\d{4}[\s\-]?\d{4}""")
private val FOUR_DIGITS_REGEX = Regex("""^\d{4}$""")
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
                val cardNumber = extractCardNumber(raw)
                val expiry = EXPIRY_REGEX.find(raw)?.value ?: ""
                if (cardNumber.length == 16 && expiry.isNotEmpty()) {
                    disable()
                    onDetect(CardDetails(cardNumber = cardNumber, expiryDate = expiry))
                }
            }
            .addOnFailureListener(onCameraError)
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun extractCardNumber(text: String): String {
        // Single line: "4567 3456 4577 4567" or "4567-3456-4577-4567"
        val inline = CARD_NUMBER_INLINE_REGEX.find(text)?.value?.replace(Regex("""[\s\-]"""), "")
        if (inline?.length == 16) return inline

        // Multi-line: four consecutive lines each containing exactly 4 digits
        val lines = text.lines().map { it.trim() }
        for (i in 0..lines.size - 4) {
            val group = lines.subList(i, i + 4)
            if (group.all { FOUR_DIGITS_REGEX.matches(it) }) {
                return group.joinToString("")
            }
        }

        return ""
    }
}
