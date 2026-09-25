package com.example.core.data.firebase

import android.content.Context
import android.util.Log
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.BookingStatus
import com.example.core.model.CustomerUser
import com.example.core.model.NotificationItem
import com.example.core.model.PartnerInfo
import com.example.core.model.PaymentMethod
import com.example.core.model.PaymentStatus
import com.example.core.model.ServiceCategory
import com.example.core.model.SupportTicket
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * State of backend ecosystem connection
 */
enum class BackendConnectionState {
    CONNECTED,
    CONNECTING,
    OFFLINE_CACHE,
    NO_FIREBASE_CONFIG
}

/**
 * Enterprise Firebase Integration Service for Cleankr Customer App.
 * Connects directly to the shared Cleankr Firebase Ecosystem (Customer, Partner, Admin).
 */
class FirebaseBackendService(private val context: Context) {

    private val tag = "CleankrBackend"

    private val _connectionState = MutableStateFlow(
        if (isFirebaseConfigured()) BackendConnectionState.CONNECTING else BackendConnectionState.NO_FIREBASE_CONFIG
    )
    val connectionState: StateFlow<BackendConnectionState> = _connectionState.asStateFlow()

    fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseApp check error: ${e.message}")
            false
        }
    }

    init {
        initializeAppCheck()
    }

    /**
     * Initializes Firebase App Check with Play Integrity provider in production,
     * and Debug provider during local testing/development.
     */
    fun initializeAppCheck() {
        if (!isFirebaseConfigured()) return
        try {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d(tag, "AppCheck: DebugAppCheckProviderFactory installed for development")
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d(tag, "AppCheck: PlayIntegrityAppCheckProviderFactory installed for production")
            }
        } catch (e: Exception) {
            Log.w(tag, "AppCheck initialization note: ${e.message}")
        }
    }

    private val auth: FirebaseAuth? by lazy {
        if (isFirebaseConfigured()) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.w(tag, "FirebaseAuth unavailable: ${e.message}")
                null
            }
        } else null
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (isFirebaseConfigured()) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w(tag, "FirebaseFirestore unavailable: ${e.message}")
                null
            }
        } else null
    }

    private val storage: FirebaseStorage? by lazy {
        if (isFirebaseConfigured()) {
            try {
                FirebaseStorage.getInstance()
            } catch (e: Exception) {
                Log.w(tag, "FirebaseStorage unavailable: ${e.message}")
                null
            }
        } else null
    }

    /**
     * Target Firebase Project ID for the unified Cleankr Ecosystem.
     */
    fun getTargetProjectId(): String = FirebaseConstants.PROJECT_ID

    /**
     * Detects and returns the active Firebase project ID.
     * Returns "cleankr-724ce" by default, or the project ID from FirebaseOptions if configured.
     */
    fun getConnectedProjectId(): String {
        return try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val app = FirebaseApp.getInstance()
                val detectedId = app.options.projectId
                if (!detectedId.isNullOrBlank()) {
                    if (detectedId != FirebaseConstants.PROJECT_ID) {
                        Log.e(tag, "MISMATCH WARNING: Connected Firebase project is $detectedId but expected ${FirebaseConstants.PROJECT_ID}")
                    }
                    detectedId
                } else {
                    FirebaseConstants.PROJECT_ID
                }
            } else {
                FirebaseConstants.PROJECT_ID
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to read Firebase project ID: ${e.message}")
            FirebaseConstants.PROJECT_ID
        }
    }

    /**
     * Verifies that the connected Firebase configuration points strictly to cleankr-724ce.
     */
    fun isTargetProjectVerified(): Boolean {
        return getConnectedProjectId() == FirebaseConstants.PROJECT_ID
    }

    val currentFirebaseUser: FirebaseUser?
        get() = auth?.currentUser

    val currentCustomerId: String
        get() = currentFirebaseUser?.uid ?: "cust_offline"

    // -------------------------------------------------------------------------
    // 1. Firebase Authentication & Customer Profile
    // -------------------------------------------------------------------------

    suspend fun syncCustomerProfile(
        customerId: String,
        name: String,
        phone: String,
        email: String
    ): Result<Unit> {
        val db = firestore ?: return Result.success(Unit) // Offline fallback
        return try {
            val userPayload = hashMapOf<String, Any>(
                "uid" to customerId,
                "displayName" to name,
                "phone" to phone,
                "email" to email,
                FirebaseConstants.FIELD_ROLE to FirebaseConstants.ROLE_CUSTOMER, // Strictly customer role
                FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
            )

            val customerPayload = hashMapOf<String, Any>(
                FirebaseConstants.FIELD_CUSTOMER_ID to customerId,
                "name" to name,
                "phone" to phone,
                "email" to email,
                FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
            )

            // Write to 'users' and 'customers'
            db.collection(FirebaseConstants.COLLECTION_USERS)
                .document(customerId)
                .set(userPayload, SetOptions.merge())
                .await()

            db.collection(FirebaseConstants.COLLECTION_CUSTOMERS)
                .document(customerId)
                .set(customerPayload, SetOptions.merge())
                .await()

            _connectionState.value = BackendConnectionState.CONNECTED
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error syncing customer profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomerAccountInBackend(customerId: String, reason: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val updatePayload = mapOf(
                "status" to "DELETED",
                "deletionReason" to reason,
                "deletedAt" to System.currentTimeMillis(),
                "phone" to "[DELETED]",
                "email" to "[DELETED]",
                "displayName" to "Deleted Customer"
            )
            // Anonymize and mark user & customer profile in Firestore while preserving tax/invoicing records
            db.collection(FirebaseConstants.COLLECTION_USERS).document(customerId)
                .set(updatePayload, SetOptions.merge())
                .await()
            db.collection(FirebaseConstants.COLLECTION_CUSTOMERS).document(customerId)
                .set(updatePayload, SetOptions.merge())
                .await()

            // Delete current Firebase Auth user if authenticated
            auth?.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Account deletion in backend encountered: ${e.message}")
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // 2. Customer Bookings (Shared with Admin & Partner Ecosystem)
    // -------------------------------------------------------------------------

    suspend fun createBookingInSharedBackend(booking: Booking): Result<String> {
        val db = firestore ?: return Result.failure(
            IllegalStateException("Backend connection not configured. Please ensure google-services.json is attached.")
        )

        return try {
            val bookingDocRef = db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(booking.id)

            val addressMap = hashMapOf<String, Any>(
                "id" to booking.address.id,
                "label" to booking.address.label,
                "flatNo" to booking.address.flatNo,
                "street" to booking.address.street,
                "landmark" to booking.address.landmark,
                "city" to booking.address.city,
                "pincode" to booking.address.pincode,
                "contactPhone" to booking.address.contactPhone,
                "instructions" to booking.address.instructions
            )

            val bookingData = hashMapOf<String, Any?>(
                FirebaseConstants.FIELD_BOOKING_ID to booking.id,
                FirebaseConstants.FIELD_CUSTOMER_ID to booking.customerId,
                "customerName" to (currentFirebaseUser?.displayName ?: "Cleankr Customer"),
                "customerPhone" to (currentFirebaseUser?.phoneNumber ?: booking.address.contactPhone),
                FirebaseConstants.FIELD_SERVICE_ID to booking.serviceId,
                "serviceTitle" to booking.serviceTitle,
                "category" to booking.category.name,
                "variantName" to booking.variantName,
                "quantity" to booking.quantity,
                "selectedAddOns" to booking.selectedAddOns,
                "addOnsTotal" to booking.addOnsTotal,
                "servicePrice" to booking.servicePrice,
                "totalAmount" to booking.totalAmount,
                "bookingDate" to booking.bookingDate,
                "slotTime" to booking.slotTime,
                "address" to addressMap,
                "instructions" to booking.instructions,
                "paymentMethod" to booking.paymentMethod.name,
                "paymentStatus" to booking.paymentStatus.name,
                FirebaseConstants.FIELD_STATUS to FirebaseConstants.STATUS_BOOKED,
                // Partner fields strictly null on initial creation; only Admin/Partner sets them
                FirebaseConstants.FIELD_PARTNER_ID to null,
                "partnerName" to null,
                "partnerPhone" to null,
                "partnerRating" to null,
                "partnerJobs" to null,
                "startPin" to booking.startPin,
                FirebaseConstants.FIELD_CREATED_AT to booking.createdAt,
                FirebaseConstants.FIELD_UPDATED_AT to booking.updatedAt,
                "source" to "customer_android_app"
            )

            // Write booking
            bookingDocRef.set(bookingData).await()

            // Write initial status audit event to 'booking_status'
            val statusEvent = hashMapOf<String, Any>(
                "eventId" to "stat_${System.currentTimeMillis()}_${(100..999).random()}",
                FirebaseConstants.FIELD_BOOKING_ID to booking.id,
                FirebaseConstants.FIELD_STATUS to FirebaseConstants.STATUS_BOOKED,
                "updatedBy" to booking.customerId,
                "role" to FirebaseConstants.ROLE_CUSTOMER,
                "timestamp" to System.currentTimeMillis(),
                "note" to "Booking requested by customer"
            )
            db.collection(FirebaseConstants.COLLECTION_BOOKING_STATUS).add(statusEvent).await()

            // Write initial payment record to 'payments'
            val paymentRecord = hashMapOf<String, Any>(
                "paymentId" to "pay_${booking.id}",
                FirebaseConstants.FIELD_BOOKING_ID to booking.id,
                FirebaseConstants.FIELD_CUSTOMER_ID to booking.customerId,
                "amount" to booking.totalAmount,
                "method" to booking.paymentMethod.name,
                "status" to booking.paymentStatus.name,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection(FirebaseConstants.COLLECTION_PAYMENTS).document("pay_${booking.id}").set(paymentRecord).await()

            // Write in-app notification in 'notifications'
            val notificationData = hashMapOf<String, Any>(
                "notificationId" to "notif_${booking.id}_confirm",
                FirebaseConstants.FIELD_CUSTOMER_ID to booking.customerId,
                "title" to "Booking Confirmed! 🎉",
                "message" to "Your order for ${booking.serviceTitle} (${booking.id}) has been recorded. Admin will assign a partner soon.",
                "timestamp" to System.currentTimeMillis(),
                "type" to "BOOKING",
                "isRead" to false,
                "bookingId" to booking.id
            )
            db.collection(FirebaseConstants.COLLECTION_NOTIFICATIONS).add(notificationData).await()

            _connectionState.value = BackendConnectionState.CONNECTED
            Result.success(booking.id)
        } catch (e: Exception) {
            Log.e(tag, "Failed to create booking in backend: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for bookings belonging to this customer.
     * Reflects Partner assignments and live status changes (ASSIGNED, ACCEPTED, ON_THE_WAY, etc.) instantly.
     */
    fun observeCustomerBookings(customerId: String): Flow<List<Booking>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection(FirebaseConstants.COLLECTION_BOOKINGS)
            .whereEqualTo(FirebaseConstants.FIELD_CUSTOMER_ID, customerId)
            .orderBy(FirebaseConstants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.e(tag, "Firestore permission denied for customer bookings.")
                    } else if (error.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                        _connectionState.value = BackendConnectionState.OFFLINE_CACHE
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _connectionState.value = BackendConnectionState.CONNECTED
                    val bookings = snapshot.documents.mapNotNull { doc ->
                        parseBookingDocument(doc)
                    }
                    trySend(bookings)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Real-time listener for a single booking (Tracking screen).
     */
    fun observeBookingById(bookingId: String): Flow<Booking?> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection(FirebaseConstants.COLLECTION_BOOKINGS)
            .document(bookingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Error observing booking $bookingId: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val booking = parseBookingDocument(snapshot)
                    trySend(booking)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun cancelBookingInSharedBackend(bookingId: String, customerId: String, reason: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Backend unavailable"))
        return try {
            val docRef = db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(bookingId)
            docRef.update(
                mapOf(
                    FirebaseConstants.FIELD_STATUS to FirebaseConstants.STATUS_CANCELLED,
                    "cancellationReason" to reason,
                    FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
                )
            ).await()

            // Status event
            val statusEvent = hashMapOf<String, Any>(
                "eventId" to "stat_${System.currentTimeMillis()}_${(100..999).random()}",
                FirebaseConstants.FIELD_BOOKING_ID to bookingId,
                FirebaseConstants.FIELD_STATUS to FirebaseConstants.STATUS_CANCELLED,
                "updatedBy" to customerId,
                "role" to FirebaseConstants.ROLE_CUSTOMER,
                "timestamp" to System.currentTimeMillis(),
                "note" to "Cancelled by customer: $reason"
            )
            db.collection(FirebaseConstants.COLLECTION_BOOKING_STATUS).add(statusEvent).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error cancelling booking in backend: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun rescheduleBookingInSharedBackend(bookingId: String, newDate: String, newSlot: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Backend unavailable"))
        return try {
            val docRef = db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(bookingId)
            docRef.update(
                mapOf(
                    "bookingDate" to newDate,
                    "slotTime" to newSlot,
                    FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error rescheduling booking: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun submitRatingInSharedBackend(
        bookingId: String,
        customerId: String,
        serviceId: String,
        rating: Float,
        review: String
    ): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Backend unavailable"))
        return try {
            // Update booking document
            db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(bookingId).update(
                mapOf(
                    "userRating" to rating,
                    "userReview" to review,
                    FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
                )
            ).await()

            // Add to 'reviews' collection
            val reviewData = hashMapOf<String, Any>(
                "reviewId" to "rev_${bookingId}",
                FirebaseConstants.FIELD_BOOKING_ID to bookingId,
                FirebaseConstants.FIELD_SERVICE_ID to serviceId,
                FirebaseConstants.FIELD_CUSTOMER_ID to customerId,
                "rating" to rating,
                "review" to review,
                FirebaseConstants.FIELD_CREATED_AT to System.currentTimeMillis()
            )
            db.collection(FirebaseConstants.COLLECTION_REVIEWS).document("rev_${bookingId}").set(reviewData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error submitting review: ${e.message}", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // 3. Notifications (Shared Backend)
    // -------------------------------------------------------------------------

    fun observeCustomerNotifications(customerId: String): Flow<List<NotificationItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        val listener = db.collection(FirebaseConstants.COLLECTION_NOTIFICATIONS)
            .whereEqualTo(FirebaseConstants.FIELD_CUSTOMER_ID, customerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Error observing notifications: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifs = snapshot.documents.mapNotNull { doc ->
                        parseNotificationDocument(doc)
                    }
                    trySend(notifs)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    // -------------------------------------------------------------------------
    // 4. Support Tickets (Shared Backend)
    // -------------------------------------------------------------------------

    suspend fun createSupportTicketInBackend(ticket: SupportTicket, customerId: String): Result<SupportTicket> {
        val db = firestore ?: return Result.success(ticket)
        return try {
            val payload = hashMapOf<String, Any?>(
                "ticketId" to ticket.id,
                FirebaseConstants.FIELD_CUSTOMER_ID to customerId,
                FirebaseConstants.FIELD_BOOKING_ID to ticket.bookingId,
                "category" to ticket.category,
                "subject" to ticket.subject,
                "description" to ticket.description,
                FirebaseConstants.FIELD_STATUS to ticket.status,
                FirebaseConstants.FIELD_CREATED_AT to ticket.createdAt
            )
            db.collection(FirebaseConstants.COLLECTION_SUPPORT_TICKETS).document(ticket.id).set(payload).await()
            Result.success(ticket)
        } catch (e: Exception) {
            Log.e(tag, "Error saving support ticket: ${e.message}", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // 5. Customer Addresses (Shared Backend)
    // -------------------------------------------------------------------------

    suspend fun saveAddressInBackend(customerId: String, address: Address): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val addressMap = hashMapOf<String, Any>(
                "id" to address.id,
                "label" to address.label,
                "flatNo" to address.flatNo,
                "street" to address.street,
                "landmark" to address.landmark,
                "city" to address.city,
                "pincode" to address.pincode,
                "contactPhone" to address.contactPhone,
                "instructions" to address.instructions,
                "isDefault" to address.isDefault,
                FirebaseConstants.FIELD_CUSTOMER_ID to customerId,
                FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
            )

            db.collection(FirebaseConstants.COLLECTION_CUSTOMERS)
                .document(customerId)
                .collection(FirebaseConstants.SUBCOLLECTION_ADDRESSES)
                .document(address.id)
                .set(addressMap, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to save address in backend: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAddressInBackend(customerId: String, addressId: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(FirebaseConstants.COLLECTION_CUSTOMERS)
                .document(customerId)
                .collection(FirebaseConstants.SUBCOLLECTION_ADDRESSES)
                .document(addressId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to delete address in backend: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun observeCustomerAddresses(customerId: String): Flow<List<Address>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        val listener = db.collection(FirebaseConstants.COLLECTION_CUSTOMERS)
            .document(customerId)
            .collection(FirebaseConstants.SUBCOLLECTION_ADDRESSES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Error observing customer addresses: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val addresses = snapshot.documents.mapNotNull { doc ->
                        try {
                            Address(
                                id = doc.getString("id") ?: doc.id,
                                label = doc.getString("label") ?: "Home",
                                flatNo = doc.getString("flatNo") ?: "",
                                street = doc.getString("street") ?: "",
                                landmark = doc.getString("landmark") ?: "",
                                city = doc.getString("city") ?: "Bengaluru",
                                pincode = doc.getString("pincode") ?: "560001",
                                contactPhone = doc.getString("contactPhone") ?: "",
                                instructions = doc.getString("instructions") ?: "",
                                isDefault = doc.getBoolean("isDefault") ?: false
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(addresses)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    // -------------------------------------------------------------------------
    // 6. Device Push Notification Tokens (FCM)
    // -------------------------------------------------------------------------

    suspend fun updateFcmToken(customerId: String, token: String) {
        val db = firestore ?: return
        try {
            val tokenData = mapOf(
                FirebaseConstants.FIELD_FCM_TOKEN to token,
                FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
            )
            db.collection(FirebaseConstants.COLLECTION_USERS)
                .document(customerId)
                .set(tokenData, SetOptions.merge())
                .await()

            db.collection(FirebaseConstants.COLLECTION_CUSTOMERS)
                .document(customerId)
                .set(tokenData, SetOptions.merge())
                .await()
            Log.d(tag, "FCM token synchronized for customer: $customerId")
        } catch (e: Exception) {
            Log.w(tag, "Failed to update FCM token in backend: ${e.message}")
        }
    }

    /**
     * Proactively retrieves the active FCM token from FirebaseMessaging
     * and syncs it with the customer's profile in Firestore.
     */
    fun syncCurrentFcmToken(customerId: String) {
        if (!isFirebaseConfigured() || customerId.isBlank()) return
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        if (!token.isNullOrBlank()) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                updateFcmToken(customerId, token)
                            }
                        }
                    } else {
                        Log.w(tag, "Fetching FCM token failed: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "FirebaseMessaging token retrieval error: ${e.message}")
        }
    }

    /**
     * Removes the FCM token upon logout or account deletion to prevent
     * delivering notifications to an unauthenticated session.
     */
    suspend fun removeFcmToken(customerId: String) {
        val db = firestore ?: return
        try {
            val tokenData = mapOf(
                FirebaseConstants.FIELD_FCM_TOKEN to FieldValue.delete(),
                FirebaseConstants.FIELD_UPDATED_AT to System.currentTimeMillis()
            )
            db.collection(FirebaseConstants.COLLECTION_USERS)
                .document(customerId)
                .set(tokenData, SetOptions.merge())
                .await()

            db.collection(FirebaseConstants.COLLECTION_CUSTOMERS)
                .document(customerId)
                .set(tokenData, SetOptions.merge())
                .await()
            Log.d(tag, "FCM token deregistered for customer: $customerId")
        } catch (e: Exception) {
            Log.w(tag, "Failed to remove FCM token in backend: ${e.message}")
        }
    }

    // -------------------------------------------------------------------------
    // 7. Customer Media & Photos (Firebase Storage)
    // -------------------------------------------------------------------------

    suspend fun uploadCustomerMedia(
        customerId: String,
        category: String,
        fileName: String,
        fileBytes: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String> {
        val st = storage ?: return Result.failure(IllegalStateException("Firebase Storage is not available or offline"))
        return try {
            val storageRef = st.reference
                .child("customers")
                .child(customerId)
                .child(category)
                .child(fileName)

            val metadata = StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata("customerId", customerId)
                .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                .build()

            storageRef.putBytes(fileBytes, metadata).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(tag, "Failed to upload customer media: ${e.message}", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------------------
    // Document Parsers
    // -------------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    private fun parseBookingDocument(doc: DocumentSnapshot): Booking? {
        return try {
            val id = doc.getString(FirebaseConstants.FIELD_BOOKING_ID) ?: doc.id
            val customerId = doc.getString(FirebaseConstants.FIELD_CUSTOMER_ID) ?: ""
            val serviceId = doc.getString(FirebaseConstants.FIELD_SERVICE_ID) ?: ""
            val serviceTitle = doc.getString("serviceTitle") ?: "Cleaning Service"
            val categoryStr = doc.getString("category") ?: ServiceCategory.BATHROOM.name
            val category = try { ServiceCategory.valueOf(categoryStr) } catch (_: Exception) { ServiceCategory.BATHROOM }
            val variantName = doc.getString("variantName") ?: "Standard"
            val quantity = (doc.getLong("quantity") ?: 1L).toInt()
            val selectedAddOns = (doc.get("selectedAddOns") as? List<String>) ?: emptyList()
            val addOnsTotal = (doc.getLong("addOnsTotal") ?: 0L).toInt()
            val servicePrice = (doc.getLong("servicePrice") ?: 0L).toInt()
            val totalAmount = (doc.getLong("totalAmount") ?: (servicePrice + addOnsTotal).toLong()).toInt()
            val bookingDate = doc.getString("bookingDate") ?: ""
            val slotTime = doc.getString("slotTime") ?: ""

            val addressMap = doc.get("address") as? Map<String, Any?>
            val address = if (addressMap != null) {
                Address(
                    id = addressMap["id"]?.toString() ?: "addr_default",
                    label = addressMap["label"]?.toString() ?: "Home",
                    flatNo = addressMap["flatNo"]?.toString() ?: "",
                    street = addressMap["street"]?.toString() ?: "",
                    landmark = addressMap["landmark"]?.toString() ?: "",
                    city = addressMap["city"]?.toString() ?: "Bengaluru",
                    pincode = addressMap["pincode"]?.toString() ?: "560001",
                    contactPhone = addressMap["contactPhone"]?.toString() ?: "",
                    instructions = addressMap["instructions"]?.toString() ?: ""
                )
            } else {
                Address(
                    id = "addr_default",
                    label = "Home",
                    flatNo = "",
                    street = "",
                    landmark = "",
                    city = "Bengaluru",
                    pincode = "560001"
                )
            }

            val instructions = doc.getString("instructions") ?: ""
            val paymentMethodStr = doc.getString("paymentMethod") ?: PaymentMethod.ONLINE.name
            val paymentMethod = try { PaymentMethod.valueOf(paymentMethodStr) } catch (_: Exception) { PaymentMethod.ONLINE }
            val paymentStatusStr = doc.getString("paymentStatus") ?: PaymentStatus.PENDING.name
            val paymentStatus = try { PaymentStatus.valueOf(paymentStatusStr) } catch (_: Exception) { PaymentStatus.PENDING }

            val statusStr = doc.getString(FirebaseConstants.FIELD_STATUS) ?: BookingStatus.BOOKED.name
            val status = try { BookingStatus.valueOf(statusStr) } catch (_: Exception) { BookingStatus.BOOKED }

            // Partner details (populated when Admin assigns partner or Partner accepts)
            val partnerId = doc.getString(FirebaseConstants.FIELD_PARTNER_ID)
            val partnerName = doc.getString("partnerName")
            val partner = if (!partnerId.isNullOrBlank() && !partnerName.isNullOrBlank()) {
                PartnerInfo(
                    id = partnerId,
                    name = partnerName,
                    rating = (doc.getDouble("partnerRating") ?: 4.9).toFloat(),
                    jobsCompleted = (doc.getLong("partnerJobs") ?: 150L).toInt(),
                    maskedPhone = doc.getString("partnerPhone") ?: "+91 80 4719 3200"
                )
            } else null

            val startPin = doc.getString("startPin") ?: "4821"
            val createdAt = doc.getLong(FirebaseConstants.FIELD_CREATED_AT) ?: System.currentTimeMillis()
            val updatedAt = doc.getLong(FirebaseConstants.FIELD_UPDATED_AT) ?: createdAt
            val cancellationReason = doc.getString("cancellationReason")
            val userRating = doc.getDouble("userRating")?.toFloat()
            val userReview = doc.getString("userReview")

            Booking(
                id = id,
                customerId = customerId,
                serviceId = serviceId,
                serviceTitle = serviceTitle,
                category = category,
                variantName = variantName,
                quantity = quantity,
                selectedAddOns = selectedAddOns,
                addOnsTotal = addOnsTotal,
                servicePrice = servicePrice,
                totalAmount = totalAmount,
                bookingDate = bookingDate,
                slotTime = slotTime,
                address = address,
                instructions = instructions,
                paymentMethod = paymentMethod,
                paymentStatus = paymentStatus,
                status = status,
                partner = partner,
                startPin = startPin,
                createdAt = createdAt,
                updatedAt = updatedAt,
                cancellationReason = cancellationReason,
                userRating = userRating,
                userReview = userReview
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to parse booking doc ${doc.id}: ${e.message}", e)
            null
        }
    }

    private fun parseNotificationDocument(doc: DocumentSnapshot): NotificationItem? {
        return try {
            NotificationItem(
                id = doc.getString("notificationId") ?: doc.id,
                title = doc.getString("title") ?: "Cleankr Update",
                message = doc.getString("message") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                type = doc.getString("type") ?: "BOOKING",
                isRead = doc.getBoolean("isRead") ?: false,
                bookingId = doc.getString("bookingId")
            )
        } catch (e: Exception) {
            null
        }
    }
}
