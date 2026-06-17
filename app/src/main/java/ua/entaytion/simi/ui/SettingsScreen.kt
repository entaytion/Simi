package ua.entaytion.simi.ui

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import ua.entaytion.simi.data.model.UserMode
import ua.entaytion.simi.viewmodel.SettingsViewModel
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.ui.components.MenuRow
import ua.entaytion.simi.ui.components.SimiIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    onOpenAdminPanel: () -> Unit
) {
    val state by viewModel.settingsState.collectAsState()

    var debugClickCount by remember { mutableIntStateOf(0) }

    Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                        title = {
                            Text(
                                    "Налаштування",
                                    modifier =
                                            Modifier.clickable(
                                                            interactionSource =
                                                                    remember {
                                                                        MutableInteractionSource()
                                                                    },
                                                            indication = null
                                                    ) {
                                                debugClickCount++
                                                if (debugClickCount >= 5) {
                                                    onOpenAdminPanel()
                                                    debugClickCount = 0
                                                }
                                            }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        imageVector = SimiIcons.Back,
                                        contentDescription = "Назад"
                                )
                            }
                        }
                )
            }
    ) { innerPadding ->
        Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state?.let { currentState ->
                
                // Theme
                MenuContainer {
                    val isDark = currentState.isDarkTheme
                    MenuRow(
                        title = "Темна тема",
                        icon = if (isDark) SimiIcons.DarkMode else SimiIcons.LightMode,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setDarkTheme(!isDark) },
                        endContent = {
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.setDarkTheme(it) }
                            )
                        }
                    )
                }

                Text(
                    text = "Режим користувача",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )

                // User Mode Options
                MenuContainer {
                    val modes = listOf(
                        Triple(UserMode.NEWBIE, "Новачок", "Каса, Чек-ліст, Донати, Хот-доги"),
                        Triple(UserMode.EXPERIENCED, "Досвідчений", "Загроза протерміну, Каса, Нагадування протерміну"),
                        Triple(UserMode.INDIFFERENT, "Мені байдуже", "Всі функції доступні")
                    )

                    modes.forEachIndexed { index, (mode, title, desc) ->
                        val isSelected = currentState.userMode == mode
                        MenuRow(
                            title = title,
                            icon = SimiIcons.Person,
                            iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { viewModel.setUserMode(mode) },
                            endContent = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = SimiIcons.Ok,
                                        contentDescription = "Вибрано",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                        if (index < modes.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                // Description for selected mode
                val selectedDesc = when(currentState.userMode) {
                    UserMode.NEWBIE -> "Режим для новачків: фокусуємось на основних операціях."
                    UserMode.EXPERIENCED -> "Режим для досвідчених: контроль термінів та каси."
                    UserMode.INDIFFERENT -> "Повний доступ до всіх функцій."
                }
                
                Text(
                    text = selectedDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Про додаток",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )

                MenuContainer {
                    val context = LocalContext.current
                    val appVersion = ua.entaytion.simi.BuildConfig.VERSION_NAME
                    MenuRow(
                        title = "Автор",
                        icon = SimiIcons.Person,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://t.me/entaytion")
                            )
                            context.startActivity(intent)
                        },
                        endContent = {
                            Text(
                                text = "@entaytion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    MenuRow(
                        title = "Версія",
                        icon = SimiIcons.Settings,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = {},
                        endContent = {
                            Text(
                                text = appVersion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}
