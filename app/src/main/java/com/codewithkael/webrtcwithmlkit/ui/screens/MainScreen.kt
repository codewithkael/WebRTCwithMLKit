package com.codewithkael.webrtcwithmlkit.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codewithkael.webrtcwithmlkit.ui.components.CallControlsSection
import com.codewithkael.webrtcwithmlkit.ui.components.FooterSection
import com.codewithkael.webrtcwithmlkit.ui.components.TopBarSection
import com.codewithkael.webrtcwithmlkit.ui.components.VideoStageSection
import com.codewithkael.webrtcwithmlkit.ui.viewmodel.MainViewModel

@Composable
fun MainScreen() {

    val viewModel: MainViewModel = hiltViewModel()
    val callState by viewModel.callState.collectAsState()
    val context = LocalContext.current

    // ---------- Permissions ----------
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!permissions.all { it.value }) {
            Toast.makeText(
                context, "Camera and Microphone permissions are required", Toast.LENGTH_SHORT
            ).show()
        } else {
            viewModel.permissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAEAEA))
            .padding(top = 14.dp)
    ) {

        TopBarSection(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f),
        )
        CallControlsSection(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onCall = { viewModel.sendStartCallSignal(it) })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f)
                .padding(8.dp)
        ) {
            if (callState) {
                VideoStageSection(
                    modifier = Modifier.fillMaxSize(),
                    inCall = true,
                    onRemoteReady = { viewModel.initRemoteSurfaceView(it) },
                    onLocalReady = { viewModel.startLocalStream(it) })
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black, RoundedCornerShape(12.dp))
                )
            }
        }

        FooterSection(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 5.dp, vertical = 10.dp)
        )
    }
}
