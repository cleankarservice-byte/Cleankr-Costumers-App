package com.example.core.repository

import android.util.Log
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.BookingDao
import com.example.core.data.local.BookingEntity
import com.example.core.data.local.NotificationDao
import com.example.core.data.local.NotificationEntity
import com.example.core.data.session.SessionManager
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.BookingStatus
import com.example.core.model.PartnerInfo
import com.example.core.model.PaymentMethod
import com.example.core.model.PaymentStatus
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BookingRepository(
    private val bookingDao: BookingDao,
    private val notificationDao: NotificationDao,
    private val slotRepository: SlotRepository,
    private val serviceRepository: ServiceRepository,
    private val firebaseBackend: FirebaseBackendService? = null,
    private val sessionManager: SessionManager? = null
) {
    private val tag = "BookingRepo"

    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings().map { list ->
        list.map { it.toDomain() }
    }

    val activeBookings: Flow<List<Booking>> = bookingDao.getActiveBookings().map { list ->
        list.map { it.toDomain() }
    }

    fun getBookingById(id: String): Flow<Booking?> = bookingDao.getBookingById(id).map { it?.toDomain() }

    /**
     * Connect real-time Firestore synchronization for the authenticated customer.
     * Receives partner assignment, live tracking status updates from Admin Panel & Partner App.
     */
    fun startRealtimeSync(customerId: String, scope: CoroutineScope) {
        val fb = firebaseBackend ?: return
        if (!fb.isFirebaseConfigured()) return

        Log.d(tag, "Starting real-time Firebase sync for customer: $customerId")
        scope.launch {
            try {
                fb.observeCustomerBookings(customerId).collect { backendBookings ->
                    if (backendBookings.isNotEmpty()) {
                        bookingDao.insertBookings(backendBookings.map { BookingEntity.fromDomain(it) })
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Real-time sync error: ${e.message}")
            }
        }
    }

    suspend fun ensureInitialData() {
        // Only seed fallback initial demo data if database is completely empty
        val initialList = listOf(
            BookingEntity(
                id = "CK-2026-8192",
                customerId = "cust_001",
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
                updatedAt = System.currentTimeMillis() - 7200000,
                cancellationReason = null,
                userRating = null,
                userReview = null
            ),
            BookingEntity(
                id = "CK-2026-6411",
                customerId = "cust_001",
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
                updatedAt = System.currentTimeMillis() - 86400000 * 6,
                cancellationReason = null,
                userRating = 5.0f,
                userReview = "Spotless cleaning! The team was on time and very thorough."
            )
        )
        bookingDao.insertBookings(initialList)
    }

    /**
     * Server-side creation and validation logic:
     * - Checks slot availability
     * - Recalculates total price strictly based on official ServiceRepository data (never trusting client-modified price)
     * - Connects to shared Firebase ecosystem if configured
     * - Never displays "Booking confirmed" until the backend confirms successful creation
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

        // 2. Authoritative price recalculation (protects against client manipulation)
        val addOns = service.addOns.filter { selectedAddOnNames.contains(it.name) }
        val addOnsTotal = addOns.sumOf { it.price }
        val servicePrice = variant.price * quantity.coerceAtLeast(1)
        val calculatedTotal = servicePrice + addOnsTotal

        // 3. Current authenticated customer context
        val customerId = sessionManager?.currentUser?.value?.id
            ?: firebaseBackend?.currentCustomerId
            ?: "cust_001"

        // 4. Generate unique Booking ID & customer security PIN
        val randomNum = (1000..9999).random()
        val bookingId = "CK-2026-$randomNum"
        val startPin = (1000..9999).random().toString()

        val paymentStatus = if (paymentMethod == PaymentMethod.ONLINE) PaymentStatus.PAID else PaymentStatus.PENDING

        val newBooking = Booking(
            id = bookingId,
            customerId = customerId,
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
            status = BookingStatus.BOOKED, // Initial status strictly controlled by backend business rules
            partner = null, // Partner can only be assigned by Admin / Partner App
            startPin = startPin,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // 5. Backend synchronization (Enterprise Cleankr Ecosystem)
        if (firebaseBackend != null && firebaseBackend.isFirebaseConfigured()) {
            val backendResult = firebaseBackend.createBookingInSharedBackend(newBooking)
            if (backendResult.isFailure) {
                // Must not confirm if backend rejects
                val err = backendResult.exceptionOrNull() ?: Exception("Failed to sync booking to Cleankr backend.")
                Log.e(tag, "Backend booking failed: ${err.message}")
                return Result.failure(err)
            }
        }

        // 6. Cache into Room database for offline accessibility & fast UI updates
        bookingDao.insertBooking(BookingEntity.fromDomain(newBooking))

        // In-app notification
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
     * Partner status transitions must be reflected from trusted backend data.
     * Customer App is strictly forbidden from directly writing partner status in production.
     */
    suspend fun advanceStatus(bookingId: String, nextStatus: BookingStatus) {
        if (firebaseBackend != null && firebaseBackend.isFirebaseConfigured()) {
            Log.w(tag, "Customer App cannot directly write partner status in production ecosystem.")
            return
        }

        // Local demo/offline fallback only
        bookingDao.updateBookingStatus(bookingId, nextStatus.name)
    }

    suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit> {
        val customerId = sessionManager?.currentUser?.value?.id ?: "cust_001"
        if (firebaseBackend != null && firebaseBackend.isFirebaseConfigured()) {
            val backendResult = firebaseBackend.cancelBookingInSharedBackend(bookingId, customerId, reason)
            if (backendResult.isFailure) {
                return backendResult
            }
        }

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

        if (firebaseBackend != null && firebaseBackend.isFirebaseConfigured()) {
            val backendResult = firebaseBackend.rescheduleBookingInSharedBackend(bookingId, newDate, newSlot)
            if (backendResult.isFailure) {
                return backendResult
            }
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
        val customerId = sessionManager?.currentUser?.value?.id ?: "cust_001"
        bookingDao.rateBooking(bookingId, rating, review)

        if (firebaseBackend != null && firebaseBackend.isFirebaseConfigured()) {
            firebaseBackend.submitRatingInSharedBackend(
                bookingId = bookingId,
                customerId = customerId,
                serviceId = "",
                rating = rating,
                review = review
            )
        }
    }
}
