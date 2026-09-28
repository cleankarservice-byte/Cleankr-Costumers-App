package com.example.core.repository

import com.example.core.data.local.SupportTicketDao
import com.example.core.data.local.SupportTicketEntity
import com.example.core.model.SupportTicket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class SupportRepository(private val supportTicketDao: SupportTicketDao) {

    fun observeTickets(): Flow<List<SupportTicket>> {
        return supportTicketDao.getAllTickets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createTicket(bookingId: String?, category: String, subject: String, description: String): SupportTicket {
        val ticket = SupportTicket(
            id = "TKT-${System.currentTimeMillis() % 100000}",
            bookingId = bookingId,
            category = category,
            subject = subject,
            description = description,
            status = "OPEN",
            createdAt = System.currentTimeMillis()
        )
        supportTicketDao.insertTicket(SupportTicketEntity.fromDomain(ticket))
        return ticket
    }
}
