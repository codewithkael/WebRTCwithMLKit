package com.codewithkael.webrtcwithmlkit.ui.states

import android.net.Uri
import androidx.compose.runtime.*
import com.codewithkael.webrtcwithmlkit.data.persistence.BackgroundStorage

class BackgroundUiState(initial: BackgroundStorage.Config) {
    var showDialog by mutableStateOf(false)

    var uriString by mutableStateOf(initial.uri)
        private set

    var scaleMode by mutableStateOf(initial.scaleMode)

    fun onPickedUri(uri: Uri) {
        uriString = uri.toString()
    }

    fun clear() { uriString = null }
}

@Composable
fun rememberBackgroundUiState(initial: BackgroundStorage.Config): BackgroundUiState {
    return remember(initial) { BackgroundUiState(initial) }
}