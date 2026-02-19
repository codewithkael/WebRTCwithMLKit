package com.codewithkael.webrtcwithmlkit.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codewithkael.webrtcwithmlkit.utils.MyApplication

@Composable
fun TopBarSection(
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Your ID: ${MyApplication.UserID}",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
