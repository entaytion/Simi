package ua.entaytion.simi.ui

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.entaytion.simi.data.model.UserMode
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.ui.components.MenuRow
import ua.entaytion.simi.ui.components.SimiIcons

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
        onOpenExpiration: () -> Unit,
        onOpenCashBalance: () -> Unit,
        onOpenDonuts: () -> Unit,
        onOpenHotDogs: () -> Unit,
        onOpenSettings: () -> Unit,
        onOpenDateCalculator: () -> Unit,
        onOpenDefrostCalculator: () -> Unit,
        onOpenBaking: () -> Unit,
        userMode: UserMode,
        pendingNotificationsCount: Int = 0
) {
        val allItems =
                listOf(
                        HomeItem(
                                "Загроза протерміну",
                                SimiIcons.Expiration,
                                Color(0xFF7033AC),
                                onOpenExpiration,
                                setOf(UserMode.EXPERIENCED, UserMode.INDIFFERENT),
                                HomeGroup.PRIORITY
                        ),
                        HomeItem(
                                "Калькулятор дат",
                                SimiIcons.DaysLeft,
                                Color(0xFF7033AC),
                                onOpenDateCalculator,
                                setOf(UserMode.NEWBIE, UserMode.EXPERIENCED, UserMode.INDIFFERENT),
                                HomeGroup.PRIORITY
                        ),
                        HomeItem(
                                "Терміни дефростації",
                                SimiIcons.DaysLeft,
                                Color(0xFF7033AC),
                                onOpenDefrostCalculator,
                                setOf(UserMode.NEWBIE, UserMode.EXPERIENCED, UserMode.INDIFFERENT),
                                HomeGroup.PRIORITY
                        ),
                        HomeItem(
                                "Вирівнювання готівки",
                                SimiIcons.Cash,
                                Color(0xFF7033AC),
                                onOpenCashBalance,
                                setOf(UserMode.NEWBIE, UserMode.EXPERIENCED, UserMode.INDIFFERENT),
                                HomeGroup.FINANCE
                        ),
                        HomeItem(
                                "Докласти донати",
                                SimiIcons.Donut,
                                Color(0xFF7033AC),
                                onOpenDonuts,
                                setOf(UserMode.NEWBIE, UserMode.INDIFFERENT),
                                HomeGroup.GOODS
                        ),
                        HomeItem(
                                "Докласти хот-доги",
                                SimiIcons.HotDog,
                                Color(0xFF7033AC),
                                onOpenHotDogs,
                                setOf(UserMode.NEWBIE, UserMode.INDIFFERENT),
                                HomeGroup.GOODS
                        ),
                        HomeItem(
                                "Випічка",
                                SimiIcons.Baking,
                                Color(0xFF7033AC),
                                onOpenBaking,
                                setOf(UserMode.NEWBIE, UserMode.EXPERIENCED, UserMode.INDIFFERENT),
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
                                        IconButton(onClick = onOpenSettings) {
                                                Icon(
                                                        imageVector = SimiIcons.Settings,
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
                                                                icon = item.icon,
                                                                iconTint = item.accent,
                                                                onClick = item.onTap,
                                                                endContent = {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        if (item.title == "Загроза протерміну" && pendingNotificationsCount > 0) {
                                                                            Badge(
                                                                                containerColor = Color(0xFFF44336),
                                                                                modifier = Modifier.padding(end = 8.dp)
                                                                            ) {
                                                                                Text(
                                                                                    text = if (pendingNotificationsCount > 99) "99+" else pendingNotificationsCount.toString(),
                                                                                    color = Color.White,
                                                                                    fontWeight = FontWeight.Bold,
                                                                                    fontSize = 10.sp
                                                                                )
                                                                            }
                                                                        }
                                                                        Icon(
                                                                            imageVector = SimiIcons.Forward,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                                            modifier = Modifier.size(20.dp)
                                                                        )
                                                                    }
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
        val icon: ImageVector,
        val accent: Color,
        val onTap: () -> Unit,
        val visibleFor: Set<UserMode>,
        val group: HomeGroup
)
