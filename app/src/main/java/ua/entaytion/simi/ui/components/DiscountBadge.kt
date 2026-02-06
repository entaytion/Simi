package ua.entaytion.simi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiscountBadge(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (percent) {
        10 -> Color(0xFFAEEA00) // Lime Green
        25 -> Color(0xFFFFD600) // Yellow
        50 -> Color(0xFFFF6D00) // Orange
        75 -> Color(0xFFFF4081) // Pink/Red
        else -> Color.Gray
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "-$percent%",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 16.sp
            )
            // Fake Barcode lines
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(8.dp)
            ) {
                // Just some static lines to simulate barcode look
                androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height
                    var x = 0f
                    while (x < width) {
                        val lineWidth = if (x % 3 == 0f) 2.dp.toPx() else 1.dp.toPx()
                        drawRect(
                            color = Color.Black.copy(alpha = 0.6f),
                            topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                            size = androidx.compose.ui.geometry.Size(lineWidth, height)
                        )
                        x += lineWidth + 2.dp.toPx()
                    }
                }
            }
        }
    }
}
