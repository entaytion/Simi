package ua.entaytion.simi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ua.entaytion.simi.R

/** Firebase Cloud Messaging service. Handles push notifications from FCM. */
class SimiFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Get notification data
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Simi"
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: ""

        showNotification(title, message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token is automatically managed when using topics
        // No need to send to server for topic-based messaging
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fcm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                                    channelId,
                                    "Сповіщення Simi",
                                    NotificationManager.IMPORTANCE_HIGH
                            )
                            .apply {
                                description = "Push-сповіщення від Simi"
                                enableLights(true)
                                enableVibration(true)
                            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification =
                NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_bell)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
