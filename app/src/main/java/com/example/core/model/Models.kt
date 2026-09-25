package com.example.core.model

enum class ServiceCategory(val displayName: String, val iconDescription: String) {
    BATHROOM("Bathroom", "Bathrooms & Washrooms"),
    KITCHEN("Kitchen", "Kitchens & Pantries"),
    FLAT("Flat", "Full Apartments & BHKs"),
    BALCONY("Balcony", "Balcony & Verandas"),
    OTHER("Other", "Specialized Services")
}

data class ServiceVariant(
    val id: String,
    val name: String,
    val price: Int,
    val durationText: String
)

data class AddOnItem(
    val id: String,
    val name: String,
    val price: Int,
    val description: String
)

data class ServiceItem(
    val id: String,
    val category: ServiceCategory,
    val title: String,
    val shortDesc: String,
    val fullDesc: String,
    val rating: Float,
    val reviewsCount: Int,
    val durationText: String,
    val basePrice: Int,
    val variants: List<ServiceVariant>,
    val addOns: List<AddOnItem>,
    val included: List<String>,
    val notIncluded: List<String>,
    val importantNotes: List<String>
)

data class Address(
    val id: String,
    val label: String, // "Home", "Work", "Other"
    val flatNo: String,
    val street: String,
    val landmark: String,
    val city: String,
    val pincode: String,
    val contactPhone: String = "",
    val instructions: String = "",
    val isDefault: Boolean = false
) {
    val fullAddressText: String
        get() = listOf(flatNo, street, landmark, "$city - $pincode")
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

data class TimeSlot(
    val id: String,
    val timeDisplay: String,
    val period: String, // "Morning", "Afternoon", "Evening"
    val isAvailable: Boolean = true,
    val unavailableReason: String? = null
)

enum class BookingStatus(val displayName: String, val stepIndex: Int) {
    BOOKED("Booked", 0),
    ASSIGNED("Assigned", 1),
    PARTNER_ACCEPTED("Partner Accepted", 2),
    ON_THE_WAY("On the Way", 3),
    ARRIVED("Arrived", 4),
    STARTED("Started", 5),
    COMPLETED("Completed", 6),
    CANCELLED("Cancelled", -1);

    val isTerminal: Boolean
        get() = this == COMPLETED || this == CANCELLED
}

data class PartnerInfo(
    val id: String,
    val name: String,
    val rating: Float,
    val jobsCompleted: Int,
    val maskedPhone: String,
    val badge: String = "Cleankr Verified Pro"
)

enum class PaymentMethod(val displayName: String) {
    ONLINE("Online Payment (UPI/Card)"),
    CASH("Cash on Service")
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PAID("Paid Securely"),
    REFUNDED("Refund Processed")
}

data class Booking(
    val id: String,
    val customerId: String = "cust_001",
    val serviceId: String,
    val serviceTitle: String,
    val category: ServiceCategory,
    val variantName: String,
    val quantity: Int,
    val selectedAddOns: List<String>,
    val addOnsTotal: Int,
    val servicePrice: Int,
    val totalAmount: Int,
    val bookingDate: String,
    val slotTime: String,
    val address: Address,
    val instructions: String,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val status: BookingStatus,
    val partner: PartnerInfo? = null,
    val startPin: String = "4821",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val cancellationReason: String? = null,
    val userRating: Float? = null,
    val userReview: String? = null
)

data class CustomerUser(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String = "customer",
    val isVerified: Boolean = true,
    val hasPinSet: Boolean = false,
    val pinHash: String = ""
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String, // "BOOKING", "PARTNER", "OFFER", "SECURITY"
    val isRead: Boolean = false,
    val bookingId: String? = null
)

data class SupportTicket(
    val id: String,
    val bookingId: String?,
    val category: String,
    val subject: String,
    val description: String,
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis()
)
