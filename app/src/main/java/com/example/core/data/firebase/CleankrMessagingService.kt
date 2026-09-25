package com.example.core.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.core.data.local.AppDatabase
import com.example.core.data.local.NotificationEntity
import com.example.core.data.session.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Handles incoming Firebase Cloud Messaging (FCM) notifications and device token rotations.
 * Automatically synchronizes incoming partner/admin booking updates with the local Room notification store.
 */
class CleankrMessagingService : FirebaseMessagingService() {

    private val tag = "CleankrFCM"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(tag, "Refreshed FCM token received for Cleankr Ecosystem.")
        val sessionManager = SessionManager(applicationContext)
        val customerId = sessionManager.currentUser.value?.id

        if (!customerId.isNullOrBlank()) {
            val backend = FirebaseBackendService(applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                backend.updateFcmToken(customerId, token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(tag, "FCM message received from ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: "Cleankr Update"
        val body = notification?.body ?: data["message"] ?: data["body"] ?: "You have a new update regarding your cleaning service."
        val bookingId = data["bookingId"]
        val type = data["type"] ?: "BOOKING"
        val notifId = data["notificationId"] ?: "fcm_${UUID.randomUUID().toString().take(8)}"

        // 1. Persist notification in local Room database
        val entity = NotificationEntity(
            id = notifId,
            title = title,
            message = body,
            timestamp = System.currentTimeMillis(),
            type = type,
            isRead = false,
            bookingId = bookingId
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(applicationContext).notificationDao().insertNotification(entity)
            } catch (e: Exception) {
                Log.w(tag, "Failed to persist FCM notification locally: ${e.message}")
            }
        }

        // 2. Display system status bar notification
        showSystemNotification(title, body, bookingId)
    }

    private fun showSystemNotification(title: String, message: String, bookingId: String?) {
        val channelId = "cleankr_booking_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cleankr Booking Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live notifications for partner assignment, arrival, and service progress"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!bookingId.isNullOrBlank()) {
                putExtra("bookingId", bookingId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
