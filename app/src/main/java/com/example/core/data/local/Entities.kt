package com.example.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.BookingStatus
import com.example.core.model.NotificationItem
import com.example.core.model.PartnerInfo
import com.example.core.model.PaymentMethod
import com.example.core.model.PaymentStatus
import com.example.core.model.ServiceCategory
import com.example.core.model.SupportTicket

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val serviceId: String,
    val serviceTitle: String,
    val categoryName: String,
    val variantName: String,
    val quantity: Int,
    val selectedAddOnsJson: String, // Comma separated
    val addOnsTotal: Int,
    val servicePrice: Int,
    val totalAmount: Int,
    val bookingDate: String,
    val slotTime: String,
    // Flat Address fields
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
    val cancellationReason: String?,
    val userRating: Float?,
    val userReview: String?
) {
    fun toDomain(): Booking {
        val address = Address(
            id = addressId,
            label = addressLabel,
            flatNo = addressFlatNo,
            street = addressStreet,
            landmark = addressLandmark,
            city = addressCity,
            pincode = addressPincode,
            contactPhone = addressPhone
        )
        val partner = if (partnerId != null && partnerName != null) {
            PartnerInfo(
                id = partnerId,
                name = partnerName,
                rating = partnerRating ?: 4.9f,
                jobsCompleted = partnerJobs ?: 120,
                maskedPhone = partnerMaskedPhone ?: "+91 80000 00000"
            )
        } else null

        val cat = try {
            ServiceCategory.valueOf(categoryName)
        } catch (_: Exception) {
            ServiceCategory.BATHROOM
        }

        val parsedAddOns = if (selectedAddOnsJson.isBlank()) emptyList() else selectedAddOnsJson.split("|||")

        return Booking(
            id = id,
            serviceId = serviceId,
            serviceTitle = serviceTitle,
            category = cat,
            variantName = variantName,
            quantity = quantity,
            selectedAddOns = parsedAddOns,
            addOnsTotal = addOnsTotal,
            servicePrice = servicePrice,
            totalAmount = totalAmount,
            bookingDate = bookingDate,
            slotTime = slotTime,
            address = address,
            instructions = instructions,
            paymentMethod = try { PaymentMethod.valueOf(paymentMethod) } catch (_: Exception) { PaymentMethod.ONLINE },
            paymentStatus = try { PaymentStatus.valueOf(paymentStatus) } catch (_: Exception) { PaymentStatus.PENDING },
            status = try { BookingStatus.valueOf(status) } catch (_: Exception) { BookingStatus.BOOKED },
            partner = partner,
            startPin = startPin,
            createdAt = createdAt,
            cancellationReason = cancellationReason,
            userRating = userRating,
            userReview = userReview
        )
    }

    companion object {
        fun fromDomain(b: Booking): BookingEntity {
            return BookingEntity(
                id = b.id,
                serviceId = b.serviceId,
                serviceTitle = b.serviceTitle,
                categoryName = b.category.name,
                variantName = b.variantName,
                quantity = b.quantity,
                selectedAddOnsJson = b.selectedAddOns.joinToString("|||"),
                addOnsTotal = b.addOnsTotal,
                servicePrice = b.servicePrice,
                totalAmount = b.totalAmount,
                bookingDate = b.bookingDate,
                slotTime = b.slotTime,
                addressId = b.address.id,
                addressLabel = b.address.label,
                addressFlatNo = b.address.flatNo,
                addressStreet = b.address.street,
                addressLandmark = b.address.landmark,
                addressCity = b.address.city,
                addressPincode = b.address.pincode,
                addressPhone = b.address.contactPhone,
                instructions = b.instructions,
                paymentMethod = b.paymentMethod.name,
                paymentStatus = b.paymentStatus.name,
                status = b.status.name,
                partnerId = b.partner?.id,
                partnerName = b.partner?.name,
                partnerRating = b.partner?.rating,
                partnerJobs = b.partner?.jobsCompleted,
                partnerMaskedPhone = b.partner?.maskedPhone,
                startPin = b.startPin,
                createdAt = b.createdAt,
                cancellationReason = b.cancellationReason,
                userRating = b.userRating,
                userReview = b.userReview
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
        fun fromDomain(a: Address): AddressEntity = AddressEntity(
            id = a.id,
            label = a.label,
            flatNo = a.flatNo,
            street = a.street,
            landmark = a.landmark,
            city = a.city,
            pincode = a.pincode,
            contactPhone = a.contactPhone,
            instructions = a.instructions,
            isDefault = a.isDefault
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
        fun fromDomain(n: NotificationItem): NotificationEntity = NotificationEntity(
            id = n.id,
            title = n.title,
            message = n.message,
            timestamp = n.timestamp,
            type = n.type,
            isRead = n.isRead,
            bookingId = n.bookingId
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
        fun fromDomain(t: SupportTicket): SupportTicketEntity = SupportTicketEntity(
            id = t.id,
            bookingId = t.bookingId,
            category = t.category,
            subject = t.subject,
            description = t.description,
            status = t.status,
            createdAt = t.createdAt
        )
    }
}
