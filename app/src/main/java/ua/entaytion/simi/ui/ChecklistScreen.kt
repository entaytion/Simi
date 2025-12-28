package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
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
                                        contentDescription = "Перемкнути тему",
                                        tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { dialogState = ChecklistDialogState.Add }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Додати")
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
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Редагувати")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Видалити")
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
