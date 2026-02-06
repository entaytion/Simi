package ua.entaytion.simi

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import ua.entaytion.simi.ui.CashBalanceScreen
import ua.entaytion.simi.ui.ChecklistScreen
import ua.entaytion.simi.ui.DonutsScreen
import ua.entaytion.simi.ui.ExpirationScreen
import ua.entaytion.simi.ui.HomeScreen
import ua.entaytion.simi.ui.HotDogsScreen
import ua.entaytion.simi.ui.theme.SimiTheme
import ua.entaytion.simi.viewmodel.ChecklistViewModel
import ua.entaytion.simi.viewmodel.DonutsViewModel
import ua.entaytion.simi.viewmodel.HotDogsViewModel

class MainActivity : ComponentActivity() {

        private val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                        isGranted: Boolean ->
                        // Handle the result if needed
                }

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                // Request notification permission for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                        this,
                                        Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                        ) {
                                requestPermissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                )
                        }
                }

                setContent {
                        val application = getApplication()
                        val settingsViewModel: ua.entaytion.simi.viewmodel.SettingsViewModel =
                                androidx.lifecycle.viewmodel.compose.viewModel(
                                        factory =
                                                ViewModelProvider.AndroidViewModelFactory
                                                        .getInstance(application)
                                )
                        val settingsState by settingsViewModel.settingsState.collectAsState()

                        val systemDark = isSystemInDarkTheme()
                        val darkTheme = settingsState?.isDarkTheme ?: true

                        SimiTheme(darkTheme = darkTheme, dynamicColor = false) {
                                ApplySystemBars(darkTheme = darkTheme)
                                App(isDarkTheme = darkTheme)
                        }
                }

                scheduleExpirationWorker()
                createNotificationChannel()
                setupFirebaseNotificationListener()
        }

        private fun createNotificationChannel() {
                val channelId = "expiration_channel"
                val channelName = "Нагадування протерміну"
                val channelDescription =
                        "Сповіщення про термін придатності товарів та знижки"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val channel =
                        NotificationChannel(channelId, channelName, importance).apply {
                                description = channelDescription
                                enableLights(true)
                                enableVibration(true)
                        }

                val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as
                                NotificationManager
                notificationManager.createNotificationChannel(channel)
        }

        private fun setupFirebaseNotificationListener() {
                val database =
                        com.google.firebase.database.FirebaseDatabase.getInstance(
                                "https://mrtv-simi-default-rtdb.europe-west1.firebasedatabase.app"
                        )
                val ref = database.getReference("global_notifications")
                val prefs = getSharedPreferences("simi_notifications", Context.MODE_PRIVATE)

                ref.addValueEventListener(
                        object : com.google.firebase.database.ValueEventListener {
                                override fun onDataChange(
                                        snapshot: com.google.firebase.database.DataSnapshot
                                ) {
                                        val sentAt =
                                                snapshot.child("sentAt").getValue(Long::class.java)
                                                        ?: 0L
                                        val lastShownSentAt =
                                                prefs.getLong("last_shown_sent_at", 0L)

                                        // Показувати тільки нові сповіщення
                                        if (sentAt <= lastShownSentAt) {
                                                return
                                        }

                                        val title =
                                                snapshot.child("title").getValue(String::class.java)
                                                        ?: "Simi"
                                        val message =
                                                snapshot.child("message")
                                                        .getValue(String::class.java)
                                                        ?: ""

                                        // Зберегти що це сповіщення вже показано
                                        prefs.edit().putLong("last_shown_sent_at", sentAt).apply()
                                }

                                override fun onCancelled(
                                        error: com.google.firebase.database.DatabaseError
                                ) {
                                        // Ignore
                                }
                        }
                )
        }

        private fun scheduleExpirationWorker() {
                scheduleWorkerAt(7, 0, "expiration_check_morning")
                scheduleWorkerAt(22, 0, "expiration_check_evening")

                // Підписка на глобальні сповіщення
                com.google.firebase.messaging.FirebaseMessaging.getInstance()
                        .subscribeToTopic("simi_all")
        }

        private fun scheduleWorkerAt(hour: Int, minute: Int, uniqueName: String) {
                val workManager = androidx.work.WorkManager.getInstance(applicationContext)

                val now = java.util.Calendar.getInstance()
                val target = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, hour)
                        set(java.util.Calendar.MINUTE, minute)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                }

                if (target.before(now)) {
                        target.add(java.util.Calendar.DAY_OF_YEAR, 1)
                }

                val initialDelay = target.timeInMillis - now.timeInMillis

                val request = androidx.work.PeriodicWorkRequestBuilder<ua.entaytion.simi.worker.ExpirationWorker>(
                        1, java.util.concurrent.TimeUnit.DAYS
                )
                        .setInitialDelay(initialDelay, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .setConstraints(
                                androidx.work.Constraints.Builder()
                                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                                        .build()
                        )
                        .build()

                workManager.enqueueUniquePeriodicWork(
                        uniqueName,
                        androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                        request
                )
        }
}

private enum class Route {
        Home,
        Expiration,
        CashBalance,
        Checklist,
        Donuts,
        HotDogs,
        Settings,
        ExpirationNotifications,
        ExpirationManagement,
        DateCalculator
}

