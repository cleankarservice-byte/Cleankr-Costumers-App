package com.example.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getBookingsForCustomer(customerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE customerId = :customerId AND status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY createdAt DESC")
    fun getActiveBookingsForCustomer(customerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingById(id: String): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingByIdDirect(id: String): BookingEntity?

    @Query("UPDATE bookings SET status = :status, cancellationReason = :reason, updatedAt = :updatedAt WHERE id = :id")
    suspend fun cancelBooking(id: String, status: String, reason: String, updatedAt: Long)

    @Query("UPDATE bookings SET bookingDate = :newDate, slotTime = :newSlot, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rescheduleBooking(id: String, newDate: String, newSlot: String, updatedAt: Long)

    @Query("UPDATE bookings SET userRating = :rating, userReview = :review, updatedAt = :updatedAt WHERE id = :id")
    suspend fun submitRating(id: String, rating: Float, review: String, updatedAt: Long)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: String)
}

@Dao
interface AddressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<AddressEntity>)

    @Query("SELECT * FROM addresses ORDER BY isDefault DESC, id DESC")
    fun getAllAddresses(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :id LIMIT 1")
    suspend fun getAddressById(id: String): AddressEntity?

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddressById(id: String)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAddress(id: String)
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String)
}

@Dao
interface SupportTicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>
}

@Dao
interface CrossHubAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: CrossHubAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<CrossHubAttemptEntity>)

    @Query("SELECT * FROM cross_hub_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<CrossHubAttemptEntity>>

    @Query("SELECT * FROM cross_hub_attempts WHERE status = :status ORDER BY timestamp DESC")
    fun getAttemptsByStatus(status: String): Flow<List<CrossHubAttemptEntity>>

    @Query("SELECT * FROM cross_hub_attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptById(id: String): CrossHubAttemptEntity?

    @Query("UPDATE cross_hub_attempts SET status = :status, adminNotes = :notes WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, notes: String?)

    @Query("UPDATE cross_hub_attempts SET status = 'ROUTED', assignedHubId = :hubId, assignedHubName = :hubName, adminNotes = :notes WHERE id = :id")
    suspend fun routeAttempt(id: String, hubId: String, hubName: String, notes: String?)

    @Query("DELETE FROM cross_hub_attempts WHERE id = :id")
    suspend fun deleteAttemptById(id: String)
}

