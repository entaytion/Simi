package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ua.entaytion.simi.R
import ua.entaytion.simi.utils.ExpirationUtils
import ua.entaytion.simi.utils.ProductType
import java.time.YearMonth
import ua.entaytion.simi.ui.components.MonthlyDatePicker
import ua.entaytion.simi.ui.components.MonthlyDatePickerState
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpirationScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val today = LocalDate.now()
    var productType by remember { mutableStateOf(ProductType.FAST_PERISHABLE) }

    var selectedDate by remember { mutableStateOf(today) }
    var visibleMonth by remember { mutableStateOf(YearMonth.from(today)) }

    val locale = remember { java.util.Locale.Builder().setLanguage("uk").setRegion("UA").build() }
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", locale) }

    val daysLeft = ExpirationUtils.daysBetween(today, selectedDate.atStartOfDayMillis())
    val discount = ExpirationUtils.discountFor(productType, daysLeft)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Загроза протерміну") },
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
                            imageVector = if (isDarkTheme) Icons.Filled.Brightness4 else Icons.Filled.Brightness7,
                            contentDescription = "Перемкнути тему",
                            tint = MaterialTheme.colorScheme.onSurface
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                TypeChipsGroup(
                    selected = productType,
                    onSelect = { productType = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            MonthlyDatePicker(
                state = MonthlyDatePickerState(
                    selectedDate = selectedDate,
                    visibleMonth = visibleMonth
                ),
                minDate = today,
                onPreviousMonth = {
                    // додатковий захист (хоча компонент і сам заблокує кнопку)
                    val minMonth = java.time.YearMonth.from(today)
                    if (visibleMonth.isAfter(minMonth)) visibleMonth = visibleMonth.minusMonths(1)
                },
                onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) },
                onDateSelected = { date ->
                    selectedDate = date
                    visibleMonth = java.time.YearMonth.from(date)
                },
                modifier = Modifier.fillMaxWidth()
            )


            val resultText = discount?.let { "Ставте цінник $it%" } ?: "Ок, ще не час списувати"
            val resultColor = discount?.let { discountColor(it) } ?: MaterialTheme.colorScheme.secondary

            val summaryItems = listOf(
                SummaryItem(
                    icon = Icons.Filled.Event,
                    title = "Днів залишилось",
                    value = daysLeft?.let { "$it" } ?: "—",
                    valueColor = Color(0xFF64B5F6),
                    emphasize = true
                ),
                SummaryItem(
                    icon = if (discount != null) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                    title = if (discount != null) "Рекомендована дія" else "Все гаразд",
                    value = resultText,
                    valueColor = resultColor,
                    emphasize = true
                )
            )

            SummaryCard(items = summaryItems)
        }
    }
}

private data class TypeChipData(val type: ProductType, val icon: Int, val label: String)

@Composable
private fun TypeChipsGroup(
    selected: ProductType,
    onSelect: (ProductType) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        TypeChipData(ProductType.FAST_PERISHABLE, R.drawable.ic_dairy, "Швидкопсувні"),
        TypeChipData(ProductType.SLOW_PERISHABLE, R.drawable.ic_box, "Повільнопсувні"),
        TypeChipData(ProductType.CHIPS, R.drawable.ic_chips, "До 4 місяців")
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { opt ->
                val isSelected = selected == opt.type
                Surface(
                    onClick = { onSelect(opt.type) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(opt.icon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = opt.label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(data: TypeChipData, isSelected: Boolean, modifier: Modifier = Modifier, onSelect: () -> Unit) {
    Surface(
        onClick = onSelect,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    painter = painterResource(data.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                )
            }
            Text(text = data.label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SummaryCard(items: List<SummaryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { SummaryRow(it) }
        }
    }
}

private data class SummaryItem(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val valueColor: Color? = null,
    val emphasize: Boolean = false
)

@Composable
private fun SummaryRow(item: SummaryItem) {
    val accentColor = item.valueColor ?: MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.25f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = accentColor
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (item.emphasize) accentColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun LocalDate.atStartOfDayMillis(): Long =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun discountColor(percent: Int?): Color = when (percent) {
    10 -> Color(0xFF4CAF50)
    25 -> Color(0xFFFFC107)
    50 -> Color(0xFFFF9800)
    75 -> Color(0xFFFF6F91)
    else -> Color(0xFF90A4AE)
}
