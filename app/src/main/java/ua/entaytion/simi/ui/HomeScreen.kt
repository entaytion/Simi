package ua.entaytion.simi.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.entaytion.simi.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
        onOpenExpiration: () -> Unit,
        onOpenCashBalance: () -> Unit,
        onOpenChecklist: () -> Unit,
        onOpenDonuts: () -> Unit,
        onOpenHotDogs: () -> Unit,
        onOpenExpirationReminder: () -> Unit,
        onOpenNotifications: () -> Unit,
        onOpenSettings: () -> Unit,
        userMode: ua.entaytion.simi.data.model.UserMode,
        isDarkTheme: Boolean,
        pendingNotificationsCount: Int = 0
) {
        val allItems =
                listOf(
                        HomeItem(
                                "Загроза\nпротерміну",
                                R.drawable.ic_expiration,
                                Color(0xFFF44336),
                                onOpenExpiration,
                                visibleFor =
                                        setOf(
                                                ua.entaytion.simi.data.model.UserMode.EXPERIENCED,
                                                ua.entaytion.simi.data.model.UserMode.INDIFFERENT
                                        )
                        ),
                        HomeItem(
                                "Нагадування\nпротерміну",
                                R.drawable.ic_notify,
                                Color(0xFFE91E63),
                                onOpenExpirationReminder,
                                visibleFor =
                                        setOf(
                                                ua.entaytion.simi.data.model.UserMode.EXPERIENCED,
                                                ua.entaytion.simi.data.model.UserMode.INDIFFERENT
                                        )
                        ),
                        HomeItem(
                                "Вирівнювання готівки",
                                R.drawable.ic_cash,
                                Color(0xFF4CAF50),
                                onOpenCashBalance,
                                visibleFor =
                                        setOf(
                                                ua.entaytion.simi.data.model.UserMode.NEWBIE,
                                                ua.entaytion.simi.data.model.UserMode.EXPERIENCED,
                                                ua.entaytion.simi.data.model.UserMode.INDIFFERENT
                                        )
                        ),
                        HomeItem(
                                "Чек-ліст",
                                R.drawable.ic_checklist,
                                Color(0xFF2196F3),
                                onOpenChecklist,
                                visibleFor =
                                        setOf(
                                                ua.entaytion.simi.data.model.UserMode.NEWBIE,
                                                ua.entaytion.simi.data.model.UserMode.INDIFFERENT
                                        )
                        ),
                        HomeItem(
                                "Докласти\nдонати",
                                R.drawable.ic_donut,
                                Color(0xFFFF9800),
                                onOpenDonuts,
                                visibleFor =
                                        setOf(
                                                ua.entaytion.simi.data.model.UserMode.NEWBIE,
                                                ua.entaytion.simi.data.model.UserMode.INDIFFERENT
                                        )
                        ),
                        HomeItem(
                                "Докласти\nхот-доги",
                                R.drawable.ic_hotdog,
                                Color(0xFFFF5722),
                                onOpenHotDogs,
                                visibleFor =
                                        setOf(
                                                ua.entaytion.simi.data.model.UserMode.NEWBIE,
                                                ua.entaytion.simi.data.model.UserMode.INDIFFERENT
                                        )
                        )
                )

        val displayedItems = allItems.filter { it.visibleFor.contains(userMode) }

        Scaffold(
                topBar = {
                        CenterAlignedTopAppBar(
                                title = { Text("Інструменти") },
                                actions = {
                                        IconButton(onClick = onOpenNotifications) {
                                                BadgedBox(
                                                        badge = {
                                                                if (pendingNotificationsCount > 0) {
                                                                        Badge(
                                                                                containerColor =
                                                                                        Color(
                                                                                                0xFFF44336
                                                                                        )
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                if (pendingNotificationsCount >
                                                                                                                99
                                                                                                )
                                                                                                        "99+"
                                                                                                else
                                                                                                        pendingNotificationsCount
                                                                                                                .toString(),
                                                                                        fontSize =
                                                                                                10.sp,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                ) {
                                                        Icon(
                                                                painter =
                                                                        painterResource(
                                                                                R.drawable.ic_bell
                                                                        ),
                                                                contentDescription = "Сповіщення",
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )
                                                }
                                        }
                                        IconButton(onClick = onOpenSettings) {
                                                Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "Налаштування",
                                                        tint = MaterialTheme.colorScheme.onSurface
                                                )
                                        }
                                }
                        )
                }
        ) { innerPadding ->
                LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                ) {
                        items(displayedItems) { item ->
                                HomeCard(item = item, isDarkTheme = isDarkTheme)
                        }
                }
        }
}

private data class HomeItem(
        val title: String,
        @DrawableRes val iconRes: Int,
        val accent: Color,
        val onTap: () -> Unit,
        val visibleFor: Set<ua.entaytion.simi.data.model.UserMode>
)

@Composable
private fun HomeCard(item: HomeItem, isDarkTheme: Boolean) {
        val cardBg = item.accent.copy(alpha = if (isDarkTheme) 0.15f else 0.1f)
        val iconBg = item.accent.copy(alpha = if (isDarkTheme) 0.25f else 0.15f)

        Card(
                onClick = item.onTap,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        Surface(
                                color = iconBg,
                                shape = CircleShape,
                                modifier = Modifier.size(52.dp)
                        ) {
                                Box(contentAlignment = Alignment.Center) {
                                        // Try catch for icon resource to prevent crash if user lied
                                        // about adding it?
                                        // Compose doesn't crash easily on drawables but better be
                                        // safe if I could.
                                        // But I can't try-catch declarative UI easily.
                                        // Assuming R.drawable.ic_notify exists as per user
                                        // statement.
                                        // If not, I should have used a placeholder.
                                        // I will use 'R.drawable.ic_expiration' for the NEW item
                                        // momentarily to be safe,
                                        // OR assume the user provided it.
                                        // "я добавил иконку ic_notify" -> proceed with
                                        // `R.drawable.ic_notify`.
                                        // Wait, I can't reference it if it's not in my R file
                                        // context because the user added it OUTSIDE this chat
                                        // context?
                                        // No, "The user's current state... Active Document...". I
                                        // don't see R file changes.
                                        // If the user added it, it SHOULD be in R class. But I
                                        // cannot see R class generated code.
                                        // I'll assume `R.drawable.ic_notify` is valid.
                                        Icon(
                                                painter = painterResource(item.iconRes),
                                                contentDescription = null,
                                                tint = item.accent,
                                                modifier = Modifier.size(28.dp)
                                        )
                                }
                        }

                        Text(
                                text = item.title,
                                style =
                                        MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                lineHeight =
                                                        MaterialTheme.typography
                                                                .titleLarge
                                                                .lineHeight * 0.85f
                                        ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                        )
                }
        }
}
