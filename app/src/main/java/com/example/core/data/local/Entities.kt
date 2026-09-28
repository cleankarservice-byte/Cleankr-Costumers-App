package com.example.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.AddOnItem
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.CrossHubAttempt
import com.example.core.model.NotificationItem
import com.example.core.model.SupportTicket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val serviceId: String,
    val serviceTitle: String,
    val categoryName: String,
    val variantName: String,
    val quantity: Int,
    val selectedAddOnsJson: String,
    val addOnsTotal: Int,
    val servicePrice: Int,
    val totalAmount: Int,
    val basePrice: Int,
    val addOnPrice: Int,
    val hubId: String?,
    val hubName: String?,
    val bookingDate: String,
    val slotTime: String,
    val addressId: String,
    val addressLabel: String,
    val addressFlatNo: String,
    val addressStreet: String,
    val addressLandmark: String,
    val addressCity: String,
    val addressPincode: String,
    val addressPhone: String,
    val instructions: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val status: String,
    val partnerId: String?,
    val partnerName: String?,
    val partnerRating: Float?,
    val partnerJobs: Int?,
    val partnerMaskedPhone: String?,
    val startPin: String,
    val createdAt: Long,
    val updatedAt: Long,
    val cancellationReason: String?,
    val userRating: Float?,
    val userReview: String?
) {
    fun toDomain(): Booking {
        val addOns = try {
            if (selectedAddOnsJson.isNotBlank()) {
                Json.decodeFromString<List<AddOnItem>>(selectedAddOnsJson)
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        return Booking(
            id = id,
            customerId = customerId,
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            categoryName = categoryName,
            variantName = variantName,
            quantity = quantity,
            selectedAddOns = addOns,
            addOnsTotal = addOnsTotal,
            servicePrice = servicePrice,
            totalAmount = totalAmount,
            basePrice = basePrice,
            addOnPrice = addOnPrice,
            hubId = hubId,
            hubName = hubName,
            bookingDate = bookingDate,
            slotTime = slotTime,
            address = Address(
                id = addressId,
                label = addressLabel,
                flatNo = addressFlatNo,
                street = addressStreet,
                landmark = addressLandmark,
                city = addressCity,
                pincode = addressPincode,
                contactPhone = addressPhone
            ),
            instructions = instructions,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            status = status,
            partnerId = partnerId,
            partnerName = partnerName,
            partnerRating = partnerRating,
            partnerJobs = partnerJobs,
            partnerMaskedPhone = partnerMaskedPhone,
            startPin = startPin,
            createdAt = createdAt,
            updatedAt = updatedAt,
            cancellationReason = cancellationReason,
            userRating = userRating,
            userReview = userReview
        )
    }

    companion object {
        fun fromDomain(domain: Booking): BookingEntity {
            val jsonAddOns = try {
                Json.encodeToString(domain.selectedAddOns)
            } catch (_: Exception) {
                "[]"
            }
            return BookingEntity(
                id = domain.id,
                customerId = domain.customerId,
                serviceId = domain.serviceId,
                serviceTitle = domain.serviceTitle,
                categoryName = domain.categoryName,
                variantName = domain.variantName,
                quantity = domain.quantity,
                selectedAddOnsJson = jsonAddOns,
                addOnsTotal = domain.addOnsTotal,
                servicePrice = domain.servicePrice,
                totalAmount = domain.totalAmount,
                basePrice = domain.basePrice,
                addOnPrice = domain.addOnPrice,
                hubId = domain.hubId,
                hubName = domain.hubName,
                bookingDate = domain.bookingDate,
                slotTime = domain.slotTime,
                addressId = domain.address.id,
                addressLabel = domain.address.label,
                addressFlatNo = domain.address.flatNo,
                addressStreet = domain.address.street,
                addressLandmark = domain.address.landmark,
                addressCity = domain.address.city,
                addressPincode = domain.address.pincode,
                addressPhone = domain.address.contactPhone,
                instructions = domain.instructions,
                paymentMethod = domain.paymentMethod,
                paymentStatus = domain.paymentStatus,
                status = domain.status,
                partnerId = domain.partnerId,
                partnerName = domain.partnerName,
                partnerRating = domain.partnerRating,
                partnerJobs = domain.partnerJobs,
                partnerMaskedPhone = domain.partnerMaskedPhone,
                startPin = domain.startPin,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                cancellationReason = domain.cancellationReason,
                userRating = domain.userRating,
                userReview = domain.userReview
            )
        }
    }
}

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val label: String,
    val flatNo: String,
    val street: String,
    val landmark: String,
    val city: String,
    val pincode: String,
    val contactPhone: String,
    val instructions: String,
    val isDefault: Boolean
) {
    fun toDomain(): Address = Address(
        id = id,
        label = label,
        flatNo = flatNo,
        street = street,
        landmark = landmark,
        city = city,
        pincode = pincode,
        contactPhone = contactPhone,
        instructions = instructions,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(domain: Address): AddressEntity = AddressEntity(
            id = domain.id,
            label = domain.label,
            flatNo = domain.flatNo,
            street = domain.street,
            landmark = domain.landmark,
            city = domain.city,
            pincode = domain.pincode,
            contactPhone = domain.contactPhone,
            instructions = domain.instructions,
            isDefault = domain.isDefault
        )
    }
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String,
    val isRead: Boolean,
    val bookingId: String?
) {
    fun toDomain(): NotificationItem = NotificationItem(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        type = type,
        isRead = isRead,
        bookingId = bookingId
    )

    companion object {
        fun fromDomain(domain: NotificationItem): NotificationEntity = NotificationEntity(
            id = domain.id,
            title = domain.title,
            message = domain.message,
            timestamp = domain.timestamp,
            type = domain.type,
            isRead = domain.isRead,
            bookingId = domain.bookingId
        )
    }
}

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val bookingId: String?,
    val category: String,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: Long
) {
    fun toDomain(): SupportTicket = SupportTicket(
        id = id,
        bookingId = bookingId,
        category = category,
        subject = subject,
        description = description,
        status = status,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: SupportTicket): SupportTicketEntity = SupportTicketEntity(
            id = domain.id,
            bookingId = domain.bookingId,
            category = domain.category,
            subject = domain.subject,
            description = domain.description,
            status = domain.status,
            createdAt = domain.createdAt
        )
    }
}

