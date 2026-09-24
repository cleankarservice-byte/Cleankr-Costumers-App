package com.example.core.repository

import com.example.core.data.local.AddressDao
import com.example.core.data.local.AddressEntity
import com.example.core.model.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AddressRepository(private val addressDao: AddressDao) {

    val addresses: Flow<List<Address>> = addressDao.getAllAddresses().map { list ->
        list.map { it.toDomain() }
    }

    val defaultAddress: Flow<Address?> = addressDao.getDefaultAddress().map { it?.toDomain() }

    suspend fun ensureInitialAddresses() {
        // Seed default sample address if none exist
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
        return finalAddress
    }

    suspend fun updateAddress(address: Address) {
        if (address.isDefault) {
            addressDao.clearDefaultFlags()
        }
        addressDao.insertAddress(AddressEntity.fromDomain(address))
    }

    suspend fun setDefault(id: String) {
        addressDao.clearDefaultFlags()
        addressDao.setDefaultAddress(id)
    }

    suspend fun deleteAddress(id: String) {
        addressDao.deleteAddressById(id)
    }
}
