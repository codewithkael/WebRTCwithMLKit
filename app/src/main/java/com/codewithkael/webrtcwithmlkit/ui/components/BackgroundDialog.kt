package com.codewithkael.webrtcwithmlkit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.codewithkael.webrtcwithmlkit.data.persistence.BackgroundStorage
import com.codewithkael.webrtcwithmlkit.ui.states.BackgroundUiState
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.BackgroundScaleMode

@Composable
fun BackgroundDialog(
    state: BackgroundUiState,
    onPickImage: () -> Unit,
    onCancel: () -> Unit,
    onSave: (BackgroundStorage.Config) -> Unit,
    onClear: (() -> Unit)? = null,
) {
    val scroll = rememberScrollState()
    val ctx = LocalContext.current

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Background") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // --- Pick image ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Background image")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = onPickImage) { Text("Pick Image") }
                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))

                        if (!state.uriString.isNullOrBlank() && onClear != null) {
                            OutlinedButton(onClick = onClear) { Text("Clear") }
                        }
                    }

                    if (state.uriString.isNullOrBlank()) {
                        Text("No image selected")
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(state.uriString)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected background preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // --- Scale mode ---
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Scale mode")

                    ScaleModeRow(
                        title = "Center Crop",
                        subtitle = "Fill frame, crop edges (no stretch)",
                        selected = state.scaleMode == BackgroundScaleMode.CENTER_CROP,
                        onSelect = { state.scaleMode = BackgroundScaleMode.CENTER_CROP }
                    )

                    ScaleModeRow(
                        title = "Stretch",
                        subtitle = "Fill frame by stretching (may distort)",
                        selected = state.scaleMode == BackgroundScaleMode.STRETCH,
                        onSelect = { state.scaleMode = BackgroundScaleMode.STRETCH }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    BackgroundStorage.Config(
                        uri = state.uriString,
                        scaleMode = state.scaleMode
                    )
                )
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}

@Composable
private fun ScaleModeRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(title)
            Text(subtitle)
        }
    }
}