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
                val storage = FirebaseExpirationStorage()
                val reminders = storage.reminders.first()
                val today = System.currentTimeMillis()

                reminders.forEach { item ->
                        // Check if active
                        if (item.isWrittenOff) return@forEach

                        // Calculate remaining days
                        val diff = item.finalDate - today
                        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diff)

                        // Logic for discounts?
                        // The item has pre-calculated discount dates.
                        // Check if TODAY is one of those dates or past them, AND discount not
                        // applied.

                        // Note: Since Worker runs daily, we check if we are AT or PAST the trigger
                        // date,
                        // but logic says "Notification comes.. check if checkmark.. if checkmark no
                        // notification".
                        // item.isDiscount10Applied stores the checkmark state.

                        // 10%
                        // Expired?
                        if (daysRemaining <= 0) {
                                sendNotification(
                                        item.id.hashCode() + 3,
                                        "Протерміновано!",
                                        "Списати товар: ${item.name}"
                                )
                        }
                        // 50%
                        else if (!item.isDiscount50Applied &&
                                        item.discount50Date != null &&
                                        today >= item.discount50Date
                        ) {
                                sendNotification(
                                        item.id.hashCode() + 2,
                                        "Знижка 50%",
                                        "Товар: ${item.name}. Треба наклеїти 50%."
                                )
                        }
                        // 25%
                        else if (!item.isDiscount25Applied &&
                                        item.discount25Date != null &&
                                        today >= item.discount25Date
                        ) {
                                sendNotification(
                                        item.id.hashCode() + 1,
                                        "Знижка 25%",
                                        "Товар: ${item.name}. Треба наклеїти 25%."
                                )
                        }
                        // 10%
                        else if (!item.isDiscount10Applied &&
                                        item.discount10Date != null &&
                                        today >= item.discount10Date
                        ) {
                                sendNotification(
                                        item.id.hashCode(),
                                        "Знижка 10%",
                                        "Товар: ${item.name}. Треба наклеїти 10%."
                                )
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
