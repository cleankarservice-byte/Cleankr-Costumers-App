package com.example.core.repository

import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.SupportTicketDao
import com.example.core.data.local.SupportTicketEntity
import com.example.core.data.session.SessionManager
import com.example.core.model.SupportTicket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class FaqItem(
    val category: String,
    val question: String,
    val answer: String
)

class SupportRepository(
    private val supportTicketDao: SupportTicketDao,
    private val firebaseBackend: FirebaseBackendService? = null,
    private val sessionManager: SessionManager? = null
) {

    val tickets: Flow<List<SupportTicket>> = supportTicketDao.getAllTickets().map { list ->
        list.map { it.toDomain() }
    }

    fun getFaqs(): List<FaqItem> = listOf(
        FaqItem(
            category = "Booking Help",
            question = "How do I book a cleaning service on Cleankr?",
            answer = "Choose your service category (e.g., Bathroom, Kitchen, Flat), select the required variant or number of bathrooms, pick an available date and time slot, select your delivery address, review the summary, and pay online or choose Cash on Service."
        ),
        FaqItem(
            category = "Booking Help",
            question = "Can I change my time slot after booking?",
            answer = "Yes! You can reschedule your booking free of charge up to 2 hours prior to the scheduled service time directly from the Booking Details screen."
        ),
        FaqItem(
            category = "Cancellation & Refund",
            question = "What is Cleankr's Cancellation Policy?",
            answer = "Cancellations made more than 2 hours before the service slot are 100% free with a full instant refund. Cancellations within 2 hours of the slot may incur a minimal ₹100 partner dispatch fee."
        ),
        FaqItem(
            category = "Cancellation & Refund",
            question = "How are refunds processed for online payments?",
            answer = "Online refunds are automatically credited back to your original source of payment (UPI / Bank Account / Card) within 3 to 5 business days."
        ),
        FaqItem(
            category = "Payments",
            question = "Is online payment secure?",
            answer = "Yes. All transactions are processed through 256-bit SSL encrypted PCI-DSS compliant payment gateways. Cleankr never stores your card details or banking passwords."
        ),
        FaqItem(
            category = "Service Quality & Safety",
            question = "Are Cleankr partners background-verified?",
            answer = "Every Cleankr professional undergoes police verification, background identity verification, and extensive training on industrial cleaning machinery and eco-friendly chemicals."
        ),
        FaqItem(
            category = "Service Quality & Safety",
            question = "What is the Start PIN?",
            answer = "For your safety and protection against unauthorized service starts, we generate a confidential 4-digit Start PIN for your booking. Only share this PIN with your partner once they arrive at your door and you verify their identity."
        )
    )

    suspend fun createTicket(bookingId: String?, category: String, subject: String, description: String): SupportTicket {
        val ticket = SupportTicket(
            id = "TKT-" + UUID.randomUUID().toString().take(6).uppercase(),
            bookingId = bookingId,
            category = category,
            subject = subject,
            description = description,
            status = "OPEN",
            createdAt = System.currentTimeMillis()
        )
        supportTicketDao.insertTicket(SupportTicketEntity.fromDomain(ticket))

        val customerId = sessionManager?.currentUser?.value?.id ?: "cust_001"
        if (firebaseBackend != null && firebaseBackend.isFirebaseConfigured()) {
            firebaseBackend.createSupportTicketInBackend(ticket, customerId)
        }
        return ticket
    }
}
