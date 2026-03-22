package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.entaytion.simi.ui.components.SimiIcons
import ua.entaytion.simi.ui.components.MenuContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DefrostItem(val name: String, val days: Int)

val defrostItems = listOf(
    DefrostItem("Тістечко \"Макаронс мікс\" 21г /Nonpareil/", 50),
    DefrostItem("Тістечко \"Тарти мікс\" 40г /Nonpareil/", 30),
    DefrostItem("Десерт \"Солона карамель\" 100г /Nonpareil/", 16),
    DefrostItem("Тістечко Salted Caramel Napoleon", 16),
    DefrostItem("Тістечко Caramel (з горіхом)", 16),
    DefrostItem("Десерт \"Чизкейк з малиною\" 100г /Nonpareil/", 14),
    DefrostItem("Тістечко Raspberry Cheesecake", 14),
    DefrostItem("Тістечко Cherry Pincher", 14),
    DefrostItem("Вафлі \"Трубочка\" зі згущеним молоком 50г /Мантінга Україна/", 30),
    DefrostItem("Торт \"Вафельний\" зі згущеним молоком 45г /Мантінга Україна/", 30),
    DefrostItem("Торт \"Чизкейк Нью-Йорк\" 130г /GFS/", 15),
    DefrostItem("Мафін \"Шоколадно-банановий\" 80г /Party Box/", 17),
    DefrostItem("Мафін \"Тірамісу\" 80г /Party Box/", 17),
    DefrostItem("Мафін \"Шоколадний\" 80г /Party Box/", 17),
    DefrostItem("Мафін \"Латте\" з маршмелоу 80г /Party Box/", 20),
    DefrostItem("Тістечко \"Еклер із заварним кремом\" 50г /Nonpareil/", 8),
    DefrostItem("Тістечко \"Еклер зі згущеним молоком\" 50г /Nonpareil/", 8),
    DefrostItem("Горішки в ХО (2-6 градусів)", 60)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefrostCalculatorScreen(onBack: () -> Unit) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Терміни дефростації") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = SimiIcons.Back, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Сьогодні: ${today.format(formatter)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Дати розраховано автоматично",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            MenuContainer {
                defrostItems.forEachIndexed { index, item ->
                    val expirationDate = today.plusDays(item.days.toLong())
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Термін: ${item.days} діб",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = expirationDate.format(formatter),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(+${item.days} днів)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (index < defrostItems.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
