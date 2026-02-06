package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ua.entaytion.simi.R
import ua.entaytion.simi.data.model.ChecklistItem
import ua.entaytion.simi.viewmodel.ChecklistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
        onBack: () -> Unit,
        viewModel: ChecklistViewModel,
        isDarkTheme: Boolean,
        onToggleTheme: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var dialogState by remember { mutableStateOf<ChecklistDialogState?>(null) }

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                        title = { Text("Чек-ліст") },
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
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { dialogState = ChecklistDialogState.Add }) {
                    Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Додати")
                }
            }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    ChecklistRow(
                            item = item,
                            onCheckedChange = { checked -> viewModel.toggleItem(item.id, checked) },
                            onEdit = { dialogState = ChecklistDialogState.Edit(item) },
                            onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }
    }

    dialogState?.let { dialog ->
        ChecklistItemDialog(
                title = if (dialog is ChecklistDialogState.Add) "Новий пункт" else "Редагування",
                initialValue = if (dialog is ChecklistDialogState.Edit) dialog.item.title else "",
                onConfirm = { text ->
                    when (dialog) {
                        ChecklistDialogState.Add -> viewModel.addItem(text)
                        is ChecklistDialogState.Edit -> viewModel.editItem(dialog.item.id, text)
                    }
                    dialogState = null
                },
                onDismiss = { dialogState = null }
        )
    }
}

@Composable
private fun ChecklistRow(
        item: ChecklistItem,
        onCheckedChange: (Boolean) -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = onCheckedChange)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = item.title,
                        style =
                                MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration =
                                                if (item.isChecked) TextDecoration.LineThrough
                                                else TextDecoration.None
                                )
                )
            }
            IconButton(onClick = onEdit) {
                Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = "Редагувати")
            }
            IconButton(onClick = onDelete) {
                Icon(painter = painterResource(id = R.drawable.ic_delete), contentDescription = "Видалити")
            }
        }
    }
}

@Composable
private fun ChecklistItemDialog(
        title: String,
        initialValue: String,
        onConfirm: (String) -> Unit,
        onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = AlertDialogDefaults.containerColor,
            title = { Text(title, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Опишіть дію") },
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedLabelColor =
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                        cursorColor = MaterialTheme.colorScheme.onSurface
                                )
                )
            },
            confirmButton = {
                TextButton(
                        onClick = { onConfirm(text) },
                        colors =
                                ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                )
                ) { Text("Зберегти") }
            },
            dismissButton = {
                TextButton(
                        onClick = onDismiss,
                        colors =
                                ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                ) { Text("Скасувати") }
            }
    )
}

private sealed interface ChecklistDialogState {
    data object Add : ChecklistDialogState
    data class Edit(val item: ChecklistItem) : ChecklistDialogState
}
