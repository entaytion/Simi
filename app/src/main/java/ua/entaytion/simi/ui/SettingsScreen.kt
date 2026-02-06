package ua.entaytion.simi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ua.entaytion.simi.R
import com.google.firebase.database.FirebaseDatabase
import ua.entaytion.simi.data.model.UserMode
import ua.entaytion.simi.viewmodel.SettingsViewModel
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.ui.components.MenuRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel) {
    val state by viewModel.settingsState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var debugClickCount by remember { mutableIntStateOf(0) }
    var showDebugDialog by remember { mutableStateOf(false) }

    if (showDebugDialog) {
        var notificationTitle by remember { mutableStateOf("") }
        var notificationMessage by remember { mutableStateOf("") }

        AlertDialog(
                onDismissRequest = { showDebugDialog = false },
                title = { Text("Тестове сповіщення") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                                value = notificationTitle,
                                onValueChange = { notificationTitle = it },
                                label = { Text("Заголовок") },
                                modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                                value = notificationMessage,
                                onValueChange = { notificationMessage = it },
                                label = { Text("Опис") },
                                modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Локальний тест - тільки на цьому пристрої
                        TextButton(
                                onClick = {
                                    ua.entaytion.simi.worker.ExpirationWorker
                                            .sendCustomNotification(
                                                    context,
                                                    notificationTitle.ifEmpty { "Simi" },
                                                    notificationMessage.ifEmpty {
                                                        "Тестове сповіщення"
                                                    }
                                            )
                                    showDebugDialog = false
                                }
                        ) { Text("Тест локально") }

                        // Firebase - для всіх пристроїв
                        TextButton(
                                onClick = {
                                    val database =
                                            FirebaseDatabase.getInstance(
                                                    "https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app"
                                            )
                                    val ref = database.getReference("global_notifications")
                                    val notification =
                                            mapOf(
                                                    "title" to notificationTitle.ifEmpty { "Simi" },
                                                    "message" to
                                                            notificationMessage.ifEmpty {
                                                                "Тестове сповіщення"
                                                            },
                                                    "sentAt" to System.currentTimeMillis()
                                            )
                                    ref.setValue(notification)
                                    showDebugDialog = false
                                }
                        ) { Text("Надіслати всім") }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDebugDialog = false }) { Text("Скасувати") }
                }
        )
    }

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
                                                    showDebugDialog = true
                                                    debugClickCount = 0
                                                }
                                            }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        painter = painterResource(id = ua.entaytion.simi.R.drawable.ic_back),
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
                        iconRes = if (isDark) ua.entaytion.simi.R.drawable.ic_dark_mode else ua.entaytion.simi.R.drawable.ic_light_mode,
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
                            iconRes = ua.entaytion.simi.R.drawable.ic_person,
                            iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { viewModel.setUserMode(mode) },
                            endContent = {
                                if (isSelected) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_ok),
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
            }
        }
    }
}
