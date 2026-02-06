package ua.entaytion.simi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MenuContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
fun MenuRow(
    title: String,
    icon: Painter? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        if (endContent != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                endContent()
            }
        }
    }
}

@Composable
fun MenuRow(
    title: String,
    iconVector: ImageVector, // Overload for Vector icons
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null
) {
    MenuRow(
        title = title,
        icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(iconVector),
        iconTint = iconTint,
        onClick = onClick,
        endContent = endContent
    )
}

@Composable
fun MenuRow(
    title: String,
    iconRes: Int, // Overload for Drawable resources
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null
) {
    MenuRow(
        title = title,
        icon = painterResource(id = iconRes),
        iconTint = iconTint,
        onClick = onClick,
        endContent = endContent
    )
}
