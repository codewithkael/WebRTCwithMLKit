package com.codewithkael.webrtcwithmlkit.ui.states

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.codewithkael.webrtcwithmlkit.utils.persistence.FilterStorage

class FiltersUiState(initial: FilterStorage.Config) {
    var showDialog by mutableStateOf(false)

    var textRecognition by mutableStateOf(initial.textRecognition)
    var watermark by mutableStateOf(initial.watermark)
    var faceDetect by mutableStateOf(initial.faceDetect)
    var faceMesh by mutableStateOf(initial.faceMesh)
    var blurBackground by mutableStateOf(initial.blurBackground)

    fun reloadFromStorage(context: Context) {
        val cfg = FilterStorage.load(context)
        textRecognition = cfg.textRecognition
        watermark = cfg.watermark
        faceDetect = cfg.faceDetect
        faceMesh = cfg.faceMesh
        blurBackground = cfg.blurBackground

    }
}

@Composable
fun rememberFiltersUiState(initial: FilterStorage.Config): FiltersUiState {
    return remember(initial) { FiltersUiState(initial) }
}
