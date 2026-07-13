package ua.entaytion.simi.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import ua.entaytion.simi.R
import ua.entaytion.simi.data.storage.FirebaseExpirationStorage

class ExpirationWorker(context: Context, workerParams: WorkerParameters) :
        CoroutineWorker(context, workerParams) {

        override suspend fun doWork(): Result {
                val storage = FirebaseExpirationStorage(applicationContext)
                val threats = storage.threats.first()

                // Check Threats (from ExpirationScreen)
                threats.forEach { threat ->
                    if (threat.isResolved) return@forEach

                    val threatDate = java.time.Instant.ofEpochMilli(threat.expirationDate)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    val todayDate = java.time.LocalDate.now()
                    
                    val daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(todayDate, threatDate)
                    val discount = ua.entaytion.simi.utils.ExpirationUtils.discountFor(threat.matrix, daysRemaining)

                    if (daysRemaining <= 0) {
                        sendNotification(
                            threat.id.hashCode(),
                            "Протерміновано!",
                            "Товар: ${threat.name}. ТЕРМІНОВО СПИСАТИ!"
                        )
                    } else if (discount != null) {
                        val isApplied = when(discount) {
                            10 -> threat.isDiscount10Applied
                            25 -> threat.isDiscount25Applied
                            50 -> threat.isDiscount50Applied
                            else -> false
                        }
                        
                        if (!isApplied) {
                            sendNotification(
                                threat.id.hashCode(),
                                "Потрібна дія: ${discount}%",
                                "Товар: ${threat.name}. Наклейте знижку ${discount}% або перевірте стан."
                            )
                        }
                    }
                }

                return Result.success()
        }

        private fun sendNotification(id: Int, title: String, message: String) {
                val notificationManager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                                NotificationManager
                val channelId = "expiration_channel"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel =
                                NotificationChannel(
                                        channelId,
                                        "Нагадування протерміну",
                                        NotificationManager.IMPORTANCE_HIGH
                                )
                        notificationManager.createNotificationChannel(channel)
                }

                val notification =
                        NotificationCompat.Builder(applicationContext, channelId)
                                .setSmallIcon(R.drawable.ic_bell)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .build()

                notificationManager.notify(id, notification)
        }

        companion object {
                fun sendCustomNotification(context: Context, title: String, message: String) {
                        val notificationManager =
                                context.getSystemService(Context.NOTIFICATION_SERVICE) as
                                        NotificationManager
                        val channelId = "expiration_channel"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel =
                                        NotificationChannel(
                                                channelId,
                                                "Нагадування протерміну",
                                                NotificationManager.IMPORTANCE_HIGH
                                        )
                                notificationManager.createNotificationChannel(channel)
                        }

                        val notification =
                                NotificationCompat.Builder(context, channelId)
                                        .setSmallIcon(R.drawable.ic_bell)
                                        .setContentTitle(title)
                                        .setContentText(message)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setAutoCancel(true)
                                        .build()

                        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
                }
        }
}
