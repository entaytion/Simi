package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import ua.entaytion.simi.R
import ua.entaytion.simi.viewmodel.HotDogEntry
import ua.entaytion.simi.viewmodel.HotDogType
import ua.entaytion.simi.viewmodel.HotDogsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotDogsScreen(
        onBack: () -> Unit,
        viewModel: HotDogsViewModel,
        isDarkTheme: Boolean,
        onToggleTheme: () -> Unit
) {
    val items = remember {
        listOf(
                "Світла булка (маленька)",
                "Світла булка (велика)",
                "Темна булка (маленька)",
                "Темна булка (велика)",
                "Сосиски Large",
                "Сосиски XLarge",
                "Сосиски Big",
                "Швейцарські з сиром",
                "В'ялені з томатами (італійські)",
                "Зальцбурзькі (австрійська)",
                "Сосиски White",
                "Сосиски Black"
        )
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedName by remember { mutableStateOf("") }
    val entries = viewModel.entries

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                        title = { Text("Докласти хот-доги") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Назад"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onToggleTheme) {
                                Icon(
                                        imageVector =
                                                if (isDarkTheme) Icons.Filled.Brightness4
                                                else Icons.Filled.Brightness7,
                                        contentDescription = "Тема"
                                )
                            }
                        }
                )
            }
    ) { innerPadding ->
        Column(
                modifier =
                        Modifier.padding(innerPadding)
                                .padding(16.dp)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                                painter = painterResource(id = R.drawable.ic_hotdog),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                        )
                        Text(
                                "Вибери позицію",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                                value = selectedName,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                label = { Text("Позиція") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors =
                                        OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor =
                                                        MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor =
                                                        MaterialTheme.colorScheme.surface
                                        )
                        )

                        ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                        ) {
                            items.forEach { name ->
                                DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            selectedName = name
                                            expanded = false
                                        }
                                )
                            }
                        }
                    }

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                                onClick = {
                                    if (selectedName.isNotBlank())
                                            viewModel.addEntry(selectedName, HotDogType.DeFrost)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = selectedName.isNotBlank()
                        ) { Text("На дефрост") }
                        Button(
                                onClick = {
                                    if (selectedName.isNotBlank())
                                            viewModel.addEntry(selectedName, HotDogType.Container)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = selectedName.isNotBlank()
                        ) { Text("У контейнер") }
                    }
                }
            }

            entries.forEach { entry ->
                HotDogItem(
                        entry = entry,
                        dateFormatter,
                        timeFormatter,
                        onDelete = { viewModel.removeEntry(entry) }
                )
            }
        }
    }
}

@Composable
private fun HotDogItem(
        entry: HotDogEntry,
        dateFormatter: DateTimeFormatter,
        timeFormatter: DateTimeFormatter,
        onDelete: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = entry.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = if (entry.type == HotDogType.DeFrost) "ДЕФРОСТ" else "КОНТЕЙНЕР",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                            "ПТП ${dateFormatter.format(entry.ptp)}",
                            style = MaterialTheme.typography.labelMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                            text = timeFormatter.format(entry.ptp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                    )
                    Text(
                            " / ",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                            text = timeFormatter.format(entry.ktp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                            "КТП ${dateFormatter.format(entry.ktp)}",
                            style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
