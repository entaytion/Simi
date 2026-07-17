package ua.entaytion.simi.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import ua.entaytion.simi.ui.components.SimiIcons
import ua.entaytion.simi.viewmodel.BakingEntry
import ua.entaytion.simi.viewmodel.BakingProduct
import ua.entaytion.simi.viewmodel.BakingViewModel
import ua.entaytion.simi.viewmodel.SettingsViewModel

fun getInitialRoundedDateTime(): LocalDateTime {
    val now = LocalDateTime.now()
    val minutes = now.minute
    return if (minutes >= 30) {
        now.plusHours(1).withMinute(0).withSecond(0).withNano(0)
    } else {
        now.withMinute(0).withSecond(0).withNano(0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakingScreen(
    onBack: () -> Unit,
    viewModel: BakingViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val showBakingAll = settingsState?.showBakingAll ?: false

    var selectedDateTime by remember { mutableStateOf(getInitialRoundedDateTime()) }
    var selectedProduct by remember { mutableStateOf<BakingProduct?>(null) }
    var showProductSelectDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val entries = viewModel.entries

    val ukLocale = remember { Locale.Builder().setLanguage("uk").setRegion("UA").build() }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val displayTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val cardDateTimeFormatter = remember { DateTimeFormatter.ofPattern("d MMM HH:mm", ukLocale) }

    val filteredProducts = remember(searchQuery) {
        viewModel.availableProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Випічка") },
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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning Banner: Stickers > 24 hours
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚠️ Увага! Стікери пишуться, якщо термін придатності більше 24 годин!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Card 1: Time Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Час випікання",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Picker Button
                        Button(
                            onClick = {
                                val datePickerDialog = android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        selectedDateTime = selectedDateTime
                                            .withYear(year)
                                            .withMonth(month + 1)
                                            .withDayOfMonth(dayOfMonth)
                                    },
                                    selectedDateTime.year,
                                    selectedDateTime.monthValue - 1,
                                    selectedDateTime.dayOfMonth
                                )
                                datePickerDialog.show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(selectedDateTime.format(displayDateFormatter))
                        }

                        // Time Picker Button
                        Button(
                            onClick = {
                                val timePickerDialog = android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        selectedDateTime = selectedDateTime
                                            .withHour(hourOfDay)
                                            .withMinute(minute)
                                            .withSecond(0)
                                            .withNano(0)
                                    },
                                    selectedDateTime.hour,
                                    selectedDateTime.minute,
                                    true
                                )
                                timePickerDialog.show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(selectedDateTime.format(displayTimeFormatter))
                        }
                    }

                    TextButton(
                        onClick = { selectedDateTime = getInitialRoundedDateTime() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Скинути на поточний округлений час")
                    }
                }
            }

            if (!showBakingAll) {
                // Card 2: Product Selector and Add Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
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
                                imageVector = SimiIcons.Baking,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Вибір продукції",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Selected Product Button or Field
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProductSelectDialog = true }
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.name ?: "Натисніть, щоб вибрати позицію",
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = if (selectedProduct != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        if (selectedProduct != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Код: ${selectedProduct!!.code}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val displayHoursText = if (selectedProduct!!.shelfLifeHours == 168) "7 діб" else "${selectedProduct!!.shelfLifeHours} год"
                                Text(
                                    text = "Термін: $displayHoursText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                selectedProduct?.let {
                                    viewModel.addEntry(it, selectedDateTime)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedProduct != null
                        ) {
                            Text("Розрахувати та додати")
                        }
                    }
                }
            } else {
                // Show all available products directly as cards
                Text(
                    text = "Уся випічка",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                viewModel.availableProducts.forEach { product ->
                    val ptp = selectedDateTime
                    val ktp = ptp.plusHours(product.shelfLifeHours.toLong()).minusMinutes(1)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.addEntry(product, selectedDateTime) },
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Код: ${product.code}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        "ВИХІД",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = ptp.format(cardDateTimeFormatter),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    "➔",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "СПИСАННЯ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = ktp.format(cardDateTimeFormatter),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Entries List
            if (!showBakingAll && entries.isNotEmpty()) {
                Text(
                    text = "Додані позиції",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                entries.forEach { entry ->
                    BakingItemRow(
                        entry = entry,
                        cardDateTimeFormatter = cardDateTimeFormatter,
                        onDelete = { viewModel.removeEntry(entry) }
                    )
                }
            }
        }
    }

    // Search and select product dialog
    if (showProductSelectDialog) {
        AlertDialog(
            onDismissRequest = { showProductSelectDialog = false },
            title = { Text("Оберіть випічку") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Пошук за назвою чи кодом") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredProducts.forEach { product ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProduct = product
                                            showProductSelectDialog = false
                                            searchQuery = ""
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Код: ${product.code}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val displayHoursText = if (product.shelfLifeHours == 168) "7 діб" else "${product.shelfLifeHours} год"
                                            Text(
                                                text = "Термін: $displayHoursText",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProductSelectDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }
}

@Composable
fun BakingItemRow(
    entry: BakingEntry,
    cardDateTimeFormatter: DateTimeFormatter,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Код: ${entry.product.code}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = SimiIcons.Delete,
                        contentDescription = "Видалити",
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
                        "ВИХІД",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = entry.ptp.format(cardDateTimeFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    "➔",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "СПИСАННЯ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = entry.ktp.format(cardDateTimeFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
