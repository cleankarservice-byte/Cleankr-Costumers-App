package com.example.core.di

import android.content.Context
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.AppDatabase
import com.example.core.data.session.SessionManager
import com.example.core.repository.AddressRepository
import com.example.core.repository.AdminHubRepository
import com.example.core.repository.BookingRepository
import com.example.core.repository.NotificationRepository
import com.example.core.repository.ServiceRepository
import com.example.core.repository.SlotRepository
import com.example.core.repository.SupportRepository

class AppContainer(val context: Context) {
    val database: AppDatabase by lazy { AppDatabase.getInstance(context) }
    val sessionManager: SessionManager by lazy { SessionManager(context) }
    val firebaseBackend: FirebaseBackendService by lazy { FirebaseBackendService(context) }

    val serviceRepository: ServiceRepository by lazy {
        ServiceRepository(firebaseBackend)
    }

    val addressRepository: AddressRepository by lazy {
        AddressRepository(database.addressDao())
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(database.notificationDao())
    }

    val slotRepository: SlotRepository by lazy {
        SlotRepository()
    }

    val supportRepository: SupportRepository by lazy {
        SupportRepository(database.supportTicketDao())
    }

    val bookingRepository: BookingRepository by lazy {
        BookingRepository(database.bookingDao(), notificationRepository, firebaseBackend)
    }

    val adminHubRepository: AdminHubRepository by lazy {
        AdminHubRepository(database.crossHubAttemptDao(), notificationRepository, firebaseBackend)
    }
}
