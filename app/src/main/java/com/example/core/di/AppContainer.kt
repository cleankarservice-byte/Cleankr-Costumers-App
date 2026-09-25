package com.example.core.di

import android.content.Context
import com.example.core.data.firebase.FirebaseBackendService
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
    val firebaseBackend = FirebaseBackendService(context)

    val serviceRepository = ServiceRepository()
    val slotRepository = SlotRepository()
    val addressRepository = AddressRepository(database.addressDao(), firebaseBackend, sessionManager)
    val notificationRepository = NotificationRepository(database.notificationDao(), firebaseBackend)
    val bookingRepository = BookingRepository(
        database.bookingDao(),
        database.notificationDao(),
        slotRepository,
        serviceRepository,
        firebaseBackend,
        sessionManager
    )
    val supportRepository = SupportRepository(database.supportTicketDao(), firebaseBackend, sessionManager)

    init {
        // Initialize default seed data asynchronously and attach real-time listeners
        CoroutineScope(Dispatchers.IO).launch {
            addressRepository.ensureInitialAddresses()
            bookingRepository.ensureInitialData()
            notificationRepository.ensureInitialNotifications()

            // Attach real-time Firestore listeners and sync FCM token for logged in customer
            val customerId = sessionManager.currentUser.value?.id ?: "cust_001"
            addressRepository.startRealtimeSync(customerId, this)
            bookingRepository.startRealtimeSync(customerId, this)
            notificationRepository.startRealtimeSync(customerId, this)

            // Proactively sync FCM token with Firebase profile
            if (sessionManager.isLoggedIn.value) {
                firebaseBackend.syncCurrentFcmToken(customerId)
            }
        }
    }
}
