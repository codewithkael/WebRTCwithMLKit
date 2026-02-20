package com.codewithkael.webrtcwithmlkit.ui.states

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.codewithkael.webrtcwithmlkit.data.persistence.WatermarkStorage

class WatermarkUiState(initial: WatermarkStorage.Config) {
    var showDialog by mutableStateOf(false)

    var uriString by mutableStateOf(initial.uri)
        private set

    var location by mutableStateOf(initial.location)
    var marginDp by mutableFloatStateOf(initial.marginDp)
    var sizeFraction by mutableFloatStateOf(initial.sizeFraction)

    fun onPickedUri(uri: Uri) {
        uriString = uri.toString()
    }
}

@Composable
fun rememberWatermarkUiState(initial: WatermarkStorage.Config): WatermarkUiState {
    return remember(initial) { WatermarkUiState(initial) }
}
