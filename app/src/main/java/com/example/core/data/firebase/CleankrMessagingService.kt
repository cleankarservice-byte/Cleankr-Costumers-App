package com.example.core.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.core.data.local.AppDatabase
import com.example.core.data.local.NotificationEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class CleankrMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("CleankrFCM", "New FCM registration token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "Cleankr Update"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new update."
        val bookingId = message.data["bookingId"]
        val type = message.data["type"] ?: "BOOKING"

        showNotification(title, body)

        serviceScope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                db.notificationDao().insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        message = body,
                        timestamp = System.currentTimeMillis(),
                        type = type,
                        isRead = false,
                        bookingId = bookingId
                    )
                )
            } catch (e: Exception) {
                Log.e("CleankrFCM", "Failed to store incoming notification", e)
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "cleankr_customer_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cleankr Booking Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live updates about your service bookings and cleaning partners"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
