package ua.entaytion.simi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ua.entaytion.simi.utils.ProductMatrix
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ua.entaytion.simi.data.model.ExpirationThreat
import ua.entaytion.simi.utils.ExpirationUtils
import androidx.compose.ui.layout.ContentScale

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp),
        shape = RoundedCornerShape(50)
    ) {
        Text(text)
    }
}

@Composable
fun PhotoPlaceholder(label: String, isTaken: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isTaken) {
                    Icon(
                        imageVector = SimiIcons.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = SimiIcons.Camera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isTaken) "Готово" else label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(date: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    var showModal by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showModal) {
        DatePickerDialog(
            onDismissRequest = { showModal = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedDate = java.time.Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    showModal = false
                }) {
                    Text("Вибрати")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Скасувати")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
        onValueChange = { },
        label = { Text("Дата спливання") },
        modifier = Modifier.fillMaxWidth().clickable { showModal = true },
        enabled = false,
        trailingIcon = {
            IconButton(onClick = { showModal = true }) {
                Icon(imageVector = SimiIcons.Calendar, contentDescription = "Pick Date")
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

fun matrixLabel(matrix: ProductMatrix): String = when(matrix) {
    ProductMatrix.FRESH -> "Фреш"
    ProductMatrix.NON_FRESH_SHORT -> "Non-Fresh (Короткий)"
    ProductMatrix.NON_FRESH_MEDIUM -> "Non-Fresh (Середній)"
    ProductMatrix.NON_FRESH_LONG -> "Non-Fresh (Довгий)"
    ProductMatrix.PROHIBITED -> "Заборонені товари"
}

fun matrixDesc(matrix: ProductMatrix): String = when(matrix) {
    ProductMatrix.FRESH -> "Молочка, м'ясо, ковбаса, риба..."
    ProductMatrix.NON_FRESH_SHORT -> "Хліб, лаваш, булки..."
    ProductMatrix.NON_FRESH_MEDIUM -> "Сухарики, чіпси, пиво..."
    ProductMatrix.NON_FRESH_LONG -> "Шоколад, вода, крупи, консерви..."
    ProductMatrix.PROHIBITED -> "Горілка, тютюн, підакцизні товари."
}

@Composable
fun MatrixOption(matrix: ProductMatrix, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = matrixLabel(matrix), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = matrixDesc(matrix), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ExpirationThreatCard(
    item: ExpirationThreat,
    today: LocalDate,
    onClick: () -> Unit,
    onActionClick: (Int?, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemDate = java.time.Instant.ofEpochMilli(item.expirationDate)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()

    val daysLeft = ExpirationUtils.daysBetween(today, item.expirationDate)
    val discount = ExpirationUtils.discountFor(item.matrix, daysLeft)

    val isActionCurrentApplied = when(discount) {
        10 -> item.isDiscount10Applied
        25 -> item.isDiscount25Applied
        50 -> item.isDiscount50Applied
        else -> false
    }

    val isExpired = daysLeft != null && daysLeft <= 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (item.proofImageUrls.isNotEmpty()) {
                var showGalleryDialog by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                ) {
                    AsyncImage(
                        model = item.proofImageUrls.first(),
                        contentDescription = "Фото товару",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showGalleryDialog = true },
                        contentScale = ContentScale.Crop
                    )
                    
                    if (discount != null && !item.isResolved && !isActionCurrentApplied) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            DiscountBadge(percent = discount)
                        }
                    }
                    
                    if (showGalleryDialog) {
                        GalleryDialog(
                            urls = item.proofImageUrls,
                            onDismiss = { showGalleryDialog = false }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    if (discount != null && !item.isResolved && !isActionCurrentApplied) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            DiscountBadge(percent = discount)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Категорія: ${matrixLabel(item.matrix)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        )
                    }
                }

                val statusText = when {
                    isExpired -> "НЕГАЙНО СПИСАТИ ЦЕЙ ТОВАР"
                    isActionCurrentApplied -> "Відкладено до: ${itemDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
                    else -> "Вжити до: ${itemDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isExpired -> MaterialTheme.colorScheme.error
                        isActionCurrentApplied -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isExpired || isActionCurrentApplied) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isResolved || isActionCurrentApplied) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = SimiIcons.CheckCircle,
                                contentDescription = "Done",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (item.isResolved) "Списано/Вирішено" else "Знижку наклеєно",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        if (item.matrix == ProductMatrix.PROHIBITED || isExpired) {
                            Button(
                                onClick = { onActionClick(null, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = SimiIcons.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Позначити списаним")
                            }
                        } else {
                            Button(
                                onClick = { onActionClick(discount, false) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = SimiIcons.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (discount != null) "Наклеїв знижку -$discount%" else "Вирішено")
                            }
                        }
                    }
                }
            }
        }
    }
}