@Entity(tableName = "cross_hub_attempts")
data class CrossHubAttemptEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val serviceTitle: String,
    val variantName: String,
    val estimatedAmount: Int,
    val attemptedAddressText: String,
    val attemptedPincode: String,
    val attemptedCity: String,
    val customerSelectedHubId: String,
    val customerSelectedHubName: String,
    val actualDetectedHubId: String?,
    val actualDetectedHubName: String?,
    val attemptType: String,
    val reason: String,
    val status: String,
    val assignedHubId: String?,
    val assignedHubName: String?,
    val adminNotes: String?,
    val timestamp: Long
) {
    fun toDomain(): CrossHubAttempt = CrossHubAttempt(
        id = id,
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        serviceTitle = serviceTitle,
        variantName = variantName,
        estimatedAmount = estimatedAmount,
        attemptedAddressText = attemptedAddressText,
        attemptedPincode = attemptedPincode,
        attemptedCity = attemptedCity,
        customerSelectedHubId = customerSelectedHubId,
        customerSelectedHubName = customerSelectedHubName,
        actualDetectedHubId = actualDetectedHubId,
        actualDetectedHubName = actualDetectedHubName,
        attemptType = attemptType,
        reason = reason,
        status = status,
        assignedHubId = assignedHubId,
        assignedHubName = assignedHubName,
        adminNotes = adminNotes,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(domain: CrossHubAttempt): CrossHubAttemptEntity = CrossHubAttemptEntity(
            id = domain.id,
            customerId = domain.customerId,
            customerName = domain.customerName,
            customerPhone = domain.customerPhone,
            serviceTitle = domain.serviceTitle,
            variantName = domain.variantName,
            estimatedAmount = domain.estimatedAmount,
            attemptedAddressText = domain.attemptedAddressText,
            attemptedPincode = domain.attemptedPincode,
            attemptedCity = domain.attemptedCity,
            customerSelectedHubId = domain.customerSelectedHubId,
            customerSelectedHubName = domain.customerSelectedHubName,
            actualDetectedHubId = domain.actualDetectedHubId,
            actualDetectedHubName = domain.actualDetectedHubName,
            attemptType = domain.attemptType,
            reason = domain.reason,
            status = domain.status,
            assignedHubId = domain.assignedHubId,
            assignedHubName = domain.assignedHubName,
            adminNotes = domain.adminNotes,
            timestamp = domain.timestamp
        )
    }
}

