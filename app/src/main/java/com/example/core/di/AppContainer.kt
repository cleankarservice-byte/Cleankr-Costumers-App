package com.example.core.di

import android.content.Context
import com.example.core.data.local.AppDatabase
import com.example.core.data.session.SessionManager
import com.example.core.repository.AddressRepository
import com.example.core.repository.BookingRepository
import com.example.core.repository.NotificationRepository
import com.example.core.repository.ServiceRepository
import com.example.core.repository.SlotRepository
import com.example.core.repository.SupportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val database = AppDatabase.getDatabase(context)
    val sessionManager = SessionManager(context)

    val serviceRepository = ServiceRepository()
    val slotRepository = SlotRepository()
    val addressRepository = AddressRepository(database.addressDao())
    val notificationRepository = NotificationRepository(database.notificationDao())
    val bookingRepository = BookingRepository(
        database.bookingDao(),
        database.notificationDao(),
        slotRepository,
        serviceRepository
    )
    val supportRepository = SupportRepository(database.supportTicketDao())

    init {
        // Initialize default seed data asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            addressRepository.ensureInitialAddresses()
            bookingRepository.ensureInitialData()
            notificationRepository.ensureInitialNotifications()
        }
    }
}
