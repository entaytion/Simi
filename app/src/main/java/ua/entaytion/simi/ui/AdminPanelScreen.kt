package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.ui.components.SimiIcons
import ua.entaytion.simi.viewmodel.DonutsViewModel
import ua.entaytion.simi.worker.ExpirationWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    viewModel: DonutsViewModel
) {
    val context = LocalContext.current
    val donutNames by viewModel.donutNames.collectAsState()

    var notificationTitle by remember { mutableStateOf("") }
    var notificationMessage by remember { mutableStateOf("") }
    var newDonutName by remember { mutableStateOf("") }

    var editingDonut by remember { mutableStateOf<String?>(null) }
    var editDonutNameText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Адмін-панель (Приховано)") },
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
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // СЕКЦІЯ 1: Сповіщення
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Глобальні сповіщення",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = notificationTitle,
                        onValueChange = { notificationTitle = it },
                        label = { Text("Заголовок сповіщення") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notificationMessage,
                        onValueChange = { notificationMessage = it },
                        label = { Text("Текст сповіщення") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ExpirationWorker.sendCustomNotification(
                                    context,
                                    notificationTitle.ifEmpty { "Simi" },
                                    notificationMessage.ifEmpty { "Тестове сповіщення" }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Тест локально")
                        }

                        Button(
                            onClick = {
                                val database = FirebaseDatabase.getInstance(
                                    "https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app"
                                )
                                val ref = database.getReference("global_notifications")
                                val notification = mapOf(
                                    "title" to notificationTitle.ifEmpty { "Simi" },
                                    "message" to notificationMessage.ifEmpty { "Тестове сповіщення" },
                                    "sentAt" to System.currentTimeMillis()
                                )
                                ref.setValue(notification)
                                notificationTitle = ""
                                notificationMessage = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Надіслати всім")
                        }
                    }
                }
            }

            // СЕКЦІЯ 2: Керування списком донатів
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Керування донатами",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = { viewModel.resetDonutNames() }
                        ) {
                            Text("Скинути")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newDonutName,
                            onValueChange = { newDonutName = it },
                            label = { Text("Нова позиція") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        FilledIconButton(
                            onClick = {
                                if (newDonutName.isNotBlank()) {
                                    viewModel.addDonutName(newDonutName.trim())
                                    newDonutName = ""
                                }
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = SimiIcons.Add,
                                contentDescription = "Додати"
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = "Список в базі (${donutNames.size} шт):",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    MenuContainer {
                        donutNames.forEachIndexed { index, name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            editingDonut = name
                                            editDonutNameText = name
                                        }
                                    ) {
                                        Icon(
                                            imageVector = SimiIcons.Edit,
                                            contentDescription = "Редагувати",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeDonutName(name) }
                                    ) {
                                        Icon(
                                            imageVector = SimiIcons.Delete,
                                            contentDescription = "Видалити",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            if (index < donutNames.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingDonut != null) {
        AlertDialog(
            onDismissRequest = { editingDonut = null },
            title = { Text("Редагувати назву донату") },
            text = {
                OutlinedTextField(
                    value = editDonutNameText,
                    onValueChange = { editDonutNameText = it },
                    label = { Text("Нова назва") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val old = editingDonut
                        if (old != null && editDonutNameText.isNotBlank()) {
                            viewModel.renameDonutName(old, editDonutNameText.trim())
                        }
                        editingDonut = null
                    }
                ) {
                    Text("Зберегти")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingDonut = null }) {
                    Text("Скасувати")
                }
            }
        )
    }
}
