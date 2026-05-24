package ua.entaytion.simi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ua.entaytion.simi.data.model.ExpirationThreat
import ua.entaytion.simi.ui.components.*
import ua.entaytion.simi.utils.ExpirationUtils
import ua.entaytion.simi.utils.ProductMatrix
import ua.entaytion.simi.viewmodel.ExpirationThreatViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpirationScreen(
    onBack: () -> Unit,
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
                            imageVector = SimiIcons.Back,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = SimiIcons.Add, contentDescription = "Додати")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }

            if (threats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Список порожній. Додайте товари.", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    threats.forEach { item ->
                        ExpirationThreatCard(
                            item = item,
                            today = today,
                            onClick = { showEditDialogFor = item },
                            onActionClick = { discount, markResolved ->
                                viewModel.applyAction(item.id, discount, markResolved)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onAdd = { name, matrix, date, imageUris ->
                viewModel.addThreat(name, matrix, date, imageUris, context)
                showAddDialog = false
            }
        )
    }

    if (showEditDialogFor != null) {
         EditThreatDialog(
             threat = showEditDialogFor!!,
             onDismiss = { showEditDialogFor = null },
             onUpdate = { updated, newImageUris ->
                 viewModel.updateThreat(updated, newImageUris, context)
                 showEditDialogFor = null
             },
             onDelete = {
                 viewModel.deleteThreat(showEditDialogFor!!.id)
                 showEditDialogFor = null
             }
         )
    }
}
