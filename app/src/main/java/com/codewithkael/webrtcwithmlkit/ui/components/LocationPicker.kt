package com.codewithkael.webrtcwithmlkit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codewithkael.webrtcwithmlkit.utils.imageProcessor.WatermarkLocation

@Composable
fun LocationPicker(
    value: WatermarkLocation,
    onChange: (WatermarkLocation) -> Unit
) {
    val items = listOf(
        WatermarkLocation.TOP_LEFT to Pair("Top Left", Icons.Filled.NorthWest),
        WatermarkLocation.TOP_RIGHT to Pair("Top Right", Icons.Filled.NorthEast),
        WatermarkLocation.CENTER to Pair("Center", Icons.Filled.CenterFocusWeak),
        WatermarkLocation.BOTTOM_LEFT to Pair("Bottom Left", Icons.Filled.SouthWest),
        WatermarkLocation.BOTTOM_RIGHT to Pair("Bottom Right", Icons.Filled.SouthEast),
    )

    // 2 rows grid: 3 + 2 (last row gets a spacer to align)
    val rows = listOf(items.take(3), items.drop(3))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (loc, meta) ->
                    val (label, icon) = meta
                    val selected = value == loc

                    LocationTile(
                        selected = selected,
                        label = label,
                        icon = icon,
                        onClick = { onChange(loc) },
                        modifier = Modifier.weight(1.5f)
                    )
                }

                if (index == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LocationTile(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    else MaterialTheme.colorScheme.surface

    val border = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .aspectRatio(1.6f) // wide tile, looks modern
            .border(1.dp, border, shape)
            .background(bg, shape)
            .clickable(onClick = onClick)
            .padding(5.dp),
        color = Color.Transparent,
        shape = shape
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
