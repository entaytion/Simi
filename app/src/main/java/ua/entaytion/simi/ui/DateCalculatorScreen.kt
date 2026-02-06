package ua.entaytion.simi.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.entaytion.simi.R
import ua.entaytion.simi.ui.components.MonthlyDatePicker
import ua.entaytion.simi.ui.components.MonthlyDatePickerState
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.ui.components.DiscountBadge
import ua.entaytion.simi.utils.ExpirationUtils
import ua.entaytion.simi.utils.ProductMatrix
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculatorScreen(onBack: () -> Unit) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Калькулятор дат") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(id = R.drawable.ic_back), contentDescription = "Назад")
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
            // Calendar Component
            MonthlyDatePicker(
                state = MonthlyDatePickerState(selectedDate, visibleMonth),
                onPreviousMonth = { visibleMonth = visibleMonth.minusMonths(1) },
                onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) },
                onDateSelected = { selectedDate = it }
            )

            // Result Header
            Text(
                "Якщо товар спливає ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Calculations
            val daysLeft = ExpirationUtils.daysBetween(
                today,
                selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )

            MenuContainer {
                ProductMatrix.values().forEachIndexed { index, matrix ->
                    CalculatorRow(matrix, daysLeft)
                    
                    if (index < ProductMatrix.values().size - 1) {
                         HorizontalDivider(
                             modifier = Modifier.padding(start = 16.dp),
                             color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                         )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorRow(matrix: ProductMatrix, daysLeft: Long?) {
    val discount = ExpirationUtils.discountFor(matrix, daysLeft)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = calculatorMatrixLabel(matrix),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = calculatorMatrixShortDesc(matrix),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (matrix == ProductMatrix.PROHIBITED) {
             Text(
                "ЗАБОРОНЕНО",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
             )
        } else if (discount != null) {
            DiscountBadge(percent = discount)
        } else {
             // If daysLeft is huge, it's "Fresh" / OK. If negative, it's expired.
             // ExpirationUtils logic: if daysLeft <= rule.first, apply rule.
             // If null is returned, it means no rule matched => it's still fresh/ok (assuming daysLeft > max rule days).
             
             if (daysLeft != null && daysLeft < 0) {
                 Text(
                    "OK",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                 )
             } else {
                 Text(
                    "OK",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                 )
             }
        }
    }
}

// Reusing labels slightly modified for table context if needed, or link to existing ones if public
// Assuming logic copied or made public from ExpirationScreen.kt
// Let's redefine short ones here or import if possible. They were private/local in ExpirationScreen? No, top level.
// We can move them to ExpirationUtils or duplicate for now to avoid breaking ExpirationScreen if I touch it.
// Actually, better to move to ExpirationUtils to respect DRY. But for speed and safety, I will duplicate locally or rename.

private fun calculatorMatrixLabel(matrix: ProductMatrix): String = when(matrix) {
    ProductMatrix.FRESH -> "Фреш"
    ProductMatrix.NON_FRESH_SHORT -> "Non-Fresh (Короткий)"
    ProductMatrix.NON_FRESH_MEDIUM -> "Non-Fresh (Середній)"
    ProductMatrix.NON_FRESH_LONG -> "Non-Fresh (Довгий)"
    ProductMatrix.PROHIBITED -> "Горілка, сигарети"
}

private fun calculatorMatrixShortDesc(matrix: ProductMatrix): String = when(matrix) {
    ProductMatrix.FRESH -> "Молочка, м'ясо"
    ProductMatrix.NON_FRESH_SHORT -> "Хліб, булки"
    ProductMatrix.NON_FRESH_MEDIUM -> "Чіпси, пиво"
    ProductMatrix.NON_FRESH_LONG -> "Вода, бакалія"
    ProductMatrix.PROHIBITED -> "Горілка, сигарети"
}
