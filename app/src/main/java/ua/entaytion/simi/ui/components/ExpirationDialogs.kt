package ua.entaytion.simi.ui.components

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import ua.entaytion.simi.data.model.ExpirationThreat
import ua.entaytion.simi.utils.ProductDictionary
import ua.entaytion.simi.utils.ProductMatrix
import ua.entaytion.simi.viewmodel.ExpirationThreatViewModel
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun copyUriToCache(context: android.content.Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("copied_proof_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }
        Uri.fromFile(tempFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
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
                Icon(imageVector = SimiIcons.Camera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Камера")
            }
        },
        dismissButton = {
            TextButton(onClick = onGallery) {
                Icon(imageVector = SimiIcons.Gallery, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Галерея")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    viewModel: ExpirationThreatViewModel,
    onDismiss: () -> Unit,
    onAdd: (String, ProductMatrix, LocalDate, List<Uri>) -> Unit
) {
    var isManualMode by remember { mutableStateOf(true) }

    // Form State
    var name by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf(LocalDate.now().plusDays(3)) }
    var selectedMatrix by remember { mutableStateOf(ProductMatrix.FRESH) }
    val selectedImages = remember { mutableStateListOf<Uri>() }

    // AI Mock State
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    var showDateSuggestions by remember { mutableStateOf<List<LocalDate>?>(null) }
    
    // Photo source selection
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUriForAdd by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            val cachedUris = uris.mapNotNull { copyUriToCache(context, it) }
            selectedImages.addAll(cachedUris)
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempPhotoUriForAdd?.let { uri ->
                    copyUriToCache(context, uri)?.let { selectedImages.add(it) }
                }
            }
        }
    )

    fun launchCamera() {
        val file = File.createTempFile("proof_add_", ".jpg", context.externalCacheDir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempPhotoUriForAdd = uri
        cameraLauncher.launch(uri)
    }

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
                        Icon(imageVector = SimiIcons.Close, contentDescription = "Close")
                    }
                    Text("Додати товар", style = MaterialTheme.typography.titleLarge)
                    TextButton(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onAdd(name, selectedMatrix, expirationDate, selectedImages.toList())
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
                var showProhibitedDialog by remember { mutableStateOf(false) }

                // When analysis completes, update form
                LaunchedEffect(analyzedProduct) {
                    analyzedProduct?.let {
                        if (it.matrix == ProductMatrix.PROHIBITED) {
                            showProhibitedDialog = true
                            viewModel.clearAnalyzedResult()
                            return@LaunchedEffect
                        }

                        name = it.name
                        selectedMatrix = it.matrix

                        if (it.dates.size > 1) {
                            showDateSuggestions = it.dates
                        } else if (it.dates.size == 1) {
                            expirationDate = it.dates[0]
                            isManualMode = true
                            viewModel.clearAnalyzedResult()
                        } else {
                            isManualMode = true
                            viewModel.clearAnalyzedResult()
                        }
                    }
                }

                if (showProhibitedDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showProhibitedDialog = false
                            isManualMode = true
                        },
                        title = { Text("Заборонений товар") },
                        text = { Text("Вибачте, на цей товар не діє протермін.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showProhibitedDialog = false
                                isManualMode = true
                            }) {
                                Text("Зрозуміло")
                            }
                        }
                    )
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
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Назва товару") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                val guess = ProductDictionary.guessCategory(name)
                                if (guess != null) {
                                    Icon(imageVector = SimiIcons.AI, contentDescription = "AI Detected", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )

                        DatePickerField(
                            date = expirationDate,
                            onDateSelected = { expirationDate = it }
                        )

                        Text("Фото-докази товару", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            for (uri in selectedImages) {
                                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImages.removeIf { it == uri } },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(imageVector = SimiIcons.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            
                            Card(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable { showPhotoSourceDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = SimiIcons.Add, contentDescription = "Add Photo", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Text("Категорія (Матриця)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                             ProductMatrix.values().filter { it != ProductMatrix.PROHIBITED }.forEach { matrix ->
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
                            val contextForAI = LocalContext.current
                            val aiCameraLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.TakePicture(),
                                onResult = { success ->
                                    if (success) {
                                        tempPhotoUri?.let { uri ->
                                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(contextForAI.contentResolver, uri))
                                            } else {
                                                @Suppress("DEPRECATION")
                                                android.provider.MediaStore.Images.Media.getBitmap(contextForAI.contentResolver, uri)
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
                                            android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(contextForAI.contentResolver, it))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            android.provider.MediaStore.Images.Media.getBitmap(contextForAI.contentResolver, it)
                                        }
                                        viewModel.analyzeImage(bitmap)
                                    }
                                }
                            )

                            fun launchAiCamera() {
                                val file = File.createTempFile("ai_ref_", ".jpg", contextForAI.externalCacheDir)
                                val uri = FileProvider.getUriForFile(contextForAI, "${contextForAI.packageName}.fileprovider", file)
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
                                    Icon(imageVector = SimiIcons.Camera, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Камера")
                                }

                                OutlinedButton(
                                    onClick = { aiGalleryLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = SimiIcons.Gallery, contentDescription = null)
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

    if (showPhotoSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onCamera = {
                launchCamera()
                showPhotoSourceDialog = false
            },
            onGallery = {
                galleryLauncher.launch("image/*")
                showPhotoSourceDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditThreatDialog(
    threat: ExpirationThreat,
    onDismiss: () -> Unit,
    onUpdate: (ExpirationThreat, List<Uri>) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(threat.name) }
    var selectedMatrix by remember { mutableStateOf(threat.matrix) }
    val itemDate = java.time.Instant.ofEpochMilli(threat.expirationDate)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    var expirationDate by remember { mutableStateOf(itemDate) }

    val serverImages = remember { mutableStateListOf<String>().apply { addAll(threat.proofImageUrls) } }
    val newImages = remember { mutableStateListOf<Uri>() }
    
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUriForEdit by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            val cachedUris = uris.mapNotNull { copyUriToCache(context, it) }
            newImages.addAll(cachedUris)
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempPhotoUriForEdit?.let { uri ->
                    copyUriToCache(context, uri)?.let { newImages.add(it) }
                }
            }
        }
    )

    fun launchCamera() {
        val file = File.createTempFile("proof_edit_", ".jpg", context.externalCacheDir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempPhotoUriForEdit = uri
        cameraLauncher.launch(uri)
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = SimiIcons.Close, contentDescription = "Close")
                    }
                    Text("Редагувати", style = MaterialTheme.typography.titleLarge)
                    TextButton(
                        onClick = {
                            val timestamp = expirationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            onUpdate(threat.copy(
                                name = name,
                                matrix = selectedMatrix,
                                expirationDate = timestamp,
                                proofImageUrls = serverImages.toList()
                            ), newImages.toList())
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

                    Text("Зображення товару", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // Server Images
                        for (url in serverImages) {
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { serverImages.removeIf { it == url } },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(imageVector = SimiIcons.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        // New Local Images
                        for (uri in newImages) {
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { newImages.removeIf { it == uri } },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(imageVector = SimiIcons.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        
                        Card(
                            modifier = Modifier
                                    .size(80.dp)
                                    .clickable { showPhotoSourceDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(imageVector = SimiIcons.Add, contentDescription = "Add Photo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Text("Категорія (Матриця)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProductMatrix.values().filter { it != ProductMatrix.PROHIBITED }.forEach { matrix ->
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
                        Icon(imageVector = SimiIcons.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Видалити запис")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showPhotoSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onCamera = {
                launchCamera()
                showPhotoSourceDialog = false
            },
            onGallery = {
                galleryLauncher.launch("image/*")
                showPhotoSourceDialog = false
            }
        )
    }
}

@Composable
fun PhotoSourceDialog(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Додати фото товару") },
        text = { Text("Оберіть джерело фотографії:") },
        confirmButton = {
            TextButton(onClick = onCamera) {
                Icon(imageVector = SimiIcons.Camera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Камера")
            }
        },
        dismissButton = {
            TextButton(onClick = onGallery) {
                Icon(imageVector = SimiIcons.Gallery, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Галерея")
            }
        }
    )
}

@Composable
fun GalleryDialog(urls: List<String>, onDismiss: () -> Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = urls[currentIndex],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                ) {
                    Icon(imageVector = SimiIcons.Close, contentDescription = "Close", tint = Color.White)
                }
                
                if (urls.size > 1) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0
                        ) {
                            Icon(
                                imageVector = SimiIcons.Back,
                                contentDescription = "Prev",
                                tint = if (currentIndex > 0) Color.White else Color.Gray
                            )
                        }
                        
                        Text(
                            text = "${currentIndex + 1} / ${urls.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        IconButton(
                            onClick = { if (currentIndex < urls.size - 1) currentIndex++ },
                            enabled = currentIndex < urls.size - 1
                        ) {
                            Icon(
                                imageVector = SimiIcons.Forward,
                                contentDescription = "Next",
                                tint = if (currentIndex < urls.size - 1) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
