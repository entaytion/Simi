package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import ua.entaytion.simi.R
import ua.entaytion.simi.utils.MoneyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashBalanceScreen(onBack: () -> Unit, isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    var expected by remember { mutableStateOf("") }
    var actual by remember { mutableStateOf("") }

    val expectedValue = expected.replace(',', '.').toDoubleOrNull() ?: 0.0
    val actualValue = actual.replace(',', '.').toDoubleOrNull() ?: 0.0
    val rawDelta = actualValue - expectedValue
    val roundedDelta = MoneyUtils.roundedDifference(rawDelta)
    val denominations = MoneyUtils.breakdown(abs(roundedDelta))

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                        title = { Text("Вирівнювання готівки") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        painter = painterResource(id = R.drawable.ic_back),
                                        contentDescription = "Назад"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onToggleTheme) {
                                Icon(
                                        painter = painterResource(id = if (isDarkTheme) R.drawable.ic_dark_mode else R.drawable.ic_light_mode),
                                        contentDescription = "Перемкнути тему",
                                        tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                )
            }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
                modifier =
                        Modifier.padding(innerPadding)
                                .padding(16.dp)
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor =
                                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.5f
                                            )
                            )
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = expected,
                            onValueChange = { expected = it },
                            label = { Text("Очікувана сума (система)") },
                            leadingIcon = {
                                Icon(
                                        painter = painterResource(R.drawable.ic_cash_desk),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor =
                                                    MaterialTheme.colorScheme.onSurface,
                                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedLabelColor =
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                            focusedContainerColor =
                                                    MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor =
                                                    MaterialTheme.colorScheme.surface,
                                            focusedBorderColor =
                                                    MaterialTheme.colorScheme.onSurface,
                                            cursorColor = MaterialTheme.colorScheme.onSurface
                                    )
                    )

                    OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = actual,
                            onValueChange = { actual = it },
                            label = { Text("Фактична сума в касі") },
                            leadingIcon = {
                                Icon(
                                        painter = painterResource(R.drawable.ic_cash_total),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor =
                                                    MaterialTheme.colorScheme.onSurface,
                                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedLabelColor =
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                            focusedContainerColor =
                                                    MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor =
                                                    MaterialTheme.colorScheme.surface,
                                            focusedBorderColor =
                                                    MaterialTheme.colorScheme.onSurface,
                                            cursorColor = MaterialTheme.colorScheme.onSurface
                                    )
                    )
                }
            }

            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
            ) {
                Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                                painter = painterResource(R.drawable.ic_cash_total),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = "Результат",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    val status =
                            when {
                                roundedDelta > 0 -> "Заберіть ${"%.0f".format(roundedDelta)} грн"
                                roundedDelta < 0 ->
                                        "Додайте ${"%.0f".format(abs(roundedDelta))} грн"
                                else -> "Баланс рівний"
                            }

                    Text(
                            text = status,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                    )

                    Text(
                            text = "Суми до 0.50 грн автоматично заокруглюємо",
                            style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (denominations.isNotEmpty()) {
                Text(
                        text = "Номінали",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                ) {
                    Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        denominations.forEach { item ->
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                        text = "x${item.count}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