@Composable
private fun ApplySystemBars(darkTheme: Boolean) {
        val view = LocalView.current
        val context = LocalContext.current
        val activity = context as? Activity ?: return
        val window = activity.window
        val statusBarColor = MaterialTheme.colorScheme.background.toArgb()

        SideEffect {
                window.statusBarColor = statusBarColor
                WindowInsetsControllerCompat(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                }
        }
}

@Composable
private fun getApplication(): Application {
        val context = LocalContext.current
        return context.applicationContext as Application
}

@Composable
private fun App(isDarkTheme: Boolean) {
        val application = getApplication()
        val settingsViewModel: ua.entaytion.simi.viewmodel.SettingsViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )
        val settingsState by settingsViewModel.settingsState.collectAsState()
        val userMode = settingsState?.userMode ?: ua.entaytion.simi.data.model.UserMode.NEWBIE

        val backStack =
                rememberSaveable(
                        saver =
                                listSaver(
                                        save = { list -> list.map { it.name } },
                                        restore = { names ->
                                                mutableStateListOf<Route>().apply {
                                                        addAll(
                                                                names.mapNotNull { n ->
                                                                        Route.entries.firstOrNull {
                                                                                it.name == n
                                                                        }
                                                                }
                                                        )
                                                        if (isEmpty()) add(Route.Home)
                                                }
                                        }
                                )
                ) { mutableStateListOf(Route.Home) }

        fun navigate(to: Route) {
                backStack.add(to)
        }

        fun goBack() {
                if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        }

        BackHandler(enabled = backStack.size > 1) { goBack() }

        // Get pending notifications count from ExpirationReminderViewModel
        val expirationViewModel: ua.entaytion.simi.viewmodel.ExpirationReminderViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )
        val reminders by expirationViewModel.reminders.collectAsState()

        // Count items that need attention (pending discounts or expired)
        val today = System.currentTimeMillis()
        val pendingCount =
                reminders.count { item ->
                        if (item.isWrittenOff) return@count false

                        // Check for pending discounts
                        val pendingDiscount10 =
                                !item.isDiscount10Applied &&
                                        item.discount10Date != null &&
                                        today >= item.discount10Date
                        val pendingDiscount25 =
                                !item.isDiscount25Applied &&
                                        item.discount25Date != null &&
                                        today >= item.discount25Date
                        val pendingDiscount50 =
                                !item.isDiscount50Applied &&
                                        item.discount50Date != null &&
                                        today >= item.discount50Date
                        val expired = item.finalDate <= today

                        pendingDiscount10 || pendingDiscount25 || pendingDiscount50 || expired
                }

        when (val route = backStack.last()) {
                Route.Home ->
                        HomeScreen(
                                onOpenExpiration = { navigate(Route.Expiration) },
                                onOpenCashBalance = { navigate(Route.CashBalance) },
                                onOpenChecklist = { navigate(Route.Checklist) },
                                onOpenDonuts = { navigate(Route.Donuts) },
                                onOpenHotDogs = { navigate(Route.HotDogs) },
                                onOpenNotifications = { navigate(Route.ExpirationNotifications) },
                                onOpenSettings = { navigate(Route.Settings) },
                                onOpenDateCalculator = { navigate(Route.DateCalculator) },
                                userMode = userMode,
                                isDarkTheme = isDarkTheme,
                                pendingNotificationsCount = pendingCount
                        )
                Route.Expiration ->
                        ExpirationScreen(
                                onBack = { goBack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { settingsViewModel.setDarkTheme(!isDarkTheme) }
                        )
                Route.ExpirationNotifications -> {
                        ua.entaytion.simi.ui.ExpirationNotificationsScreen(
                                onBack = { goBack() },
                                viewModel = expirationViewModel
                        )
                }
                Route.ExpirationManagement -> {
                        ua.entaytion.simi.ui.ExpirationManagementScreen(
                                onBack = { goBack() },
                                viewModel = expirationViewModel
                        )
                }
                Route.Settings ->
                        ua.entaytion.simi.ui.SettingsScreen(
                                onBack = { goBack() },
                                viewModel = settingsViewModel
                        )
                Route.CashBalance ->
                        CashBalanceScreen(
                                onBack = { goBack() },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { settingsViewModel.setDarkTheme(!isDarkTheme) }
                        )
                Route.Checklist -> {
                        val vm =
                                androidx.lifecycle.viewmodel.compose.viewModel<ChecklistViewModel>(
                                        factory = ChecklistViewModel.factory(LocalContext.current)
                                )
                        ChecklistScreen(
                                onBack = { goBack() },
                                viewModel = vm,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { settingsViewModel.setDarkTheme(!isDarkTheme) }
                        )
                }
                Route.Donuts -> {
                        val vm = androidx.lifecycle.viewmodel.compose.viewModel<DonutsViewModel>()
                        DonutsScreen(
                                onBack = { goBack() },
                                viewModel = vm,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { settingsViewModel.setDarkTheme(!isDarkTheme) }
                        )
                }
                Route.HotDogs -> {
                        val vm = androidx.lifecycle.viewmodel.compose.viewModel<HotDogsViewModel>()
                        HotDogsScreen(
                                onBack = { goBack() },
                                viewModel = vm,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { settingsViewModel.setDarkTheme(!isDarkTheme) }
                        )
                }
                Route.DateCalculator -> {
                        ua.entaytion.simi.ui.DateCalculatorScreen(onBack = { goBack() })
                }
        }
}
