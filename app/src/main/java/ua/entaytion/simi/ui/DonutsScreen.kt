package ua.entaytion.simi.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import java.util.Locale
import ua.entaytion.simi.ui.components.SimiIcons
import ua.entaytion.simi.viewmodel.DonutsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonutsScreen(
        onBack: () -> Unit,
        viewModel: DonutsViewModel
) {
        val maxCountPerItem = viewModel.maxCountPerItem

        val donutNames by viewModel.donutNames.collectAsState()

        var expanded by remember { mutableStateOf(false) }
        var selectedName by remember { mutableStateOf("") }
        val entries = viewModel.entries

        val locale = remember { Locale.Builder().setLanguage("uk").setRegion("UA").build() }
        val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM HH:mm", locale) }

        var isAdminMode by remember { mutableStateOf(false) }
        var titleClickCount by remember { mutableIntStateOf(0) }
        val context = LocalContext.current

        fun upsert(name: String, delta: Int) {
                viewModel.upsert(name, delta)
        }

        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                        CenterAlignedTopAppBar(
                                title = {
                                        Text(
                                                text = "Докласти донати" + if (isAdminMode) " ⚙️" else "",
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
                                                                        if (isAdminMode) "Режим адміна активовано" else "Режим перегляду",
                                                                        Toast.LENGTH_SHORT
                                                                ).show()
                                                        }
                                                }
                                        )
                                },
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
                        modifier =
                                Modifier.padding(innerPadding)
                                        .padding(16.dp)
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors =
                                        CardDefaults.cardColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.surfaceVariant
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
                                                        imageVector = SimiIcons.Donut,
                                                        contentDescription = null,
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                                Text(
                                                        text = "Вибери донат",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
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
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .menuAnchor(),
                                                        label = { Text("Донат") },
                                                        trailingIcon = {
                                                                ExposedDropdownMenuDefaults
                                                                        .TrailingIcon(
                                                                                expanded = expanded
                                                                        )
                                                        },
                                                        colors =
                                                                OutlinedTextFieldDefaults.colors(
                                                                        focusedTextColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface,
                                                                        unfocusedTextColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface,
                                                                        focusedLabelColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface,
                                                                        unfocusedLabelColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurfaceVariant,
                                                                        focusedContainerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surface,
                                                                        unfocusedContainerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surface,
                                                                        focusedBorderColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface,
                                                                        unfocusedBorderColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .outline,
                                                                        cursorColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onSurface
                                                                )
                                                )

                                                ExposedDropdownMenu(
                                                        expanded = expanded,
                                                        onDismissRequest = { expanded = false }
                                                ) {
                                                        donutNames.forEach { name ->
                                                                DropdownMenuItem(
                                                                        text = { Text(name) },
                                                                        onClick = {
                                                                                selectedName = name
                                                                                expanded = false
                                                                                upsert(name, +1)
                                                                        }
                                                                )
                                                        }
                                                }
                                        }

                                        Text(
                                                text =
                                                        "Максимум $maxCountPerItem шт на одну позицію",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                        }

                        if (isAdminMode) {
                                var newDonutName by remember { mutableStateOf("") }
                                var editingDonut by remember { mutableStateOf<String?>(null) }
                                var editDonutNameText by remember { mutableStateOf("") }

                                var localDonutNames by remember(donutNames) { mutableStateOf(donutNames) }
                                var draggedIndex by remember { mutableStateOf<Int?>(null) }
                                var dragOffsetY by remember { mutableStateOf(0f) }

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
                                                        text = "Список в базі (${localDonutNames.size} шт):",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                ua.entaytion.simi.ui.components.MenuContainer {
                                                        localDonutNames.forEachIndexed { index, name ->
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
                                                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
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
                                                                                                                        viewModel.updateDonutNames(localDonutNames)
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
                                                                                                                                val mutable = localDonutNames.toMutableList()
                                                                                                                                val temp = mutable[currentDragIdx]
                                                                                                                                mutable[currentDragIdx] = mutable[currentDragIdx - 1]
                                                                                                                                mutable[currentDragIdx - 1] = temp
                                                                                                                                localDonutNames = mutable
                                                                                                                                draggedIndex = currentDragIdx - 1
                                                                                                                                dragOffsetY += itemHeightPx
                                                                                                                        } else if (dragOffsetY > itemHeightPx / 2 && currentDragIdx < localDonutNames.size - 1) {
                                                                                                                                val mutable = localDonutNames.toMutableList()
                                                                                                                                val temp = mutable[currentDragIdx]
                                                                                                                                mutable[currentDragIdx] = mutable[currentDragIdx + 1]
                                                                                                                                mutable[currentDragIdx + 1] = temp
                                                                                                                                localDonutNames = mutable
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

                                                                if (index < localDonutNames.size - 1) {
                                                                        HorizontalDivider(
                                                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                                        )
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

                        if (entries.isNotEmpty()) {
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.extraLarge,
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant
                                                )
                                ) {
                                        Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(10.dp)
                                                ) {
                                                        Icon(
                                                                imageVector = SimiIcons.Box,
                                                                contentDescription = null,
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                        Text(
                                                                text = "Які донести",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium,
                                                                fontWeight = FontWeight.SemiBold
                                                        )
                                                }

                                                entries.forEach { entry ->
                                                        Surface(
                                                                shape = MaterialTheme.shapes.large,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .surface
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                horizontal =
                                                                                                        14.dp,
                                                                                                vertical =
                                                                                                        12.dp
                                                                                        ),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Icon(
                                                                                imageVector = SimiIcons.Donut,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurfaceVariant,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                22.dp
                                                                                        )
                                                                        )

                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                10.dp
                                                                                        )
                                                                        )

                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                entry.name,
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleSmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .SemiBold,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurface
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "Дата: ${dateFormatter.format(entry.dateTaken)}",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodySmall,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                        }

                                                                        FilledTonalIconButton(
                                                                                onClick = {
                                                                                        upsert(
                                                                                                entry.name,
                                                                                                -1
                                                                                        )
                                                                                }
                                                                        ) {
                                                                                 Icon(
                                                                                         imageVector = SimiIcons.Remove,
                                                                                         contentDescription =
                                                                                                 "Мінус"
                                                                                 )
                                                                        }

                                                                        Box(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                44.dp
                                                                                        ),
                                                                                contentAlignment =
                                                                                        Alignment
                                                                                                .Center
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                entry.count
                                                                                                        .toString(),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleMedium,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurface
                                                                                )
                                                                        }

                                                                        FilledTonalIconButton(
                                                                                onClick = {
                                                                                        upsert(
                                                                                                entry.name,
                                                                                                +1
                                                                                        )
                                                                                },
                                                                                enabled =
                                                                                        entry.count <
                                                                                                maxCountPerItem
                                                                        ) {
                                                                                 Icon(
                                                                                         imageVector = SimiIcons.Add,
                                                                                         contentDescription =
                                                                                                 "Плюс"
                                                                                 )
                                                                        }
                                                                }
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                        text =
                                                                "Підказка: якщо натиснути '-' коли кількість 1 — рядок видалиться.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                }
                        }
                }
        }
}
