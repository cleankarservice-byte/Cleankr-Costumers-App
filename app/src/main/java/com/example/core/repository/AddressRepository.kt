package com.example.core.repository

import com.example.core.data.local.AddressDao
import com.example.core.data.local.AddressEntity
import com.example.core.model.Address
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AddressRepository(private val addressDao: AddressDao) {

    fun observeAddresses(): Flow<List<Address>> {
        return addressDao.getAllAddresses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveAddress(address: Address): Address {
        val addressId = if (address.id.isBlank()) UUID.randomUUID().toString() else address.id
        val finalAddress = address.copy(id = addressId)
        if (finalAddress.isDefault) {
            addressDao.clearDefaultFlags()
        }
        addressDao.insertAddress(AddressEntity.fromDomain(finalAddress))
        return finalAddress
    }

    suspend fun deleteAddress(id: String) {
        addressDao.deleteAddressById(id)
    }

    suspend fun setDefaultAddress(id: String) {
        addressDao.clearDefaultFlags()
        addressDao.setDefaultAddress(id)
    }
}
