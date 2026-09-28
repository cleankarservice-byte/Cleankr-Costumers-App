package com.example.core.repository

import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.BookingDao
import com.example.core.data.local.BookingEntity
import com.example.core.model.AddOnItem
import com.example.core.model.Address
import com.example.core.model.Booking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.random.Random

class BookingRepository(
    private val bookingDao: BookingDao,
    private val notificationRepository: NotificationRepository,
    private val firebaseBackend: FirebaseBackendService?
) {

    fun observeAllBookings(): Flow<List<Booking>> {
        return bookingDao.getAllBookings().map { list -> list.map { it.toDomain() } }
    }

    fun observeCustomerBookings(customerId: String): Flow<List<Booking>> {
        return bookingDao.getBookingsForCustomer(customerId).map { list -> list.map { it.toDomain() } }
    }

    fun observeActiveBookings(customerId: String): Flow<List<Booking>> {
        return bookingDao.getActiveBookingsForCustomer(customerId).map { list -> list.map { it.toDomain() } }
    }

    fun observeBookingById(bookingId: String): Flow<Booking?> {
        return bookingDao.getBookingById(bookingId).map { it?.toDomain() }
    }

    suspend fun getBookingByIdDirect(bookingId: String): Booking? {
        return bookingDao.getBookingByIdDirect(bookingId)?.toDomain()
    }

    suspend fun createBooking(
        customerId: String,
        serviceId: String,
        serviceTitle: String,
        categoryName: String,
        variantName: String,
        quantity: Int,
        selectedAddOns: List<AddOnItem>,
        bookingDate: String,
        slotTime: String,
        address: Address,
        instructions: String,
        paymentMethod: String
    ): Booking {
        // Active Hub Check: Must be serviceable
        val activeHub = firebaseBackend?.findActiveHubForAddress(address)
        if (activeHub == null) {
            throw IllegalStateException("Cleankr service is currently unavailable in your area (${address.pincode}). Your request has been logged in the Pune Operations Admin Panel.")
        }

        // Price validation
        val priceResult = firebaseBackend.validateAndCalculatePrice(
            serviceId = serviceId,
            variantName = variantName,
            quantity = quantity,
            selectedAddOns = selectedAddOns
        )

        val basePrice = priceResult.calculatedBasePrice
        val addOnPrice = priceResult.calculatedAddOnPrice
        val totalAmount = priceResult.calculatedTotal

        // Generate 4-digit Doorstep Safety PIN (Feature 1)
        val startPin = String.format("%04d", Random.nextInt(1000, 9999))
        val bookingId = "CLN-${System.currentTimeMillis() % 1000000}"

        val newBooking = Booking(
            id = bookingId,
            customerId = customerId,
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            categoryName = categoryName,
            variantName = variantName,
            quantity = quantity,
            selectedAddOns = selectedAddOns,
            addOnsTotal = addOnPrice,
            servicePrice = basePrice,
            totalAmount = totalAmount,
            basePrice = basePrice,
            addOnPrice = addOnPrice,
            hubId = activeHub.hubId,
            hubName = activeHub.hubName,
            bookingDate = bookingDate,
            slotTime = slotTime,
            address = address,
            instructions = instructions,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == "ONLINE") "PAID" else "PENDING",
            status = "CONFIRMED",
            partnerId = "prt_pune_01",
            partnerName = "Sachin Patil (Pune Certified Pro)",
            partnerRating = 4.9f,
            partnerJobs = 412,
            partnerMaskedPhone = "+91 98*** *2104",
            startPin = startPin,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Save local in Room
        bookingDao.insertBooking(BookingEntity.fromDomain(newBooking))

        // Sync with shared Firestore backend
        try {
            firebaseBackend.createBookingInSharedBackend(newBooking)
        } catch (_: Exception) {}

        // Add Notification
        notificationRepository.addNotification(
            title = "Booking Confirmed (#$bookingId)",
            message = "Your $serviceTitle is scheduled for $bookingDate ($slotTime). Assigned Hub: ${activeHub.hubName}. Doorstep OTP: $startPin",
            type = "BOOKING",
            bookingId = bookingId
        )

        return newBooking
    }

    suspend fun rescheduleBooking(bookingId: String, newDate: String, newSlot: String) {
        val now = System.currentTimeMillis()
        bookingDao.rescheduleBooking(bookingId, newDate, newSlot, now)
        try {
            firebaseBackend?.rescheduleBookingInSharedBackend(bookingId, newDate, newSlot)
        } catch (_: Exception) {}

        notificationRepository.addNotification(
            title = "Booking Rescheduled (#$bookingId)",
            message = "Your service slot has been updated to $newDate at $newSlot.",
            type = "BOOKING",
            bookingId = bookingId
        )
    }

    suspend fun cancelBooking(bookingId: String, reason: String) {
        val now = System.currentTimeMillis()
        bookingDao.cancelBooking(bookingId, "CANCELLED", reason, now)
        try {
            firebaseBackend?.cancelBookingInSharedBackend(bookingId, reason)
        } catch (_: Exception) {}

        notificationRepository.addNotification(
            title = "Booking Cancelled (#$bookingId)",
            message = "Your booking was cancelled. Reason: $reason",
            type = "BOOKING",
            bookingId = bookingId
        )
    }

    suspend fun submitRating(bookingId: String, rating: Float, review: String) {
        val now = System.currentTimeMillis()
        bookingDao.submitRating(bookingId, rating, review, now)
        try {
            firebaseBackend?.submitReviewInSharedBackend(bookingId, rating, review)
        } catch (_: Exception) {}

        notificationRepository.addNotification(
            title = "Thank You for Your Feedback! ⭐",
            message = "Your review helps us keep Cleankr cleaning standards five-star.",
            type = "INFO",
            bookingId = bookingId
        )
    }
}
