package com.example.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceItem(
    val id: String = "",
    val title: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val description: String = "",
    val basePrice: Int = 0,
    val durationMinutes: Int = 60,
    val rating: Float = 4.8f,
    val reviewCount: Int = 120,
    val imageUrl: String = "",
    val isPopular: Boolean = false,
    val variants: List<ServiceVariant> = emptyList(),
    val addOns: List<AddOnItem> = emptyList(),
    val includedItems: List<String> = emptyList(),
    val notIncludedItems: List<String> = emptyList()
)

@Serializable
data class ServiceVariant(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val durationMinutes: Int = 60
)

@Serializable
data class AddOnItem(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val description: String = "",
    val isSelected: Boolean = false
)

@Serializable
data class ServiceCategory(
    val id: String = "",
    val name: String = "",
    val iconName: String = "",
    val description: String = "",
    val serviceCount: Int = 0
)

@Serializable
data class CleankrHub(
    val hubId: String = "",
    val hubName: String = "",
    val city: String = "Pune",
    val divisionArea: String = "Pune Hub Division",
    val keyLocalities: String = "",
    val coveredPincodes: List<String> = emptyList(),
    val isActive: Boolean = true,
    val activeStaff: Int = 12,
    val contactPhone: String = "+91 80000 12345"
)

@Serializable
data class CrossHubAttempt(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val serviceTitle: String = "",
    val variantName: String = "",
    val estimatedAmount: Int = 0,
    val attemptedAddressText: String = "",
    val attemptedPincode: String = "",
    val attemptedCity: String = "Pune",
    val customerSelectedHubId: String = "",
    val customerSelectedHubName: String = "",
    val actualDetectedHubId: String? = null,
    val actualDetectedHubName: String? = null,
    val attemptType: String = "CROSS_HUB", // CROSS_HUB, OUT_OF_COVERAGE
    val reason: String = "",
    val status: String = "NEW_ALERT", // NEW_ALERT, ROUTED, CONTACTED, RESOLVED
    val assignedHubId: String? = null,
    val assignedHubName: String? = null,
    val adminNotes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Booking(
    val id: String = "",
    val customerId: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val categoryName: String = "",
    val variantName: String = "",
    val quantity: Int = 1,
    val selectedAddOns: List<AddOnItem> = emptyList(),
    val addOnsTotal: Int = 0,
    val servicePrice: Int = 0,
    val totalAmount: Int = 0,
    val basePrice: Int = 0,
    val addOnPrice: Int = 0,
    val hubId: String? = null,
    val hubName: String? = null,
    val bookingDate: String = "",
    val slotTime: String = "",
    val address: Address = Address(),
    val instructions: String = "",
    val paymentMethod: String = "COD",
    val paymentStatus: String = "PENDING",
    val status: String = "CONFIRMED", // CONFIRMED, ASSIGNED, PARTNER_ARRIVED, IN_PROGRESS, COMPLETED, CANCELLED
    val partnerId: String? = null,
    val partnerName: String? = null,
    val partnerRating: Float? = null,
    val partnerJobs: Int? = null,
    val partnerMaskedPhone: String? = null,
    val startPin: String = "", // 4-digit Doorstep Verification PIN
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val cancellationReason: String? = null,
    val userRating: Float? = null,
    val userReview: String? = null
)

@Serializable
data class Address(
    val id: String = "",
    val label: String = "Home", // Home, Office, Other
    val flatNo: String = "",
    val street: String = "",
    val landmark: String = "",
    val city: String = "Pune",
    val pincode: String = "",
    val contactPhone: String = "",
    val instructions: String = "",
    val isDefault: Boolean = false
)

@Serializable
data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "INFO", // BOOKING, OFFER, SYSTEM, SECURITY
    val isRead: Boolean = false,
    val bookingId: String? = null
)

@Serializable
data class SupportTicket(
    val id: String = "",
    val bookingId: String? = null,
    val category: String = "GENERAL",
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val referralCode: String = "",
    val referralEarnings: Int = 0,
    val language: String = "en" // "en" or "hi"
)
