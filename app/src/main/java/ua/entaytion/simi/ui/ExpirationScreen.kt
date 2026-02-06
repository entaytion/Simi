package ua.entaytion.simi.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.entaytion.simi.R
import ua.entaytion.simi.data.model.ExpirationThreat
import ua.entaytion.simi.ui.components.DiscountBadge
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.utils.ExpirationUtils
import ua.entaytion.simi.utils.ProductDictionary
import ua.entaytion.simi.utils.ProductMatrix
import ua.entaytion.simi.viewmodel.ExpirationThreatViewModel
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpirationScreen(
    onBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: ExpirationThreatViewModel = viewModel()
) {
    val today = LocalDate.now()
    val threats by viewModel.threats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialogFor by remember { mutableStateOf<ExpirationThreat?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Загроза протерміну") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Додати")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (threats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Список порожній. Додайте товари.", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    MenuContainer {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Товар",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Дія",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        threats.forEachIndexed { index, item ->
                            val itemDate = java.time.Instant.ofEpochMilli(item.expirationDate)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()

                            val daysLeft = ExpirationUtils.daysBetween(
                                today,
                                item.expirationDate
                            )
                            val discount = ExpirationUtils.discountFor(item.matrix, daysLeft)
                            
                            val isActionCurrentApplied = when(discount) {
                                10 -> item.isDiscount10Applied
                                25 -> item.isDiscount25Applied
                                50 -> item.isDiscount50Applied
                                else -> false
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEditDialogFor = item }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    
                                    val isExpired = daysLeft != null && daysLeft <= 0
                                    val statusText = when {
                                        isExpired -> "НЕГАЙНО СПИСАТИ ЦЕЙ ТОВАР"
                                        isActionCurrentApplied -> "Відкладено до: ${itemDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
                                        else -> "Вжити до: ${itemDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
                                    }

                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isExpired -> MaterialTheme.colorScheme.error
                                            isActionCurrentApplied -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "Категорія: ${matrixLabel(item.matrix)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                    )
                                }

                                if (item.isResolved || isActionCurrentApplied) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_checkcircle),
                                            contentDescription = "Done",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                } else {
                                    // Action Button
                                    if (discount != null) {
                                        DiscountBadge(percent = discount)
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    
                                    if (item.matrix == ProductMatrix.PROHIBITED || (daysLeft != null && daysLeft <= 0)) {
                                         IconButton(onClick = { 
                                             viewModel.applyAction(item.id, null, markResolved = true)
                                         }) {
                                             Icon(painterResource(id = R.drawable.ic_checkcircle), "Done", tint = MaterialTheme.colorScheme.error)
                                         }
                                    } else {
                                         IconButton(onClick = { 
                                             viewModel.applyAction(item.id, discount, markResolved = false)
                                         }) {
                                             Icon(
                                                 painter = painterResource(id = R.drawable.ic_checkcircle), 
                                                 contentDescription = "Verify",
                                                 tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                                 modifier = Modifier.size(32.dp)
                                             )
                                         }
                                    }
                                }
                            }

                            if (index < threats.size - 1) {
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
    }

    if (showAddDialog) {
        AddItemDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onAdd = { name, matrix, date ->
                viewModel.addThreat(name, matrix, date)
                showAddDialog = false
            }
        )
    }

    if (showEditDialogFor != null) {
         EditThreatDialog(
             threat = showEditDialogFor!!,
             onDismiss = { showEditDialogFor = null },
             onUpdate = { updated ->
                 viewModel.updateThreat(updated)
                 showEditDialogFor = null
             },
             onDelete = {
                 viewModel.deleteThreat(showEditDialogFor!!.id)
                 showEditDialogFor = null
             }
         )
    }
}

