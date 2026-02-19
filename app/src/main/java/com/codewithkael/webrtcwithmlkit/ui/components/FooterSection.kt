package com.codewithkael.webrtcwithmlkit.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.codewithkael.webrtcwithmlkit.R

@Composable
fun FooterSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://www.youtube.com/@codewithkael".toUri()
                )
                context.startActivity(intent)
            }
            .background(Color(0xA4D6DFE5), RoundedCornerShape(8.dp)),
        horizontalArrangement = Arrangement.Absolute.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.youtube_logo),
            contentDescription = null,
            modifier = Modifier
                .size(54.dp)
                .weight(1f),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "To Learn how to create this app, Join my Youtube channel now !! \n www.Youtube.com/@CodeWithKael",
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .weight(6f),
            color = Color.Gray,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
