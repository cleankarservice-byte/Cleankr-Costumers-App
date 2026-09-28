package com.example.core.repository

import android.util.Log
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.CrossHubAttemptDao
import com.example.core.data.local.CrossHubAttemptEntity
import com.example.core.model.CleankrHub
import com.example.core.model.CrossHubAttempt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class AdminHubRepository(
    private val crossHubAttemptDao: CrossHubAttemptDao,
    private val notificationRepository: NotificationRepository,
    private val firebaseBackend: FirebaseBackendService?
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Seed initial sample Pune cross-hub alerts so admin panel is immediately functional
        repositoryScope.launch {
            seedSamplePuneAlertsIfEmpty()
        }
    }

    fun observeAllAttempts(): Flow<List<CrossHubAttempt>> {
        return crossHubAttemptDao.getAllAttempts().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun observeAttemptsByStatus(status: String): Flow<List<CrossHubAttempt>> {
        return crossHubAttemptDao.getAttemptsByStatus(status).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun logCrossHubAttempt(
        customerId: String,
        customerName: String,
        customerPhone: String,
        serviceTitle: String,
        variantName: String,
        estimatedAmount: Int,
        attemptedAddressText: String,
        attemptedPincode: String,
        attemptedCity: String = "Pune",
        customerSelectedHubId: String,
        customerSelectedHubName: String,
        actualDetectedHub: CleankrHub?,
        reason: String
    ): CrossHubAttempt {
        val attemptId = "XHUB-${System.currentTimeMillis() % 1000000}"
        val attemptType = if (actualDetectedHub != null) "CROSS_HUB" else "OUT_OF_COVERAGE"

        val attempt = CrossHubAttempt(
            id = attemptId,
            customerId = customerId,
            customerName = customerName.ifBlank { "Customer" },
            customerPhone = customerPhone.ifBlank { "+91 98*** 00000" },
            serviceTitle = serviceTitle,
            variantName = variantName,
            estimatedAmount = estimatedAmount,
            attemptedAddressText = attemptedAddressText,
            attemptedPincode = attemptedPincode,
            attemptedCity = attemptedCity,
            customerSelectedHubId = customerSelectedHubId,
            customerSelectedHubName = customerSelectedHubName,
            actualDetectedHubId = actualDetectedHub?.hubId,
            actualDetectedHubName = actualDetectedHub?.hubName ?: "Outside Pune Hub Division",
            attemptType = attemptType,
            reason = reason,
            status = "NEW_ALERT",
            timestamp = System.currentTimeMillis()
        )

        // Save in local Room Database
        crossHubAttemptDao.insertAttempt(CrossHubAttemptEntity.fromDomain(attempt))

        // Push to Firebase Firestore for real-time admin sync
        try {
            firebaseBackend?.recordCrossHubAttemptInBackend(attempt)
        } catch (e: Exception) {
            Log.e("AdminHubRepo", "Failed to sync cross-hub attempt to backend: ${e.message}")
        }

        // Notify app
        val alertTitle = if (attemptType == "CROSS_HUB") {
            "🚨 Cross-Hub Alert: ${actualDetectedHub?.divisionArea ?: "Other Hub"}"
        } else {
            "⚠️ Out-of-Coverage Lead: $attemptedPincode"
        }
        val alertDesc = "Customer $customerName ($customerPhone) tried booking $serviceTitle from ${customerSelectedHubName}. Address area: ${actualDetectedHub?.hubName ?: "Unserved Pincode $attemptedPincode"}."

        notificationRepository.addNotification(
            title = alertTitle,
            message = alertDesc,
            type = "SECURITY"
        )

        return attempt
    }

    suspend fun routeAttemptToHub(attemptId: String, targetHub: CleankrHub, note: String) {
        crossHubAttemptDao.routeAttempt(
            id = attemptId,
            hubId = targetHub.hubId,
            hubName = targetHub.hubName,
            notes = note
        )
        try {
            firebaseBackend?.updateCrossHubAttemptInBackend(
                attemptId = attemptId,
                status = "ROUTED",
                notes = note,
                assignedHubId = targetHub.hubId,
                assignedHubName = targetHub.hubName
            )
        } catch (_: Exception) {}

        notificationRepository.addNotification(
            title = "Lead Routed to ${targetHub.hubName} ✅",
            message = "Cross-hub booking #$attemptId has been reassigned to ${targetHub.hubName} operations team.",
            type = "INFO"
        )
    }

    suspend fun updateAttemptStatus(attemptId: String, status: String, notes: String? = null) {
        crossHubAttemptDao.updateStatus(attemptId, status, notes)
        try {
            firebaseBackend?.updateCrossHubAttemptInBackend(
                attemptId = attemptId,
                status = status,
                notes = notes,
                assignedHubId = null,
                assignedHubName = null
            )
        } catch (_: Exception) {}
    }

    suspend fun simulateSampleAttempt(
        hubFrom: CleankrHub,
        hubActual: CleankrHub?,
        addressText: String,
        pincode: String,
        serviceTitle: String,
        amount: Int
    ): CrossHubAttempt {
        val names = listOf("Vikram Joshi", "Sneha Shinde", "Kedar Kulkarni", "Neha Gaikwad", "Abhishek More")
        val phones = listOf("+91 98224 55102", "+91 99701 44293", "+91 98908 11204", "+91 97632 88910")
        val randomName = names.random()
        val randomPhone = phones.random()

        val reason = if (hubActual != null) {
            "Customer selected '${hubFrom.hubName}', but location '$addressText ($pincode)' belongs to '${hubActual.hubName}'"
        } else {
            "Customer location '$addressText ($pincode)' is outside active Pune hub divisions"
        }

        return logCrossHubAttempt(
            customerId = "cust_sim_${System.currentTimeMillis() % 1000}",
            customerName = randomName,
            customerPhone = randomPhone,
            serviceTitle = serviceTitle,
            variantName = "Standard Clean",
            estimatedAmount = amount,
            attemptedAddressText = addressText,
            attemptedPincode = pincode,
            attemptedCity = "Pune",
            customerSelectedHubId = hubFrom.hubId,
            customerSelectedHubName = hubFrom.hubName,
            actualDetectedHub = hubActual,
            reason = reason
        )
    }

    private suspend fun seedSamplePuneAlertsIfEmpty() {
        val sample1 = CrossHubAttemptEntity(
            id = "XHUB-918204",
            customerId = "cust_pune_101",
            customerName = "Rohan Deshmukh",
            customerPhone = "+91 98231 10423",
            serviceTitle = "Full Home Deep Cleaning",
            variantName = "2 BHK Flat",
            estimatedAmount = 3499,
            attemptedAddressText = "Flat 402, Rohan Tarang, Near Datta Mandir, Wakad, Pune",
            attemptedPincode = "411057",
            attemptedCity = "Pune",
            customerSelectedHubId = "hub_pune_east",
            customerSelectedHubName = "Pune East Hub (Viman Nagar, Kharadi)",
            actualDetectedHubId = "hub_pune_north",
            actualDetectedHubName = "Pune North / PCMC Hub (Hinjawadi, Wakad)",
            attemptType = "CROSS_HUB",
            reason = "Customer selected Pune East Hub (Viman Nagar), but delivery address is in Wakad (Pune North Hub)",
            status = "NEW_ALERT",
            assignedHubId = null,
            assignedHubName = null,
            adminNotes = null,
            timestamp = System.currentTimeMillis() - (15 * 60 * 1000) // 15 mins ago
        )

        val sample2 = CrossHubAttemptEntity(
            id = "XHUB-882319",
            customerId = "cust_pune_102",
            customerName = "Pooja Kulkarni",
            customerPhone = "+91 98902 33145",
            serviceTitle = "Bathroom Intense Clean",
            variantName = "2 Bathrooms",
            estimatedAmount = 850,
            attemptedAddressText = "B-301, Mayur Colony, Near MIT College, Kothrud, Pune",
            attemptedPincode = "411038",
            attemptedCity = "Pune",
            customerSelectedHubId = "hub_pune_central",
            customerSelectedHubName = "Pune Central Hub (Swargate, Shivaji Nagar, Camp)",
            actualDetectedHubId = "hub_pune_west",
            actualDetectedHubName = "Pune West Hub (Kothrud, Baner, Aundh)",
            attemptType = "CROSS_HUB",
            reason = "Customer tried booking from Central Pune Hub while address is inside Kothrud / Pune West Hub",
            status = "NEW_ALERT",
            assignedHubId = null,
            assignedHubName = null,
            adminNotes = null,
            timestamp = System.currentTimeMillis() - (45 * 60 * 1000) // 45 mins ago
        )

        val sample3 = CrossHubAttemptEntity(
            id = "XHUB-773412",
            customerId = "cust_pune_103",
            customerName = "Amit Patil",
            customerPhone = "+91 94220 88912",
            serviceTitle = "Kitchen Deep Degreasing",
            variantName = "Standard Kitchen",
            estimatedAmount = 1199,
            attemptedAddressText = "Row House 12, Wonder City, Katraj-Dehu Bypass Road, Pune",
            attemptedPincode = "411046",
            attemptedCity = "Pune",
            customerSelectedHubId = "hub_pune_southeast",
            customerSelectedHubName = "Pune South-East Hub (Hadapsar, Magarpatta)",
            actualDetectedHubId = null,
            actualDetectedHubName = "Outside Pune Hub Division (Katraj Bypass)",
            attemptType = "OUT_OF_COVERAGE",
            reason = "Customer attempted booking in unserved Katraj periphery (Pincode 411046) from Hadapsar Hub",
            status = "NEW_ALERT",
            assignedHubId = null,
            assignedHubName = null,
            adminNotes = null,
            timestamp = System.currentTimeMillis() - (2 * 3600 * 1000) // 2 hours ago
        )

        crossHubAttemptDao.insertAttempts(listOf(sample1, sample2, sample3))
    }
}
