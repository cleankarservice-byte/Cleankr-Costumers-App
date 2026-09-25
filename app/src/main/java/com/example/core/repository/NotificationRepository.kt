package com.example.core.repository

import android.util.Log
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.NotificationDao
import com.example.core.data.local.NotificationEntity
import com.example.core.model.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val firebaseBackend: FirebaseBackendService? = null
) {
    private val tag = "NotifRepo"

    val notifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications().map { list ->
        list.map { it.toDomain() }
    }

    fun startRealtimeSync(customerId: String, scope: CoroutineScope) {
        val fb = firebaseBackend ?: return
        if (!fb.isFirebaseConfigured()) return

        scope.launch {
            try {
                fb.observeCustomerNotifications(customerId).collect { backendNotifs ->
                    if (backendNotifs.isNotEmpty()) {
                        notificationDao.insertNotifications(backendNotifs.map { NotificationEntity.fromDomain(it) })
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Notification sync error: ${e.message}")
            }
        }
    }

    suspend fun ensureInitialNotifications() {
        val initial = listOf(
            NotificationEntity(
                id = "notif_welcome",
                title = "Welcome to Cleankr! ✨",
                message = "Experience sparkling clean homes with vetted 5-star hygiene professionals.",
                timestamp = System.currentTimeMillis() - 86400000 * 2,
                type = "OFFER",
                isRead = false,
                bookingId = null
            ),
            NotificationEntity(
                id = "notif_active_status",
                title = "Partner is On The Way 🚗",
                message = "Partner Suresh Kumar is on the way to your Koramangala address. ETA 15 mins.",
                timestamp = System.currentTimeMillis() - 1800000,
                type = "PARTNER",
                isRead = false,
                bookingId = "CK-2026-8192"
            )
        )
        notificationDao.insertNotifications(initial)
    }

    suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }
}
