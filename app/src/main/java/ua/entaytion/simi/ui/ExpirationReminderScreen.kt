package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import ua.entaytion.simi.data.model.ExpirationReminder
import ua.entaytion.simi.viewmodel.ExpirationReminderViewModel

// SCREEN 1: Notifications Center (Bell Icon)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpirationNotificationsScreen(onBack: () -> Unit, viewModel: ExpirationReminderViewModel) {
    val reminders by viewModel.reminders.collectAsState()

    // Filter only those that need action and are not written off
    val activeNotifications =
            reminders.filter { item ->
                val today = System.currentTimeMillis()
                val needsAction =
                        (today >= item.finalDate) ||
                                (item.discount50Date != null &&
                                        today >= item.discount50Date &&
                                        !item.isDiscount50Applied) ||
                                (item.discount25Date != null &&
                                        today >= item.discount25Date &&
                                        !item.isDiscount25Applied) ||
                                (item.discount10Date != null &&
                                        today >= item.discount10Date &&
                                        !item.isDiscount10Applied)
                needsAction && !item.isWrittenOff
            }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Сповіщення") },
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
        if (activeNotifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Немає нових сповіщень", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeNotifications) { item ->
                    ExpirationNotificationItemCard(
                            item = item,
                            onCheck = {
                                val today = System.currentTimeMillis()
                                if (today >= item.finalDate) {
                                    viewModel.markWrittenOff(item.id)
                                } else if (item.discount50Date != null &&
                                                today >= item.discount50Date &&
                                                !item.isDiscount50Applied
                                ) {
                                    viewModel.markDiscountApplied(item.id, 50)
                                } else if (item.discount25Date != null &&
                                                today >= item.discount25Date &&
                                                !item.isDiscount25Applied
                                ) {
                                    viewModel.markDiscountApplied(item.id, 25)
                                } else if (item.discount10Date != null &&
                                                today >= item.discount10Date &&
                                                !item.isDiscount10Applied
                                ) {
                                    viewModel.markDiscountApplied(item.id, 10)
                                }
                            },
                            onClose = { viewModel.markWrittenOff(item.id) }
                    )
                }
            }
        }
    }
}

// SCREEN 2: Management / Inventory (Pink Card)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpirationManagementScreen(onBack: () -> Unit, viewModel: ExpirationReminderViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    var showAddEditDialog by remember { mutableStateOf<ExpirationReminder?>(null) }
    var isAdding by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Керування протерміном") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Назад"
                                )
                            }
                        }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { isAdding = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Додати")
                }
            }
    ) { innerPadding ->
        LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reminders) { item ->
                ExpirationManagementItemCard(item = item, onClick = { showAddEditDialog = item })
            }
        }
    }

    if (isAdding) {
        AddEditExpirationReminderDialog(
                onDismiss = { isAdding = false },
                onConfirm = { name, initDate, finalDate ->
                    viewModel.addReminder(name, initDate, finalDate)
                    isAdding = false
                }
        )
    }

    if (showAddEditDialog != null) {
        AddEditExpirationReminderDialog(
                editingItem = showAddEditDialog,
                onDismiss = { showAddEditDialog = null },
                onConfirm = { name, initDate, finalDate ->
                    val updated =
                            showAddEditDialog!!.copy(
                                    name = name,
                                    initialDate = initDate,
                                    finalDate = finalDate
                            )
                    viewModel.updateReminder(updated)
                    showAddEditDialog = null
                },
                onDelete = {
                    viewModel.deleteReminder(showAddEditDialog!!.id)
                    showAddEditDialog = null
                }
        )
    }
}

@Composable
fun ExpirationNotificationItemCard(
        item: ExpirationReminder,
        onCheck: () -> Unit,
        onClose: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
            modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))

                val today = System.currentTimeMillis()
                val statusText =
                        when {
                            today >= item.finalDate -> "ПРОТЕРМІНОВАНО!"
                            item.discount50Date != null &&
                                    today >= item.discount50Date &&
                                    !item.isDiscount50Applied -> "Треба 50%"
                            item.discount25Date != null &&
                                    today >= item.discount25Date &&
                                    !item.isDiscount25Applied -> "Треба 25%"
                            item.discount10Date != null &&
                                    today >= item.discount10Date &&
                                    !item.isDiscount10Applied -> "Треба 10%"
                            else -> ""
                        }

                if (statusText.isNotEmpty()) {
                    Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            color =
                                    if (statusText.contains("!")) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                        text = "Термін до: ${dateFormat.format(Date(item.finalDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onCheck) {
                    Icon(
                            Icons.Default.Check,
                            contentDescription = "Виконано",
                            tint = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                            Icons.Default.Close,
                            contentDescription = "Скасувати",
                            tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ExpirationManagementItemCard(item: ExpirationReminder, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                        text = "До: ${dateFormat.format(Date(item.finalDate))}",
                        style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                    Icons.Default.Edit,
                    contentDescription = "Редагувати",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpirationReminderDialog(
        editingItem: ExpirationReminder? = null,
        onDismiss: () -> Unit,
        onConfirm: (String, Long?, Long) -> Unit,
        onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(editingItem?.name ?: "") }
    var initialDate by remember { mutableStateOf(editingItem?.initialDate) }
    var finalDate by remember { mutableStateOf(editingItem?.finalDate) }

    var showInitialPicker by remember { mutableStateOf(false) }
    var showFinalPicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (editingItem == null) "Додати товар" else "Редагувати") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Назва товару") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                            value = initialDate?.let { dateFormat.format(Date(it)) } ?: "",
                            onValueChange = {},
                            label = { Text("Дата виготовлення (опц.)") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showInitialPicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                            value = finalDate?.let { dateFormat.format(Date(it)) } ?: "",
                            onValueChange = {},
                            label = { Text("Кінцевий термін") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showFinalPicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            },
                            isError = finalDate == null,
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Видалити",
                                    tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Button(
                            onClick = {
                                if (name.isNotBlank() && finalDate != null) {
                                    onConfirm(name, initialDate, finalDate!!)
                                }
                            },
                            enabled = name.isNotBlank() && finalDate != null
                    ) { Text(if (editingItem == null) "Додати" else "Зберегти") }
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )

    if (showInitialPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
        DatePickerDialog(
                onDismissRequest = { showInitialPicker = false },
                confirmButton = {
                    TextButton(
                            onClick = {
                                initialDate = datePickerState.selectedDateMillis
                                showInitialPicker = false
                            }
                    ) { Text("ОК") }
                },
                dismissButton = {
                    TextButton(onClick = { showInitialPicker = false }) { Text("Скасувати") }
                }
        ) { DatePicker(state = datePickerState) }
    }

    if (showFinalPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = finalDate)
        DatePickerDialog(
                onDismissRequest = { showFinalPicker = false },
                confirmButton = {
                    TextButton(
                            onClick = {
                                finalDate = datePickerState.selectedDateMillis
                                showFinalPicker = false
                            }
                    ) { Text("ОК") }
                },
                dismissButton = {
                    TextButton(onClick = { showFinalPicker = false }) { Text("Скасувати") }
                }
        ) { DatePicker(state = datePickerState) }
    }
}
