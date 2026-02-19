package com.codewithkael.webrtcwithmlkit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.webrtc.SurfaceViewRenderer

@Composable
fun VideoStageSection(
    modifier: Modifier = Modifier,
    inCall: Boolean,
    onRemoteReady: (SurfaceViewRenderer) -> Unit,
    onLocalReady: (SurfaceViewRenderer) -> Unit
) {
    if (!inCall) return

    Box(
        modifier = modifier
            .background(Color.Black, RoundedCornerShape(12.dp))
    ) {

        SurfaceViewRendererComposable(
            modifier = Modifier.fillMaxSize(),
            onSurfaceReady = onRemoteReady
        )

        SurfaceViewRendererComposable(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .fillMaxWidth(0.32f)
                .aspectRatio(3f / 4f)
                .shadow(12.dp, RoundedCornerShape(14.dp))
                .background(Color.Black, RoundedCornerShape(14.dp))
                .padding(2.dp)
                .background(Color.Black, RoundedCornerShape(12.dp)),
            onSurfaceReady = onLocalReady
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xAA000000))
                    )
                )
        )
    }
}
