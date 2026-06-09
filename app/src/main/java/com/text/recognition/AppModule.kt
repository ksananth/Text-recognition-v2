package com.text.recognition

import com.text.recognition.scanner.ScanCardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::ScanCardViewModel)
}
