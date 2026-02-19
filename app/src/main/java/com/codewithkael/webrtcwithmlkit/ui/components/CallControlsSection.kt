package com.codewithkael.webrtcwithmlkit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codewithkael.webrtcwithmlkit.utils.MyApplication

@Composable
fun CallControlsSection(
    modifier: Modifier = Modifier,
    onCall: (String) -> Unit
) {
    var callId by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = callId,
            onValueChange = { callId = it },
            label = { Text("Enter User ID") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = { onCall(callId) },
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Call")
        }
    }
}

