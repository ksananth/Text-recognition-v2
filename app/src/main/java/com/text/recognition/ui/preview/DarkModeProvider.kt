package com.text.recognition.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class DarkModeProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}