@Composable
fun ProofOptionsDialog(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Підтвердження дії") },
        text = { Text("Щоб відмітити товар як опрацьований, необхідно додати фото-доказ (стікер знижки або списання).") },
        confirmButton = {
            TextButton(onClick = onCamera) {
                Icon(painterResource(id = R.drawable.ic_camera), null)
                Spacer(Modifier.width(8.dp))
                Text("Камера")
            }
        },
        dismissButton = {
            TextButton(onClick = onGallery) {
                Icon(painterResource(id = R.drawable.ic_gallery), null)
                Spacer(Modifier.width(8.dp))
                Text("Галерея")
            }
        }
    )
}


@Composable
fun AddItemDialog(
    viewModel: ExpirationThreatViewModel,
    onDismiss: () -> Unit,
    onAdd: (String, ProductMatrix, LocalDate) -> Unit
) {
    var isManualMode by remember { mutableStateOf(true) }
    
    // Form State
    var name by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf(LocalDate.now().plusDays(3)) }
    var selectedMatrix by remember { mutableStateOf(ProductMatrix.FRESH) }
    
    // AI Mock State
    var isAnalyzing by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showDateSuggestions by remember { mutableStateOf<List<LocalDate>?>(null) }

    // Auto-detect matrix (Manual Mode)
    LaunchedEffect(name) {
        if (isManualMode && name.isNotEmpty()) {
            val guess = ProductDictionary.guessCategory(name)
            if (guess != null) selectedMatrix = guess
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(id = R.drawable.ic_close), contentDescription = "Close")
                    }
                    Text("Додати товар", style = MaterialTheme.typography.titleLarge)
                    TextButton(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onAdd(name, selectedMatrix, expirationDate)
                            }
                        },
                        enabled = name.isNotEmpty()
                    ) {
                        Text("Зберегти")
                    }
                }

                // Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabButton(
                        text = "Вручну",
                        isSelected = isManualMode,
                        modifier = Modifier.weight(1f)
                    ) { isManualMode = true }
                    TabButton(
                        text = "AI Сканер (Beta)",
                        isSelected = !isManualMode,
                        modifier = Modifier.weight(1f)
                    ) { isManualMode = false }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                val analyzedProduct by viewModel.analyzedProduct.collectAsState()
                
                // When analysis completes, update form
                LaunchedEffect(analyzedProduct) {
                    analyzedProduct?.let {
                        name = it.name
                        selectedMatrix = it.matrix
                        
                        if (it.dates.size > 1) {
                            showDateSuggestions = it.dates
                        } else if (it.dates.size == 1) {
                            expirationDate = it.dates[0]
                            isManualMode = true
                            viewModel.clearAnalyzedResult()
                        } else {
                            // No dates found, just switch to manual
                            isManualMode = true
                            viewModel.clearAnalyzedResult()
                        }
                    }
                }

                if (showDateSuggestions != null) {
                    AlertDialog(
                        onDismissRequest = { 
                            showDateSuggestions = null
                            isManualMode = true
                            viewModel.clearAnalyzedResult()
                        },
                        title = { Text("Оберіть вірну дату") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                showDateSuggestions?.forEach { date ->
                                    Button(
                                        onClick = {
                                            expirationDate = date
                                            showDateSuggestions = null
                                            isManualMode = true
                                            viewModel.clearAnalyzedResult()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                    ) {
                                        Text(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                                    }
                                }
                                TextButton(onClick = {
                                    showDateSuggestions = null
                                    isManualMode = true
                                    viewModel.clearAnalyzedResult()
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Ввести вручну")
                                }
                            }
                        },
                        confirmButton = {}
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (isManualMode) {
                        // ... existing manual form content ...
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Назва товару") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                val guess = ProductDictionary.guessCategory(name)
                                if (guess != null) {
                                    Icon(painterResource(id = R.drawable.ic_ai), "AI Detected", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )

                        DatePickerField(
                            date = expirationDate,
                            onDateSelected = { expirationDate = it }
                        )

                        Text("Категорія (Матриця)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                             ProductMatrix.values().forEach { matrix ->
                                 MatrixOption(
                                     matrix = matrix,
                                     isSelected = selectedMatrix == matrix,
                                     onClick = { selectedMatrix = matrix }
                                 )
                             }
                        }
                    } else {
                        // AI UI
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(16.dp))
                                    Text("ШІ аналізує фото...", color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        } else {
                            val context = LocalContext.current
                            val aiCameraLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.TakePicture(),
                                onResult = { success ->
                                    if (success) {
                                        tempPhotoUri?.let { uri ->
                                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, uri))
                                            } else {
                                                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                            }
                                            viewModel.analyzeImage(bitmap)
                                        }
                                    }
                                }
                            )

                            val aiGalleryLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.GetContent(),
                                onResult = { uri ->
                                    uri?.let {
                                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                            android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, it))
                                        } else {
                                            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                                        }
                                        viewModel.analyzeImage(bitmap)
                                    }
                                }
                            )

                            fun launchAiCamera() {
                                val file = File.createTempFile("ai_ref_", ".jpg", context.externalCacheDir)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                tempPhotoUri = uri
                                aiCameraLauncher.launch(uri)
                            }

                            Text(
                                "Зробіть фото упаковки з назвою та датою протерміну. ШІ автоматично заповнить поля.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { launchAiCamera() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(painterResource(id = R.drawable.ic_camera), null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Камера")
                                }
                                
                                OutlinedButton(
                                    onClick = { aiGalleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(painterResource(id = R.drawable.ic_gallery), null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Галерея")
                                }
                            }
                            
                            Spacer(Modifier.height(40.dp))
                            
                            Text(
                                "Порада: найкраще працює якщо на фото чітко видно дату та назву продукту.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun EditThreatDialog(
    threat: ExpirationThreat,
    onDismiss: () -> Unit,
    onUpdate: (ExpirationThreat) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(threat.name) }
    var selectedMatrix by remember { mutableStateOf(threat.matrix) }
    val itemDate = java.time.Instant.ofEpochMilli(threat.expirationDate)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    var expirationDate by remember { mutableStateOf(itemDate) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(id = R.drawable.ic_close), contentDescription = "Close")
                    }
                    Text("Редагувати", style = MaterialTheme.typography.titleLarge)
                    TextButton(
                        onClick = {
                            val timestamp = expirationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            onUpdate(threat.copy(
                                name = name,
                                matrix = selectedMatrix,
                                expirationDate = timestamp
                            ))
                        },
                        enabled = name.isNotEmpty()
                    ) {
                        Text("Оновити")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Назва товару") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        date = expirationDate,
                        onDateSelected = { expirationDate = it }
                    )

                    Text("Категорія (Матриця)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProductMatrix.values().forEach { matrix ->
                            MatrixOption(
                                matrix = matrix,
                                isSelected = selectedMatrix == matrix,
                                onClick = { selectedMatrix = matrix }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(painterResource(id = R.drawable.ic_delete), null)
                        Spacer(Modifier.width(8.dp))
                        Text("Видалити запис")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

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
                        painter = painterResource(id = R.drawable.ic_checkcircle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
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
    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showModal) {
        androidx.compose.material3.DatePickerDialog(
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
            androidx.compose.material3.DatePicker(state = datePickerState)
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
                Icon(painterResource(id = R.drawable.ic_calendar), "Pick Date")
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

// Private helpers to avoid conflicts
private fun matrixLabel(matrix: ProductMatrix): String = when(matrix) {
    ProductMatrix.FRESH -> "Фреш"
    ProductMatrix.NON_FRESH_SHORT -> "Non-Fresh (Короткий)"
    ProductMatrix.NON_FRESH_MEDIUM -> "Non-Fresh (Середній)"
    ProductMatrix.NON_FRESH_LONG -> "Non-Fresh (Довгий)"
    ProductMatrix.PROHIBITED -> "Заборонені товари"
}

private fun matrixDesc(matrix: ProductMatrix): String = when(matrix) {
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
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
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
