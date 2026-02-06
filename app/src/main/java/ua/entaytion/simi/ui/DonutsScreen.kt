package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import java.util.Locale
import ua.entaytion.simi.R
import ua.entaytion.simi.viewmodel.DonutsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonutsScreen(
        onBack: () -> Unit,
        viewModel: DonutsViewModel,
        isDarkTheme: Boolean,
        onToggleTheme: () -> Unit
) {
        val maxCountPerItem = viewModel.maxCountPerItem

        val donutNames = remember {
                listOf(
                                "Срібне диво (марципан)",
                                "Веселих свят (лимон малина)",
                                "Мандарин нектарин",
                                "Pink з полуницею",
                                "Рожевий з маршмелоу",
                                "Cookies",
                                "Конфеті Кенді",
                                "Very cremy berry",
                                "Rafaela",
                                "Капучино",
                                "Панакота",
                                "Яблуко-кориця",
                                "Карамелька",
                                "Потрійний шоколад",
                                "Хелоувін",
                                "Вишневі обійми",
                                "Малинова ніжність",
                                "Love з полуничним смаком",
                                "Берлінер \"Червоне серце\"",
                                "Лісовий горіх",
                                "Вафлі \"Трубочка\"",
                                "Торт \"Вафельний\"",
                                "Мафін \"Тірамісу\"",
                                "Мафін \"Шоколадний\"",
                                "Тістечко еклер зі згущеним молоком",
                                "Тістечко еклер з заварним кремом",
                                "Торт чізкейк \"Нью-Йорк\"",
                                "Десерт \"Чізкейк\" з вишнею сакура",
                                "Десерт \"Чізкейк\" солона карамель"
                        )
                        .distinct()
        }

        var expanded by remember { mutableStateOf(false) }
        var selectedName by remember { mutableStateOf("") }
        val entries = viewModel.entries

        val locale = remember { Locale.Builder().setLanguage("uk").setRegion("UA").build() }
        val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM HH:mm", locale) }

        fun upsert(name: String, delta: Int) {
                viewModel.upsert(name, delta)
        }

        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                        CenterAlignedTopAppBar(
                                title = { Text("Докласти донати") },
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
                                                        painter =
                                                                painterResource(
                                                                        id = R.drawable.ic_donut
                                                                ),
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
                                                                                upsert(
                                                                                        name,
                                                                                        +1
                                                                                ) // вибір = одразу
                                                                                // додаємо 1 і
                                                                                // ставимо дату
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

                        if (entries.isNotEmpty()) {
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
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
                                                                painter = painterResource(id = R.drawable.ic_box),
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
                                                                                painter =
                                                                                        painterResource(
                                                                                                id =
                                                                                                        R.drawable
                                                                                                                .ic_donut
                                                                                        ),
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

                                                                        IconButton(
                                                                                onClick = {
                                                                                        upsert(
                                                                                                entry.name,
                                                                                                -1
                                                                                        )
                                                                                }
                                                                        ) {
                                                                                 Icon(
                                                                                         painter = painterResource(id = R.drawable.ic_remove),
                                                                                         contentDescription =
                                                                                                 "Мінус",
                                                                                         tint =
                                                                                                 MaterialTheme
                                                                                                         .colorScheme
                                                                                                         .onSurface
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

                                                                        IconButton(
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
                                                                                         painter = painterResource(id = R.drawable.ic_add),
                                                                                         contentDescription =
                                                                                                 "Плюс",
                                                                                         tint =
                                                                                                 MaterialTheme
                                                                                                         .colorScheme
                                                                                                         .onSurface
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
