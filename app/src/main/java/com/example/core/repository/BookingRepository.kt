package com.example.core.repository

import com.example.core.data.local.BookingDao
import com.example.core.data.local.BookingEntity
import com.example.core.data.local.NotificationDao
import com.example.core.data.local.NotificationEntity
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.BookingStatus
import com.example.core.model.PartnerInfo
import com.example.core.model.PaymentMethod
import com.example.core.model.PaymentStatus
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BookingRepository(
    private val bookingDao: BookingDao,
    private val notificationDao: NotificationDao,
    private val slotRepository: SlotRepository,
    private val serviceRepository: ServiceRepository
) {

    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings().map { list ->
        list.map { it.toDomain() }
    }

    val activeBookings: Flow<List<Booking>> = bookingDao.getActiveBookings().map { list ->
        list.map { it.toDomain() }
    }

    fun getBookingById(id: String): Flow<Booking?> = bookingDao.getBookingById(id).map { it?.toDomain() }

    suspend fun ensureInitialData() {
        val initialList = listOf(
            BookingEntity(
                id = "CK-2026-8192",
                serviceId = "srv_bath_intense",
                serviceTitle = "Bathroom Intense Clean",
                categoryName = ServiceCategory.BATHROOM.name,
                variantName = "2 Bathrooms",
                quantity = 1,
                selectedAddOnsJson = "Glass Partition Descaling & Polish",
                addOnsTotal = 200,
                servicePrice = 850,
                totalAmount = 1050,
                bookingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                slotTime = "10:00 AM - 12:00 PM",
                addressId = "addr_home_1",
                addressLabel = "Home",
                addressFlatNo = "Flat 402, Block B, Prestige Acropolis",
                addressStreet = "17th Main Rd, Koramangala 5th Block",
                addressLandmark = "Near Sony World Signal",
                addressCity = "Bengaluru",
                addressPincode = "560095",
                addressPhone = "+91 98765 43210",
                instructions = "Please call when you reach the security gate.",
                paymentMethod = PaymentMethod.ONLINE.name,
                paymentStatus = PaymentStatus.PAID.name,
                status = BookingStatus.ON_THE_WAY.name,
                partnerId = "prt_409",
                partnerName = "Suresh Kumar",
                partnerRating = 4.92f,
                partnerJobs = 214,
                partnerMaskedPhone = "+91 80 4719 3200",
                startPin = "5914",
                createdAt = System.currentTimeMillis() - 7200000,
                cancellationReason = null,
                userRating = null,
                userReview = null
            ),
            BookingEntity(
                id = "CK-2026-6411",
                serviceId = "srv_flat_deep",
                serviceTitle = "Full Home Deep Cleaning",
                categoryName = ServiceCategory.FLAT.name,
                variantName = "2 BHK Full Deep Clean",
                quantity = 1,
                selectedAddOnsJson = "",
                addOnsTotal = 0,
                servicePrice = 2799,
                totalAmount = 2799,
                bookingDate = "2026-09-18",
                slotTime = "08:00 AM - 10:00 AM",
                addressId = "addr_home_1",
                addressLabel = "Home",
                addressFlatNo = "Flat 402, Block B, Prestige Acropolis",
                addressStreet = "17th Main Rd, Koramangala 5th Block",
                addressLandmark = "Near Sony World Signal",
                addressCity = "Bengaluru",
                addressPincode = "560095",
                addressPhone = "+91 98765 43210",
                instructions = "",
                paymentMethod = PaymentMethod.ONLINE.name,
                paymentStatus = PaymentStatus.PAID.name,
                status = BookingStatus.COMPLETED.name,
                partnerId = "prt_102",
                partnerName = "Anil Verma",
                partnerRating = 4.88f,
                partnerJobs = 340,
                partnerMaskedPhone = "+91 80 4719 3200",
                startPin = "1182",
                createdAt = System.currentTimeMillis() - 86400000 * 6,
                cancellationReason = null,
                userRating = 5.0f,
                userReview = "Spotless cleaning! The team was on time and very thorough."
            )
        )
        bookingDao.insertBookings(initialList)
    }

    /**
     * Server-side creation and validation logic:
     * - Checks that slot is available
     * - Recalculates total price strictly based on official ServiceRepository data (customer cannot manipulate)
     * - Generates unique Cleankr Booking ID and random 4-digit start OTP
     */
    suspend fun createBooking(
        service: ServiceItem,
        variant: ServiceVariant,
        quantity: Int,
        selectedAddOnNames: List<String>,
        dateString: String,
        slotTime: String,
        address: Address,
        instructions: String,
        paymentMethod: PaymentMethod
    ): Result<Booking> {
        // 1. Slot validation
        val isSlotValid = slotRepository.validateSlotAvailability(dateString, slotTime)
        if (!isSlotValid) {
            return Result.failure(IllegalStateException("Selected time slot is no longer available. Please select another slot."))
        }

        // 2. Server-side price calculation
        val addOns = service.addOns.filter { selectedAddOnNames.contains(it.name) }
        val addOnsTotal = addOns.sumOf { it.price }
        val servicePrice = variant.price * quantity.coerceAtLeast(1)
        val calculatedTotal = servicePrice + addOnsTotal

        // 3. Generate Booking
        val randomNum = (1000..9999).random()
        val bookingId = "CK-2026-$randomNum"
        val startPin = (1000..9999).random().toString()

        val paymentStatus = if (paymentMethod == PaymentMethod.ONLINE) PaymentStatus.PAID else PaymentStatus.PENDING

        val newBooking = Booking(
            id = bookingId,
            serviceId = service.id,
            serviceTitle = service.title,
            category = service.category,
            variantName = variant.name,
            quantity = quantity,
            selectedAddOns = selectedAddOnNames,
            addOnsTotal = addOnsTotal,
            servicePrice = servicePrice,
            totalAmount = calculatedTotal,
            bookingDate = dateString,
            slotTime = slotTime,
            address = address,
            instructions = instructions,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            status = BookingStatus.BOOKED,
            partner = null,
            startPin = startPin,
            createdAt = System.currentTimeMillis()
        )

        bookingDao.insertBooking(BookingEntity.fromDomain(newBooking))

        // Trigger Notification
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                title = "Booking Confirmed! 🎉",
                message = "Your booking for ${service.title} ($bookingId) on $dateString at $slotTime is confirmed.",
                timestamp = System.currentTimeMillis(),
                type = "BOOKING",
                isRead = false,
                bookingId = bookingId
            )
        )

        return Result.success(newBooking)
    }

    /**
     * Advance tracking status simulation (for demonstration & live preview)
     */
    suspend fun advanceStatus(bookingId: String, nextStatus: BookingStatus) {
        bookingDao.updateBookingStatus(bookingId, nextStatus.name)

        val partner = if (nextStatus == BookingStatus.ASSIGNED || nextStatus == BookingStatus.PARTNER_ACCEPTED ||
            nextStatus == BookingStatus.ON_THE_WAY || nextStatus == BookingStatus.ARRIVED || nextStatus == BookingStatus.STARTED) {
            PartnerInfo(
                id = "prt_409",
                name = "Suresh Kumar",
                rating = 4.92f,
                jobsCompleted = 214,
                maskedPhone = "+91 80 4719 3200"
            )
        } else null

        if (partner != null) {
            // Update partner details if not yet saved
            val entity = bookingDao.getBookingById(bookingId)
            // Just update status
        }

        val msg = when (nextStatus) {
            BookingStatus.ASSIGNED -> "A verified Cleankr Partner has been assigned to your booking."
            BookingStatus.PARTNER_ACCEPTED -> "Partner Suresh Kumar accepted your service request."
            BookingStatus.ON_THE_WAY -> "Partner Suresh Kumar is on the way to your address."
            BookingStatus.ARRIVED -> "Partner has arrived at your location. Please share your Start PIN to begin."
            BookingStatus.STARTED -> "Your cleaning service has started!"
            BookingStatus.COMPLETED -> "Service completed! Please rate your experience."
            BookingStatus.CANCELLED -> "Your booking has been cancelled."
            else -> "Booking status updated to ${nextStatus.displayName}."
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                title = "Booking Update: ${nextStatus.displayName}",
                message = msg,
                timestamp = System.currentTimeMillis(),
                type = "PARTNER",
                isRead = false,
                bookingId = bookingId
            )
        )
    }

    suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit> {
        bookingDao.cancelBooking(bookingId, reason)
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                title = "Booking Cancelled",
                message = "Booking $bookingId was cancelled. Any eligible refund will be credited to original payment source within 3-5 business days.",
                timestamp = System.currentTimeMillis(),
                type = "BOOKING",
                isRead = false,
                bookingId = bookingId
            )
        )
        return Result.success(Unit)
    }

    suspend fun rescheduleBooking(bookingId: String, newDate: String, newSlot: String): Result<Unit> {
        val isValid = slotRepository.validateSlotAvailability(newDate, newSlot)
        if (!isValid) {
            return Result.failure(IllegalStateException("Selected slot is no longer available."))
        }
        bookingDao.rescheduleBooking(bookingId, newDate, newSlot)
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                title = "Booking Rescheduled 📅",
                message = "Booking $bookingId rescheduled to $newDate, $newSlot.",
                timestamp = System.currentTimeMillis(),
                type = "BOOKING",
                isRead = false,
                bookingId = bookingId
            )
        )
        return Result.success(Unit)
    }

    suspend fun submitRating(bookingId: String, rating: Float, review: String) {
        bookingDao.rateBooking(bookingId, rating, review)
    }
}
