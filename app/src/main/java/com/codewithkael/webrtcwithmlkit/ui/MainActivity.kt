package com.codewithkael.webrtcwithmlkit.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.codewithkael.webrtcwithmlkit.ui.screens.MainScreen
import com.codewithkael.webrtcwithmlkit.ui.theme.WebRTCwithMLKitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebRTCwithMLKitTheme {
                MainScreen()
            }
        }
    }
}