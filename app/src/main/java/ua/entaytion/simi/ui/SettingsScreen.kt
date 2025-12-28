package ua.entaytion.simi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import ua.entaytion.simi.data.model.UserMode
import ua.entaytion.simi.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel) {
    val state by viewModel.settingsState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var debugClickCount by remember { mutableStateOf(0) }
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
                TopAppBar(
                        title = {
                            Text(
                                    "Налаштування",
                                    modifier =
                                            Modifier.padding(8.dp).clickable(
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
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Назад"
                                )
                            }
                        }
                )
            }
    ) { innerPadding ->
        Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Switcher
            state?.let { currentState ->
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Темна тема", style = MaterialTheme.typography.titleMedium)
                    Switch(
                            checked = currentState.isDarkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) }
                    )
                }

                HorizontalDivider()

                // User Mode
                Text(text = "Режим користувача", style = MaterialTheme.typography.titleMedium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeOption(
                            title = "Новачок",
                            description = "Каса, Чек-ліст, Донати, Хот-доги",
                            isSelected = currentState.userMode == UserMode.NEWBIE,
                            onClick = { viewModel.setUserMode(UserMode.NEWBIE) }
                    )
                    ModeOption(
                            title = "Досвідчений",
                            description = "Загроза протерміну, Каса, Нагадування протерміну",
                            isSelected = currentState.userMode == UserMode.EXPERIENCED,
                            onClick = { viewModel.setUserMode(UserMode.EXPERIENCED) }
                    )
                    ModeOption(
                            title = "Мені байдуже",
                            description = "Всі функції доступні",
                            isSelected = currentState.userMode == UserMode.INDIFFERENT,
                            onClick = { viewModel.setUserMode(UserMode.INDIFFERENT) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModeOption(title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
            onClick = onClick,
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                    ),
            modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isSelected, onClick = onClick)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall)
            }
            if (description.isNotEmpty()) {
                Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 40.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
