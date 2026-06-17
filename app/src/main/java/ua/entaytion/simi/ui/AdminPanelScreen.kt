package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import ua.entaytion.simi.ui.components.SimiIcons
import ua.entaytion.simi.worker.ExpirationWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var notificationTitle by remember { mutableStateOf("") }
    var notificationMessage by remember { mutableStateOf("") }

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
        }
    }
}
