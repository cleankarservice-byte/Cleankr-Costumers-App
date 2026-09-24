package com.example.core.repository

import com.example.core.data.local.NotificationDao
import com.example.core.data.local.NotificationEntity
import com.example.core.model.NotificationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NotificationRepository(private val notificationDao: NotificationDao) {

    val notifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications().map { list ->
        list.map { it.toDomain() }
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
