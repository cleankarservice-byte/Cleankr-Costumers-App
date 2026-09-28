package com.example.core.repository

import com.example.core.data.local.NotificationDao
import com.example.core.data.local.NotificationEntity
import com.example.core.model.NotificationItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NotificationRepository(private val notificationDao: NotificationDao) {

    fun observeNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addNotification(title: String, message: String, type: String = "INFO", bookingId: String? = null) {
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                type = type,
                isRead = false,
                bookingId = bookingId
            )
        )
    }

    suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }
}
