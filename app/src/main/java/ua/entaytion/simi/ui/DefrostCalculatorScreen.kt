package ua.entaytion.simi.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.entaytion.simi.ui.components.SimiIcons
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.viewmodel.DefrostItem
import ua.entaytion.simi.viewmodel.DefrostViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefrostCalculatorScreen(
    onBack: () -> Unit,
    viewModel: DefrostViewModel
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val context = LocalContext.current

    // Selectable base/start date (defaults to today)
    var selectedDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isToday = selectedDate == today

    val defrostItemsList by viewModel.defrostItems.collectAsState()

    // Admin mode states
    var isAdminMode by remember { mutableStateOf(false) }
    var titleClickCount by remember { mutableIntStateOf(0) }

    var localList by remember(defrostItemsList) { mutableStateOf(defrostItemsList) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    // State for Quick Calculator
    var quickDaysInput by remember { mutableStateOf("") }

    // State for Dialog (Add / Edit)
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<DefrostItem?>(null) }
    var dialogProductName by remember { mutableStateOf("") }
    var dialogProductDays by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Терміни дефростації" + if (isAdminMode) " ⚙️" else "",
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            titleClickCount++
                            if (titleClickCount >= 3) {
                                isAdminMode = !isAdminMode
                                titleClickCount = 0
                                Toast.makeText(
                                    context,
                                    if (isAdminMode) "Режим редагування активовано" else "Режим перегляду",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (if (isToday) "Сьогодні: " else "Початкова дата: ") + selectedDate.format(formatter),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Натисніть, щоб змінити початкову дату",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    if (!isToday) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { selectedDate = today }) {
                            Text("Скинути на сьогодні")
                        }
                    }
                }
            }

            // Quick Calculator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Швидкий калькулятор термінів",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    OutlinedTextField(
                        value = quickDaysInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                quickDaysInput = input
                            }
                        },
                        label = { Text("Введіть кількість днів дефросту") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    val quickDays = quickDaysInput.toIntOrNull()
                    if (quickDays != null && quickDays > 0) {
                        val quickDate = selectedDate.plusDays((quickDays - 1).toLong())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Кінцевий термін: ${quickDate.format(formatter)} (включно)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Придатний протягом $quickDays днів, починаючи з ${selectedDate.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Список товарів",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isAdminMode) {
                    Button(
                        onClick = {
                            editingItem = null
                            dialogProductName = ""
                            dialogProductDays = ""
                            showDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = SimiIcons.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Додати товар")
                    }
                }
            }

            // Products List
            if (localList.isNotEmpty()) {
                MenuContainer {
                    localList.forEachIndexed { index, item ->
                        val expirationDate = selectedDate.plusDays((item.days - 1).coerceAtLeast(0).toLong())
                        val isCurrentlyDragged = draggedIndex == index
                        val offsetModifier = if (isCurrentlyDragged) {
                            Modifier.graphicsLayer {
                                translationY = dragOffsetY
                            }
                        } else {
                            Modifier
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(offsetModifier)
                                .clickable(enabled = isAdminMode) {
                                    editingItem = item
                                    dialogProductName = item.name
                                    dialogProductDays = item.days.toString()
                                    showDialog = true
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isAdminMode) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .pointerInput(index) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggedIndex = index
                                                    dragOffsetY = 0f
                                                },
                                                onDragEnd = {
                                                    if (draggedIndex != null) {
                                                        viewModel.updateList(localList)
                                                        draggedIndex = null
                                                    }
                                                },
                                                onDragCancel = {
                                                    draggedIndex = null
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY += dragAmount.y

                                                    val currentDragIdx = draggedIndex
                                                    if (currentDragIdx != null) {
                                                        val itemHeightPx = 180f

                                                        if (dragOffsetY < -itemHeightPx / 2 && currentDragIdx > 0) {
                                                            val mutable = localList.toMutableList()
                                                            val temp = mutable[currentDragIdx]
                                                            mutable[currentDragIdx] = mutable[currentDragIdx - 1]
                                                            mutable[currentDragIdx - 1] = temp
                                                            localList = mutable
                                                            draggedIndex = currentDragIdx - 1
                                                            dragOffsetY += itemHeightPx
                                                        } else if (dragOffsetY > itemHeightPx / 2 && currentDragIdx < localList.size - 1) {
                                                            val mutable = localList.toMutableList()
                                                            val temp = mutable[currentDragIdx]
                                                            mutable[currentDragIdx] = mutable[currentDragIdx + 1]
                                                            mutable[currentDragIdx + 1] = temp
                                                            localList = mutable
                                                            draggedIndex = currentDragIdx + 1
                                                            dragOffsetY -= itemHeightPx
                                                        }
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Menu,
                                        contentDescription = "Перетягнути",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                if (isAdminMode) {
                                    IconButton(
                                        onClick = {
                                            viewModel.removeItem(item.id)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = SimiIcons.Delete,
                                            contentDescription = "Видалити",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        if (index < localList.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                if (isAdminMode) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset Button
                    TextButton(
                        onClick = {
                            viewModel.resetToDefaults()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Скинути список до початкового", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Список товарів порожній",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isAdminMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.resetToDefaults() }) {
                                Text("Відновити стандартні товари")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for adding / editing items
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingItem == null) "Додати товар для дефросту" else "Редагувати товар") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dialogProductName,
                        onValueChange = { dialogProductName = it },
                        label = { Text("Назва товару") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dialogProductDays,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                dialogProductDays = input
                            }
                        },
                        label = { Text("Термін дефростації (діб)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = dialogProductDays.toIntOrNull()
                        if (dialogProductName.isNotBlank() && days != null && days > 0) {
                            if (editingItem == null) {
                                viewModel.addItem(dialogProductName.trim(), days)
                            } else {
                                viewModel.updateItem(editingItem!!.id, dialogProductName.trim(), days)
                            }
                            showDialog = false
                        }
                    },
                    enabled = dialogProductName.isNotBlank() && dialogProductDays.isNotBlank() && (dialogProductDays.toIntOrNull() ?: 0) > 0
                ) {
                    Text(if (editingItem == null) "Додати" else "Зберегти")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }

    // Date picker for selecting the start date
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Скасувати")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
