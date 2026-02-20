package com.codewithkael.webrtcwithmlkit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codewithkael.webrtcwithmlkit.ui.states.WatermarkUiState
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.WatermarkLocation
import com.codewithkael.webrtcwithmlkit.data.persistence.WatermarkStorage

@Composable
fun WatermarkDialog(
    state: WatermarkUiState,
    onPickImage: () -> Unit,
    onCancel: () -> Unit,
    onSave: (WatermarkStorage.Config) -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Watermark") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Select PNG/JPG") }

                Text("Location")
                LocationPicker(
                    value = state.location,
                    onChange = { state.location = it }
                )

                Text("Size: ${(state.sizeFraction * 100).toInt()}%")
                Slider(
                    value = state.sizeFraction,
                    onValueChange = { state.sizeFraction = it },
                    valueRange = 0.05f..0.40f
                )

                Text(
                    text = if (state.location == WatermarkLocation.CENTER)
                        "Drop (dp): ${state.marginDp.toInt()}"
                    else
                        "Margin (dp): ${state.marginDp.toInt()}"
                )
                Slider(
                    value = state.marginDp,
                    onValueChange = { state.marginDp = it },
                    valueRange = 0f..64f
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    WatermarkStorage.Config(
                        uri = state.uriString,
                        location = state.location,
                        marginDp = state.marginDp,
                        sizeFraction = state.sizeFraction
                    )
                )
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}
