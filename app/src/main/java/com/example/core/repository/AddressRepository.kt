package com.example.core.repository

import android.util.Log
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.AddressDao
import com.example.core.data.local.AddressEntity
import com.example.core.data.session.SessionManager
import com.example.core.model.Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class AddressRepository(
    private val addressDao: AddressDao,
    private val firebaseBackend: FirebaseBackendService? = null,
    private val sessionManager: SessionManager? = null
) {
    private val tag = "AddressRepo"

    val addresses: Flow<List<Address>> = addressDao.getAllAddresses().map { list ->
        list.map { it.toDomain() }
    }

    val defaultAddress: Flow<Address?> = addressDao.getDefaultAddress().map { it?.toDomain() }

    fun startRealtimeSync(customerId: String, scope: CoroutineScope) {
        val fb = firebaseBackend ?: return
        if (!fb.isFirebaseConfigured()) return

        scope.launch {
            try {
                fb.observeCustomerAddresses(customerId).collect { backendAddresses ->
                    if (backendAddresses.isNotEmpty()) {
                        addressDao.insertAddresses(backendAddresses.map { AddressEntity.fromDomain(it) })
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Address sync error: ${e.message}")
            }
        }
    }

    suspend fun ensureInitialAddresses() {
        // Seed default initial address if none exist
        val initial = listOf(
            AddressEntity(
                id = "addr_home_1",
                label = "Home",
                flatNo = "Flat 402, Block B, Prestige Acropolis",
                street = "17th Main Rd, Koramangala 5th Block",
                landmark = "Near Sony World Signal",
                city = "Bengaluru",
                pincode = "560095",
                contactPhone = "+91 98765 43210",
                instructions = "Ring bell twice, 4th floor lift available.",
                isDefault = true
            ),
            AddressEntity(
                id = "addr_work_1",
                label = "Work",
                flatNo = "Office 304, Cyber Heights",
                street = "Outer Ring Road, Bellandur",
                landmark = "Opposite EcoSpace",
                city = "Bengaluru",
                pincode = "560103",
                contactPhone = "+91 98765 43210",
                instructions = "Visitor parking at basement level 2.",
                isDefault = false
            )
        )
        addressDao.insertAddresses(initial)
    }

    suspend fun addAddress(address: Address): Address {
        val newId = if (address.id.isBlank()) "addr_" + UUID.randomUUID().toString().take(8) else address.id
        val finalAddress = address.copy(id = newId)
        if (finalAddress.isDefault) {
            addressDao.clearDefaultFlags()
        }
        addressDao.insertAddress(AddressEntity.fromDomain(finalAddress))

        // Synchronize to Firestore for authenticated customer
        val customerId = sessionManager?.currentUser?.value?.id ?: "cust_001"
        firebaseBackend?.saveAddressInBackend(customerId, finalAddress)

        return finalAddress
    }

    suspend fun updateAddress(address: Address) {
        if (address.isDefault) {
            addressDao.clearDefaultFlags()
        }
        addressDao.insertAddress(AddressEntity.fromDomain(address))

        // Synchronize to Firestore for authenticated customer
        val customerId = sessionManager?.currentUser?.value?.id ?: "cust_001"
        firebaseBackend?.saveAddressInBackend(customerId, address)
    }

    suspend fun setDefault(id: String) {
        addressDao.clearDefaultFlags()
        addressDao.setDefaultAddress(id)
    }

    suspend fun deleteAddress(id: String) {
        addressDao.deleteAddressById(id)

        // Delete from Firestore for authenticated customer
        val customerId = sessionManager?.currentUser?.value?.id ?: "cust_001"
        firebaseBackend?.deleteAddressInBackend(customerId, id)
    }
}
