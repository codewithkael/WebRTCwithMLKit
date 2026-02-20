package com.codewithkael.webrtcwithmlkit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codewithkael.webrtcwithmlkit.R
import com.codewithkael.webrtcwithmlkit.ui.states.FiltersUiState
import com.codewithkael.webrtcwithmlkit.utils.persistence.FilterStorage

@Composable
fun FiltersDialog(
    state: FiltersUiState, onCancel: () -> Unit, onSave: (FilterStorage.Config) -> Unit
) {
    val scroll = rememberScrollState()

    AlertDialog(onDismissRequest = onCancel, title = { Text("Filters") }, text = {
        Column(
            modifier = Modifier
                .heightIn(max = 420.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            FilterTile(
                title = "Text Recognition (OCR)",
                subtitle = "Detect & draw text (ML Kit v2)",
                checked = state.textRecognition,
                imageRes = R.drawable.ic_face_filter,
                onToggle = { state.textRecognition = it })

            FilterTile(
                title = "Watermark",
                subtitle = "Show watermark on camera",
                checked = state.watermark,
                imageRes = R.drawable.ic_watermark_filter,
                onToggle = { state.watermark = it })

            FilterTile(
                title = "Face Detect",
                subtitle = "Draw face oval (ML Kit)",
                checked = state.faceDetect,
                imageRes = R.drawable.ic_face_filter,
                onToggle = { state.faceDetect = it }
            )
            FilterTile(
                title = "Face Mesh",
                subtitle = "468-point mesh overlay (ML Kit)",
                checked = state.faceMesh,
                imageRes = R.drawable.ic_face_filter,
                onToggle = { state.faceMesh = it }
            )
            FilterTile(
                title = "Background Blur",
                subtitle = "Blur background (segmentation)",
                checked = state.blurBackground,
                imageRes = R.drawable.ic_blur_filter,
                onToggle = { state.blurBackground = it }
            )
        }
    }, confirmButton = {
        Button(onClick = {
            onSave(
                FilterStorage.Config(
                    textRecognition = state.textRecognition,
                    watermark = state.watermark,
                    faceDetect = state.faceDetect,
                    faceMesh = state.faceMesh,
                    blurBackground = state.blurBackground,

                    )
            )
        }) { Text("OK") }
    }, dismissButton = {
        TextButton(onClick = onCancel) { Text("Cancel") }
    })
}
