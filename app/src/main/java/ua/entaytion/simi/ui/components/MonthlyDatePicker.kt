package ua.entaytion.simi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class MonthlyDatePickerState(val selectedDate: LocalDate, val visibleMonth: YearMonth)

@Composable
fun MonthlyDatePicker(
        state: MonthlyDatePickerState,
        minDate: LocalDate = LocalDate.now(),
        onPreviousMonth: () -> Unit,
        onNextMonth: () -> Unit,
        onDateSelected: (LocalDate) -> Unit,
        modifier: Modifier = Modifier
) {
    val locale = remember { java.util.Locale.Builder().setLanguage("uk").setRegion("UA").build() }
    val headerFormatter = remember { DateTimeFormatter.ofPattern("LLLL yyyy", locale) }

    val monthTitle =
            headerFormatter.format(state.visibleMonth.atDay(1)).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
            }

    val minMonth = YearMonth.from(minDate)
    val canGoPrevious = state.visibleMonth.isAfter(minMonth)

    Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth, enabled = canGoPrevious) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Попередній місяць",
                            modifier = Modifier.alpha(if (canGoPrevious) 1f else 0.35f)
                    )
                }

                Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Наступний місяць"
                    )
                }
            }

            WeekdayHeader()

            MonthGrid(
                    visibleMonth = state.visibleMonth,
                    selectedDate = state.selectedDate,
                    minDate = minDate,
                    onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Text(
                    text = label,
                    modifier = Modifier.weight(1f).padding(vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthGrid(
        visibleMonth: YearMonth,
        selectedDate: LocalDate,
        minDate: LocalDate,
        onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val cells42 = remember(visibleMonth) { buildMonthCells42(visibleMonth) }

    val visibleCells =
            remember(cells42) {
                val lastWeek = cells42.takeLast(7)
                if (lastWeek.all { !it.inMonth }) cells42.dropLast(7) else cells42
            }

    val weeksCount = visibleCells.size / 7

    val from = if (selectedDate.isAfter(today)) today else selectedDate
    val to = if (selectedDate.isAfter(today)) selectedDate else today

    // Один червоний колір для старту/фінішу
    val markerColor = MaterialTheme.colorScheme.error
    val rangeColor = MaterialTheme.colorScheme.error

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (week in 0 until weeksCount) {
            val weekCells = (0 until 7).map { dow -> visibleCells[week * 7 + dow] }

            // знайти сегмент діапазону у цьому тижні
            val segmentIndices =
                    weekCells.mapIndexedNotNull { idx, cell ->
                        val isDisabled = cell.date.isBefore(minDate)
                        val inRange =
                                !isDisabled && !cell.date.isBefore(from) && !cell.date.isAfter(to)
                        if (inRange) idx else null
                    }
            val segStart = segmentIndices.minOrNull()
            val segEnd = segmentIndices.maxOrNull()

            Box(modifier = Modifier.fillMaxWidth()) {
                // лінія/плашка діапазону позаду клітинок (підсвічує шлях)
                if (segStart != null && segEnd != null) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val spacing = 6.dp.toPx()
                        val cellW = (size.width - spacing * 6f) / 7f
                        val barH = 20.dp.toPx()
                        val y = size.height / 2f - barH / 2f
                        val x0 = segStart * (cellW + spacing)
                        val x1 = segEnd * (cellW + spacing) + cellW

                        drawRoundRect(
                                color = rangeColor.copy(alpha = 0.35f),
                                topLeft = Offset(x0 + 1.dp.toPx(), y),
                                size = Size((x1 - x0) - 2.dp.toPx(), barH),
                                cornerRadius = CornerRadius(barH / 2f, barH / 2f)
                        )
                    }
                }

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (dow in 0 until 7) {
                        val cell = weekCells[dow]
                        val isDisabled = cell.date.isBefore(minDate)
                        val inRange =
                                !isDisabled && !cell.date.isBefore(from) && !cell.date.isAfter(to)

                        DayCell(
                                date = cell.date,
                                isInMonth = cell.inMonth,
                                isDisabled = isDisabled,
                                isToday = cell.date == today,
                                isTarget = cell.date == selectedDate,
                                inRange = inRange,
                                markerColor = markerColor,
                                rangeColor = rangeColor,
                                onClick = { if (!isDisabled) onDateSelected(cell.date) },
                                modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private data class MonthCell(val date: LocalDate, val inMonth: Boolean)

private fun buildMonthCells42(month: YearMonth): List<MonthCell> {
    val first = month.atDay(1)
    val firstDow = first.dayOfWeek.mondayBasedIndex() // 0..6, Monday=0
    val start = first.minusDays(firstDow.toLong())

    return (0 until 42).map { i ->
        val d = start.plusDays(i.toLong())
        MonthCell(date = d, inMonth = YearMonth.from(d) == month)
    }
}

private fun DayOfWeek.mondayBasedIndex(): Int =
        when (this) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }

@Composable
private fun DayCell(
        date: LocalDate,
        isInMonth: Boolean,
        isDisabled: Boolean,
        isToday: Boolean,
        isTarget: Boolean,
        inRange: Boolean,
        markerColor: Color,
        rangeColor: Color,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val baseText =
            when {
                isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                !isInMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.onSurface
            }

    // today повинен підсвічуватись завжди, навіть коли обрано іншу дату
    // діапазон показуємо лінією на рівні тижня (див. MonthGrid), тут лишаємо маркери today/selected

    val cellColor =
            when {
                isTarget && !isDisabled -> markerColor
                isToday && !isDisabled -> markerColor
                else -> Color.Transparent
            }

    androidx.compose.material3.Surface(
            modifier =
                    modifier.aspectRatio(1f)
                            .padding(4.dp) // Зменшуємо круг за запитом
                            .clip(CircleShape)
                            .clickable(enabled = !isDisabled, onClick = onClick),
            color = cellColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            val finalTextColor =
                    when {
                        isTarget && !isDisabled -> Color.White
                        isToday && !isDisabled -> Color.White
                        else -> baseText
                    }

            Text(
                    text = date.dayOfMonth.toString(),
                    style =
                            MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight =
                                            if (isTarget || isToday) FontWeight.Bold
                                            else FontWeight.Normal
                            ),
                    color = finalTextColor,
                    textAlign = TextAlign.Center
            )
        }
    }
}
