package ua.entaytion.simi.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.entaytion.simi.R
import ua.entaytion.simi.data.model.UserMode
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.ui.components.MenuRow

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
        onOpenExpiration: () -> Unit,
        onOpenCashBalance: () -> Unit,
        onOpenChecklist: () -> Unit,
        onOpenDonuts: () -> Unit,
        onOpenHotDogs: () -> Unit,
        onOpenNotifications: () -> Unit,
        onOpenSettings: () -> Unit,
        onOpenDateCalculator: () -> Unit,
        userMode: UserMode,
        isDarkTheme: Boolean,
        pendingNotificationsCount: Int = 0
) {
        val allItems =
                listOf(
                        HomeItem(
                                "Загроза протерміну",
                                R.drawable.ic_expiration,
                                Color(0xFFF44336),
                                onOpenExpiration,
                                setOf(UserMode.EXPERIENCED, UserMode.INDIFFERENT),
                                HomeGroup.PRIORITY
                        ),
                        HomeItem(
                                "Калькулятор дат",
                                R.drawable.ic_days_left,
                                Color(0xFFE91E63),
                                onOpenDateCalculator,
                                setOf(UserMode.NEWBIE, UserMode.EXPERIENCED, UserMode.INDIFFERENT),
                                HomeGroup.PRIORITY
                        ),
                        HomeItem(
                                "Вирівнювання готівки",
                                R.drawable.ic_cash,
                                Color(0xFF4CAF50),
                                onOpenCashBalance,
                                setOf(UserMode.NEWBIE, UserMode.EXPERIENCED, UserMode.INDIFFERENT),
                                HomeGroup.FINANCE
                        ),
                        HomeItem(
                                "Чек-ліст",
                                R.drawable.ic_checklist,
                                Color(0xFF2196F3),
                                onOpenChecklist,
                                setOf(UserMode.NEWBIE, UserMode.INDIFFERENT),
                                HomeGroup.TASKS
                        ),
                        HomeItem(
                                "Докласти донати",
                                R.drawable.ic_donut,
                                Color(0xFFFF9800),
                                onOpenDonuts,
                                setOf(UserMode.NEWBIE, UserMode.INDIFFERENT),
                                HomeGroup.GOODS
                        ),
                        HomeItem(
                                "Докласти хот-доги",
                                R.drawable.ic_hotdog,
                                Color(0xFFFF5722),
                                onOpenHotDogs,
                                setOf(UserMode.NEWBIE, UserMode.INDIFFERENT),
                                HomeGroup.GOODS
                        )
                )

        val displayedItems = allItems.filter { it.visibleFor.contains(userMode) }
        val groupedItems = displayedItems.groupBy { it.group }
        val groupsOrder = listOf(HomeGroup.PRIORITY, HomeGroup.FINANCE, HomeGroup.TASKS, HomeGroup.GOODS)

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
                                                        painter = painterResource(id = R.drawable.ic_settings),
                                                        contentDescription = "Налаштування",
                                                        tint = MaterialTheme.colorScheme.onSurface
                                                )
                                        }
                                }
                        )
                }
        ) { innerPadding ->
                Column(
                        modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        groupsOrder.forEach { group ->
                                val items = groupedItems[group]
                                if (!items.isNullOrEmpty()) {
                                        MenuContainer {
                                                items.forEachIndexed { index, item ->
                                                        MenuRow(
                                                                title = item.title,
                                                                iconRes = item.iconRes,
                                                                iconTint = item.accent,
                                                                onClick = item.onTap,
                                                                endContent = {
                                                                    Icon(
                                                                        painter = painterResource(id = R.drawable.ic_back),
                                                                        contentDescription = null,
                                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                                        modifier = Modifier.size(20.dp).rotate(180f)
                                                                    )
                                                                }
                                                        )
                                                        if (index < items.size - 1) {
                                                                HorizontalDivider(
                                                                        modifier = Modifier.padding(start = 56.dp),
                                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                }
        }
}

private enum class HomeGroup {
        PRIORITY,
        FINANCE,
        TASKS,
        GOODS
}

private data class HomeItem(
        val title: String,
        @DrawableRes val iconRes: Int,
        val accent: Color,
        val onTap: () -> Unit,
        val visibleFor: Set<UserMode>,
        val group: HomeGroup
)
