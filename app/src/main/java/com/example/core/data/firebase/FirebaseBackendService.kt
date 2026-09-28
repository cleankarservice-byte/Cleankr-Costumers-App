package com.example.core.data.firebase

import android.content.Context
import android.util.Log
import com.example.core.model.AddOnItem
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.CleankrHub
import com.example.core.model.CrossHubAttempt
import com.example.core.model.NotificationItem
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import com.example.core.model.SupportTicket
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

enum class BackendConnectionState {
    CONNECTED, CONNECTING, DISCONNECTED, OFFLINE_FALLBACK
}

class FirebaseBackendService(private val context: Context) {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseBackend", "Could not initialize Firestore: ${e.message}")
            null
        }
    }

    data class PriceValidationResult(
        val isValid: Boolean,
        val calculatedBasePrice: Int,
        val calculatedAddOnPrice: Int,
        val calculatedTotal: Int,
        val message: String? = null
    )

    // Official Pune Hubs Division (User Configured)
    val puneHubs = listOf(
        CleankrHub(
            hubId = "hub_pune_west",
            hubName = "Pune West Hub (Kothrud, Baner, Aundh)",
            city = "Pune",
            divisionArea = "Pune West",
            keyLocalities = "Kothrud, Baner, Aundh, Pashan, Bavdhan, SB Road",
            coveredPincodes = listOf("411038", "411045", "411007", "411021", "411008", "411016"),
            isActive = true,
            activeStaff = 14,
            contactPhone = "+91 98230 44101"
        ),
        CleankrHub(
            hubId = "hub_pune_east",
            hubName = "Pune East Hub (Viman Nagar, Kharadi)",
            city = "Pune",
            divisionArea = "Pune East",
            keyLocalities = "Viman Nagar, Kharadi, Kalyani Nagar, Koregaon Park, Wadgaon Sheri",
            coveredPincodes = listOf("411014", "411006", "411001", "411036"),
            isActive = true,
            activeStaff = 16,
            contactPhone = "+91 98230 44102"
        ),
        CleankrHub(
            hubId = "hub_pune_north",
            hubName = "Pune North / PCMC Hub (Hinjawadi, Wakad)",
            city = "Pune",
            divisionArea = "Pune North / PCMC",
            keyLocalities = "Hinjawadi Phase 1-3, Wakad, Pimple Saudagar, Pimpri, Chinchwad",
            coveredPincodes = listOf("411057", "411027", "411017", "411018", "411033", "411061"),
            isActive = true,
            activeStaff = 18,
            contactPhone = "+91 98230 44103"
        ),
        CleankrHub(
            hubId = "hub_pune_southeast",
            hubName = "Pune South-East Hub (Hadapsar, Magarpatta)",
            city = "Pune",
            divisionArea = "Pune South-East",
            keyLocalities = "Hadapsar, Magarpatta City, Wanowrie, Fatima Nagar, Kondhwa",
            coveredPincodes = listOf("411028", "411040", "411048", "411022"),
            isActive = true,
            activeStaff = 12,
            contactPhone = "+91 98230 44104"
        ),
        CleankrHub(
            hubId = "hub_pune_central",
            hubName = "Pune Central Hub (Swargate, Shivaji Nagar, Camp)",
            city = "Pune",
            divisionArea = "Pune Central",
            keyLocalities = "Shivaji Nagar, Swargate, Deccan, Camp, Bibwewadi, Kasba Peth",
            coveredPincodes = listOf("411005", "411004", "411002", "411009", "411030", "411037"),
            isActive = true,
            activeStaff = 10,
            contactPhone = "+91 98230 44105"
        )
    )

    val bengaluruHubs = listOf(
        CleankrHub(
            hubId = "hub_blr_south",
            hubName = "Koramangala & South Hub",
            city = "Bengaluru",
            divisionArea = "Bengaluru South",
            keyLocalities = "Koramangala, HSR Layout, BTM, Jayanagar",
            coveredPincodes = listOf("560034", "560095", "560068", "560076", "560102", "560029", "560041"),
            isActive = true,
            activeStaff = 10
        ),
        CleankrHub(
            hubId = "hub_blr_east",
            hubName = "Indiranagar & East Hub",
            city = "Bengaluru",
            divisionArea = "Bengaluru East",
            keyLocalities = "Indiranagar, Whitefield, Marathahalli",
            coveredPincodes = listOf("560038", "560008", "560075", "560066", "560037", "560087"),
            isActive = true,
            activeStaff = 10
        ),
        CleankrHub(
            hubId = "hub_blr_central",
            hubName = "Central Hub (MG Road & CBD)",
            city = "Bengaluru",
            divisionArea = "Bengaluru Central",
            keyLocalities = "MG Road, Richmond Town, Malleshwaram",
            coveredPincodes = listOf("560001", "560025", "560052", "560020", "560027", "560002"),
            isActive = true,
            activeStaff = 8
        ),
        CleankrHub(
            hubId = "hub_blr_north",
            hubName = "Hebbal & North Hub",
            city = "Bengaluru",
            divisionArea = "Bengaluru North",
            keyLocalities = "Hebbal, Yelahanka, Manyata Tech Park",
            coveredPincodes = listOf("560024", "560092", "560045", "560077", "560064"),
            isActive = true,
            activeStaff = 8
        ),
        CleankrHub(
            hubId = "hub_blr_west",
            hubName = "Rajajinagar & West Hub",
            city = "Bengaluru",
            divisionArea = "Bengaluru West",
            keyLocalities = "Rajajinagar, Vijayanagar, Basaveshwaranagar",
            coveredPincodes = listOf("560010", "560079", "560040", "560086", "560055"),
            isActive = true,
            activeStaff = 8
        )
    )

    val defaultActiveHubs: List<CleankrHub> = puneHubs

    val allActiveHubs: List<CleankrHub> = puneHubs + bengaluruHubs

    fun getActiveHubs(city: String = "Pune"): List<CleankrHub> {
        return if (city.equals("Bengaluru", ignoreCase = true)) bengaluruHubs else puneHubs
    }

    fun findActiveHubForPincode(pincode: String): CleankrHub? {
        val cleanPin = pincode.trim()
        if (cleanPin.length != 6) return null
        return allActiveHubs.firstOrNull { hub ->
            hub.isActive && hub.coveredPincodes.contains(cleanPin)
        }
    }

    fun findActiveHubForAddress(address: Address): CleankrHub? {
        return findActiveHubForPincode(address.pincode)
    }

    fun observeServicesCatalog(): Flow<List<ServiceItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(defaultServicesCatalog)
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection(FirebaseConstants.COLLECTION_SERVICES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseBackend", "Services snapshot error, using offline catalog", error)
                    trySend(defaultServicesCatalog)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val variantsRaw = doc.get("variants") as? List<Map<String, Any>> ?: emptyList()
                            val parsedVariants = variantsRaw.map { v ->
                                ServiceVariant(
                                    id = v["id"] as? String ?: "",
                                    name = v["name"] as? String ?: "",
                                    description = v["description"] as? String ?: "",
                                    price = (v["price"] as? Number)?.toInt() ?: 0,
                                    durationMinutes = (v["durationMinutes"] as? Number)?.toInt() ?: 60
                                )
                            }
                            val addOnsRaw = doc.get("addOns") as? List<Map<String, Any>> ?: emptyList()
                            val parsedAddOns = addOnsRaw.map { a ->
                                AddOnItem(
                                    id = a["id"] as? String ?: "",
                                    name = a["name"] as? String ?: "",
                                    price = (a["price"] as? Number)?.toInt() ?: 0,
                                    description = a["description"] as? String ?: ""
                                )
                            }
                            ServiceItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                categoryId = doc.getString("categoryId") ?: "",
                                categoryName = doc.getString("categoryName") ?: "",
                                description = doc.getString("description") ?: "",
                                basePrice = doc.getLong("basePrice")?.toInt() ?: 0,
                                durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 60,
                                rating = (doc.getDouble("rating") ?: 4.8).toFloat(),
                                reviewCount = doc.getLong("reviewCount")?.toInt() ?: 100,
                                imageUrl = doc.getString("imageUrl") ?: "",
                                isPopular = doc.getBoolean("isPopular") ?: false,
                                variants = parsedVariants,
                                addOns = parsedAddOns
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(if (list.isNotEmpty()) list else defaultServicesCatalog)
                } else {
                    trySend(defaultServicesCatalog)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun validateAndCalculatePrice(
        serviceId: String,
        variantName: String,
        quantity: Int,
        selectedAddOns: List<AddOnItem>
    ): PriceValidationResult {
        val catalog = defaultServicesCatalog
        val service = catalog.firstOrNull { it.id == serviceId }
            ?: return PriceValidationResult(false, 0, 0, 0, "Service not found in catalog")

        val variant = service.variants.firstOrNull { it.name.equals(variantName, ignoreCase = true) }
            ?: service.variants.firstOrNull()

        val unitPrice = variant?.price ?: service.basePrice
        val baseTotal = unitPrice * quantity.coerceAtLeast(1)
        val addOnTotal = selectedAddOns.sumOf { it.price }
        val grandTotal = baseTotal + addOnTotal

        return PriceValidationResult(
            isValid = true,
            calculatedBasePrice = baseTotal,
            calculatedAddOnPrice = addOnTotal,
            calculatedTotal = grandTotal
        )
    }

    suspend fun createBookingInSharedBackend(booking: Booking): Result<String> {
        val db = firestore ?: return Result.success(booking.id)
        return try {
            val map = hashMapOf<String, Any?>(
                "id" to booking.id,
                "customerId" to booking.customerId,
                "serviceId" to booking.serviceId,
                "serviceTitle" to booking.serviceTitle,
                "categoryName" to booking.categoryName,
                "variantName" to booking.variantName,
                "quantity" to booking.quantity,
                "basePrice" to booking.basePrice,
                "addOnPrice" to booking.addOnPrice,
                "totalAmount" to booking.totalAmount,
                "hubId" to booking.hubId,
                "hubName" to booking.hubName,
                "bookingDate" to booking.bookingDate,
                "slotTime" to booking.slotTime,
                "address" to mapOf(
                    "id" to booking.address.id,
                    "label" to booking.address.label,
                    "flatNo" to booking.address.flatNo,
                    "street" to booking.address.street,
                    "city" to booking.address.city,
                    "pincode" to booking.address.pincode,
                    "phone" to booking.address.contactPhone
                ),
                "instructions" to booking.instructions,
                "paymentMethod" to booking.paymentMethod,
                "paymentStatus" to booking.paymentStatus,
                "status" to booking.status,
                "startPin" to booking.startPin,
                "createdAt" to booking.createdAt,
                "updatedAt" to booking.updatedAt
            )
            db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(booking.id).set(map, SetOptions.merge()).await()
            Result.success(booking.id)
        } catch (e: Exception) {
            Log.e("FirebaseBackend", "Failed to save booking to Firestore", e)
            Result.success(booking.id) // Fallback local
        }
    }

    suspend fun rescheduleBookingInSharedBackend(bookingId: String, newDate: String, newSlot: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(bookingId).update(
                mapOf(
                    "bookingDate" to newDate,
                    "slotTime" to newSlot,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseBackend", "Failed to reschedule in backend", e)
            Result.success(Unit)
        }
    }

    suspend fun cancelBookingInSharedBackend(bookingId: String, reason: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(bookingId).update(
                mapOf(
                    "status" to "CANCELLED",
                    "cancellationReason" to reason,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseBackend", "Failed to cancel in backend", e)
            Result.success(Unit)
        }
    }

    suspend fun submitReviewInSharedBackend(bookingId: String, rating: Float, review: String): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            db.collection(FirebaseConstants.COLLECTION_BOOKINGS).document(bookingId).update(
                mapOf(
                    "userRating" to rating,
                    "userReview" to review,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseBackend", "Failed to submit review in backend", e)
            Result.success(Unit)
        }
    }

    suspend fun recordCrossHubAttemptInBackend(attempt: CrossHubAttempt): Result<String> {
        val db = firestore ?: return Result.success(attempt.id)
        return try {
            val map = hashMapOf<String, Any?>(
                "id" to attempt.id,
                "customerId" to attempt.customerId,
                "customerName" to attempt.customerName,
                "customerPhone" to attempt.customerPhone,
                "serviceTitle" to attempt.serviceTitle,
                "variantName" to attempt.variantName,
                "estimatedAmount" to attempt.estimatedAmount,
                "attemptedAddressText" to attempt.attemptedAddressText,
                "attemptedPincode" to attempt.attemptedPincode,
                "attemptedCity" to attempt.attemptedCity,
                "customerSelectedHubId" to attempt.customerSelectedHubId,
                "customerSelectedHubName" to attempt.customerSelectedHubName,
                "actualDetectedHubId" to attempt.actualDetectedHubId,
                "actualDetectedHubName" to attempt.actualDetectedHubName,
                "attemptType" to attempt.attemptType,
                "reason" to attempt.reason,
                "status" to attempt.status,
                "assignedHubId" to attempt.assignedHubId,
                "assignedHubName" to attempt.assignedHubName,
                "adminNotes" to attempt.adminNotes,
                "timestamp" to attempt.timestamp
            )
            db.collection(FirebaseConstants.COLLECTION_CROSS_HUB_ATTEMPTS)
                .document(attempt.id)
                .set(map, SetOptions.merge())
                .await()
            Result.success(attempt.id)
        } catch (e: Exception) {
            Log.e("FirebaseBackend", "Failed to record cross hub attempt in Firestore", e)
            Result.success(attempt.id)
        }
    }

    fun observeCrossHubAttemptsFromBackend(): Flow<List<CrossHubAttempt>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection(FirebaseConstants.COLLECTION_CROSS_HUB_ATTEMPTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirebaseBackend", "CrossHubAttempts snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            CrossHubAttempt(
                                id = doc.id,
                                customerId = doc.getString("customerId") ?: "",
                                customerName = doc.getString("customerName") ?: "",
                                customerPhone = doc.getString("customerPhone") ?: "",
                                serviceTitle = doc.getString("serviceTitle") ?: "",
                                variantName = doc.getString("variantName") ?: "",
                                estimatedAmount = doc.getLong("estimatedAmount")?.toInt() ?: 0,
                                attemptedAddressText = doc.getString("attemptedAddressText") ?: "",
                                attemptedPincode = doc.getString("attemptedPincode") ?: "",
                                attemptedCity = doc.getString("attemptedCity") ?: "Pune",
                                customerSelectedHubId = doc.getString("customerSelectedHubId") ?: "",
                                customerSelectedHubName = doc.getString("customerSelectedHubName") ?: "",
                                actualDetectedHubId = doc.getString("actualDetectedHubId"),
                                actualDetectedHubName = doc.getString("actualDetectedHubName"),
                                attemptType = doc.getString("attemptType") ?: "CROSS_HUB",
                                reason = doc.getString("reason") ?: "",
                                status = doc.getString("status") ?: "NEW_ALERT",
                                assignedHubId = doc.getString("assignedHubId"),
                                assignedHubName = doc.getString("assignedHubName"),
                                adminNotes = doc.getString("adminNotes"),
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateCrossHubAttemptInBackend(
        attemptId: String,
        status: String,
        notes: String?,
        assignedHubId: String?,
        assignedHubName: String?
    ): Result<Unit> {
        val db = firestore ?: return Result.success(Unit)
        return try {
            val updates = mutableMapOf<String, Any?>("status" to status)
            if (notes != null) updates["adminNotes"] = notes
            if (assignedHubId != null) updates["assignedHubId"] = assignedHubId
            if (assignedHubName != null) updates["assignedHubName"] = assignedHubName

            db.collection(FirebaseConstants.COLLECTION_CROSS_HUB_ATTEMPTS)
                .document(attemptId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseBackend", "Failed to update cross hub attempt in backend", e)
            Result.success(Unit)
        }
    }

    val defaultServicesCatalog: List<ServiceItem> = listOf(
        ServiceItem(
            id = "srv_bath_intense",
            title = "Bathroom Intense Clean",
            categoryId = "cat_bathroom",
            categoryName = "Bathroom",
            description = "High-pressure scrub, descaling, sanitary stain removal & disinfection.",
            basePrice = 450,
            durationMinutes = 60,
            rating = 4.8f,
            reviewCount = 240,
            isPopular = true,
            variants = listOf(
                ServiceVariant("v1", "1 Bathroom", "Single bathroom deep hygiene clean", 450, 60),
                ServiceVariant("v2", "2 Bathrooms", "Complete hygiene clean for 2 bathrooms", 850, 100),
                ServiceVariant("v3", "3 Bathrooms", "Full deep clean for 3 bathrooms", 1250, 140)
            ),
            addOns = listOf(
                AddOnItem("a_glass", "Glass Partition Add-on", 200, "Removes cloudy water stains and soap scum from glass partition")
            ),
            includedItems = listOf("Tile and grout deep scrubbing", "Hard water stain removal", "Toilet, sink & chrome sanitization"),
            notIncludedItems = listOf("Ceiling plaster repairs", "Plumbing replacements")
        ),
        ServiceItem(
            id = "srv_bath_movein",
            title = "Bathroom Move-in Clean",
            categoryId = "cat_bathroom",
            categoryName = "Bathroom",
            description = "Ultra-intense scrubbing for post-tenant handover or vacant flats.",
            basePrice = 550,
            durationMinutes = 75,
            rating = 4.9f,
            reviewCount = 180,
            isPopular = false,
            variants = listOf(
                ServiceVariant("v1", "1 Bathroom", "Single bathroom move-in scrub", 550, 75),
                ServiceVariant("v2", "2 Bathrooms", "Two bathrooms move-in scrub", 950, 120),
                ServiceVariant("v3", "3 Bathrooms", "Three bathrooms move-in scrub", 1350, 160)
            ),
            addOns = listOf(
                AddOnItem("a_glass", "Glass Partition Add-on", 200, "Removes cloudy water stains and soap scum from glass partition")
            ),
            includedItems = listOf("Acid-free chemical scrub", "Mirror & chrome shine restoration", "Sanitary floor and wall deep clean"),
            notIncludedItems = listOf("Painting or tile re-grouting")
        ),
        ServiceItem(
            id = "srv_bath_hardwater",
            title = "Bathroom Hard Water Removal",
            categoryId = "cat_bathroom",
            categoryName = "Bathroom",
            description = "Specialized treatment for stubborn white salt, calcium & hard water scaling.",
            basePrice = 700,
            durationMinutes = 90,
            rating = 4.9f,
            reviewCount = 210,
            isPopular = true,
            variants = listOf(
                ServiceVariant("v1", "1 Bathroom", "Complete hard water scale removal for 1 bathroom", 700, 90),
                ServiceVariant("v2", "2 Bathrooms", "Hard water scale removal for 2 bathrooms", 1100, 140),
                ServiceVariant("v3", "3 Bathrooms", "Hard water scale removal for 3 bathrooms", 1600, 180)
            ),
            addOns = listOf(
                AddOnItem("a_glass", "Glass Partition Add-on", 200, "Heavy scaling & mineral deposit removal from glass partition")
            ),
            includedItems = listOf("Heavy salt & lime scale removal", "Tap, shower & tile descaling", "Sanitary fitting polish"),
            notIncludedItems = listOf("Physical crack repair")
        ),
        ServiceItem(
            id = "srv_kitchen_clean",
            title = "Kitchen Cleaning",
            categoryId = "cat_kitchen",
            categoryName = "Kitchen",
            description = "Cabinet exterior, countertop scrub, sink descaling & grease removal.",
            basePrice = 1400,
            durationMinutes = 120,
            rating = 4.8f,
            reviewCount = 310,
            isPopular = true,
            variants = listOf(
                ServiceVariant("v1", "Kitchen Cleaning", "Complete deep kitchen hygiene clean", 1400, 120)
            ),
            addOns = listOf(
                AddOnItem("a_chimney", "Chimney", 200, "Chimney filter degreasing & exterior wipe"),
                AddOnItem("a_cabinets", "Cabinets", 250, "Internal & external cabinet deep clean"),
                AddOnItem("a_trolleys", "Trolleys", 350, "Modular trolley removal, rail wipe & sanitization")
            ),
            includedItems = listOf("Countertop & backsplash scrub", "Sink & tap descaling", "Gas stove exterior degreasing", "Floor mopping"),
            notIncludedItems = listOf("Chimney motor repair", "Appliance internal electrical repairs")
        ),
        ServiceItem(
            id = "srv_full_home",
            title = "Full Home Deep Cleaning (Flat)",
            categoryId = "cat_fullhome",
            categoryName = "Full Home",
            description = "Top-to-bottom comprehensive flat clean: rooms, kitchen, bathrooms, windows & floors.",
            basePrice = 3000,
            durationMinutes = 240,
            rating = 4.9f,
            reviewCount = 520,
            isPopular = true,
            variants = listOf(
                ServiceVariant("v1", "1 BHK", "Complete 1 BHK deep flat cleaning", 3000, 180),
                ServiceVariant("v2", "2 BHK", "Complete 2 BHK deep flat cleaning", 5000, 240),
                ServiceVariant("v3", "3 BHK", "Complete 3 BHK deep flat cleaning", 7000, 300),
                ServiceVariant("v4", "4 BHK", "Complete 4 BHK deep flat cleaning", 9200, 360)
            ),
            addOns = emptyList(),
            includedItems = listOf("All room floor machine scrubbing", "Cobweb removal & fan dusting", "Bathroom & kitchen deep clean", "Door & window frame cleaning"),
            notIncludedItems = listOf("Exterior facade rope work", "Wall painting")
        ),
        ServiceItem(
            id = "srv_balcony_clean",
            title = "Balcony Cleaning",
            categoryId = "cat_balcony",
            categoryName = "Balcony Cleaning",
            description = "Floor scrubbing, railing wipe, bird droppings removal & drainage wash.",
            basePrice = 600,
            durationMinutes = 45,
            rating = 4.7f,
            reviewCount = 140,
            isPopular = false,
            variants = listOf(
                ServiceVariant("v1", "Small Balcony", "Standard compact balcony cleaning", 600, 45),
                ServiceVariant("v2", "Big Balcony", "Large / terrace balcony deep power wash", 850, 75)
            ),
            addOns = emptyList(),
            includedItems = listOf("Floor pressure scrub", "Railing wipedown & polish", "Drainage area wash"),
            notIncludedItems = listOf("Exterior wall painting", "Bird net installation")
        ),
        ServiceItem(
            id = "srv_other_clean",
            title = "Other Cleaning",
            categoryId = "cat_other",
            categoryName = "Other Cleaning",
            description = "Fixture and specialized element cleaning: fans, glass windows & glass doors.",
            basePrice = 60,
            durationMinutes = 30,
            rating = 4.8f,
            reviewCount = 195,
            isPopular = false,
            variants = listOf(
                ServiceVariant("v1", "Ceiling Fan Cleaning", "Blade dusting, stain wipe & degrease", 60, 20),
                ServiceVariant("v2", "Exhaust Fan Cleaning", "Heavy oil & grease removal from exhaust blades", 65, 25),
                ServiceVariant("v3", "Glass Window Cleaning", "Streak-free crystal clear glass window clean", 300, 40),
                ServiceVariant("v4", "Glass Door Cleaning", "Full glass sliding/hinged door restoration", 400, 45)
            ),
            addOns = emptyList(),
            includedItems = listOf("Focused detailed spot cleaning", "Streak-free microfiber finish"),
            notIncludedItems = listOf("Electrical rewiring", "Glass replacement")
        )
    )
}
