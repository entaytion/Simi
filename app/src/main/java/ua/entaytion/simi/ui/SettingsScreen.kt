package ua.entaytion.simi.ui

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.database.FirebaseDatabase
import ua.entaytion.simi.data.model.UserMode
import ua.entaytion.simi.viewmodel.SettingsViewModel
import ua.entaytion.simi.ui.components.MenuContainer
import ua.entaytion.simi.ui.components.MenuRow
import ua.entaytion.simi.ui.components.SimiIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
    onOpenAdminPanel: () -> Unit
) {
    val state by viewModel.settingsState.collectAsState()

    var debugClickCount by remember { mutableIntStateOf(0) }

    Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                        title = {
                            Text(
                                    "Налаштування",
                                    modifier =
                                            Modifier.clickable(
                                                            interactionSource =
                                                                    remember {
                                                                        MutableInteractionSource()
                                                                    },
                                                            indication = null
                                                    ) {
                                                debugClickCount++
                                                if (debugClickCount >= 5) {
                                                    onOpenAdminPanel()
                                                    debugClickCount = 0
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
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state?.let { currentState ->
                
                // Theme
                MenuContainer {
                    val isDark = currentState.isDarkTheme
                    MenuRow(
                        title = "Темна тема",
                        icon = if (isDark) SimiIcons.DarkMode else SimiIcons.LightMode,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setDarkTheme(!isDark) },
                        endContent = {
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.setDarkTheme(it) }
                            )
                        }
                    )
                }

                Text(
                    text = "Режим користувача",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )

                // User Mode Options
                MenuContainer {
                    val modes = listOf(
                        Triple(UserMode.NEWBIE, "Новачок", "Каса, Чек-ліст, Донати, Хот-доги"),
                        Triple(UserMode.EXPERIENCED, "Досвідчений", "Загроза протерміну, Каса, Нагадування протерміну"),
                        Triple(UserMode.INDIFFERENT, "Мені байдуже", "Всі функції доступні")
                    )

                    modes.forEachIndexed { index, (mode, title, desc) ->
                        val isSelected = currentState.userMode == mode
                        MenuRow(
                            title = title,
                            icon = SimiIcons.Person,
                            iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { viewModel.setUserMode(mode) },
                            endContent = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = SimiIcons.Ok,
                                        contentDescription = "Вибрано",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                        if (index < modes.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                // Description for selected mode
                val selectedDesc = when(currentState.userMode) {
                    UserMode.NEWBIE -> "Режим для новачків: фокусуємось на основних операціях."
                    UserMode.EXPERIENCED -> "Режим для досвідчених: контроль термінів та каси."
                    UserMode.INDIFFERENT -> "Повний доступ до всіх функцій."
                }
                
                Text(
                    text = selectedDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Магазин Simi",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )

                var showStoreDialog by remember { mutableStateOf(false) }

                MenuContainer {
                    MenuRow(
                        title = currentState.selectedStoreName.ifEmpty { "Оберіть магазин" },
                        icon = SimiIcons.Store,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { showStoreDialog = true },
                        endContent = {
                            Text(
                                text = "Код: ${currentState.selectedStoreId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }

                if (showStoreDialog) {
                    var searchQuery by remember { mutableStateOf("") }
                    var isLoadingStores by remember { mutableStateOf(true) }
                    var storesList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                val allFetched = mutableListOf<Pair<String, String>>()
                                var currentPage = 1
                                var totalPages = 1
                                do {
                                    val url = java.net.URL("https://tosim.sim23.ua/api/v1/stores?page=$currentPage&limit=100")
                                    val connection = url.openConnection() as java.net.HttpURLConnection
                                    connection.requestMethod = "GET"
                                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
                                    connection.connectTimeout = 10000
                                    connection.readTimeout = 10000
                                    
                                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                                    val jsonObject = com.google.gson.JsonParser.parseString(responseText).asJsonObject
                                    totalPages = jsonObject.get("pages").asInt
                                    
                                    val dataArray = jsonObject.getAsJsonArray("data")
                                    for (i in 0 until dataArray.size()) {
                                        val storeObj = dataArray.get(i).asJsonObject
                                        val shopCodeElement = storeObj.get("shop_code")
                                        val shopCode = if (shopCodeElement.isJsonPrimitive && shopCodeElement.asJsonPrimitive.isNumber) {
                                            shopCodeElement.asInt.toString()
                                        } else {
                                            shopCodeElement.asString
                                        }
                                        val address = storeObj.get("address").asString
                                        val cityObj = storeObj.getAsJsonObject("city")
                                        val cityName = cityObj?.get("title")?.asString ?: ""
                                        
                                        val displayName = if (cityName.isNotEmpty() && !address.startsWith(cityName)) {
                                            "$cityName, $address"
                                        } else {
                                            address
                                        }
                                        allFetched.add(shopCode to "$shopCode $displayName")
                                    }
                                    currentPage++
                                } while (currentPage <= totalPages)
                                
                                storesList = allFetched
                                isLoadingStores = false
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Fallback static list in case of network issues
                                storesList = listOf(
                                    "6102" to "6102 Хмельницький, Львівське шосе, 21/2",
                                    "6106" to "6106 Соборна 69"
                                )
                                isLoadingStores = false
                            }
                        }
                    }

                    AlertDialog(
                        onDismissRequest = { showStoreDialog = false },
                        title = { Text("Оберіть магазин Simi") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Пошук за адресою чи кодом") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (isLoadingStores) {
                                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                } else {
                                    val filtered = storesList.filter {
                                        it.second.contains(searchQuery, ignoreCase = true)
                                    }
                                    Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            filtered.forEach { (storeId, storeName) ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.setSelectedStore(storeId, storeName)
                                                            showStoreDialog = false
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (currentState.selectedStoreId == storeId)
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        else
                                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                ) {
                                                    Text(
                                                        text = storeName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(12.dp),
                                                        fontWeight = if (currentState.selectedStoreId == storeId) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showStoreDialog = false }) {
                                Text("Скасувати")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Випічка",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )

                MenuContainer {
                    val showAll = currentState.showBakingAll
                    MenuRow(
                        title = "Показувати всю випічку відразу",
                        icon = SimiIcons.Baking,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setShowBakingAll(!showAll) },
                        endContent = {
                            Switch(
                                checked = showAll,
                                onCheckedChange = { viewModel.setShowBakingAll(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Про додаток",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )

                MenuContainer {
                    val context = LocalContext.current
                    val appVersion = ua.entaytion.simi.BuildConfig.VERSION_NAME
                    MenuRow(
                        title = "Автор",
                        icon = SimiIcons.Person,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://t.me/entaytion")
                            )
                            context.startActivity(intent)
                        },
                        endContent = {
                            Text(
                                text = "@entaytion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    MenuRow(
                        title = "Версія",
                        icon = SimiIcons.Settings,
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = {},
                        endContent = {
                            Text(
                                text = appVersion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}
