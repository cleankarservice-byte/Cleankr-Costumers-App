package com.example.core.repository

import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepository(private val backendService: FirebaseBackendService) {

    val categories: List<ServiceCategory> = listOf(
        ServiceCategory("cat_all", "All Services", "category", "Browse complete range of hygiene cleans", 7),
        ServiceCategory("cat_bathroom", "Bathroom", "shower", "Intense, Move-in & Hard Water scale removal", 3),
        ServiceCategory("cat_kitchen", "Kitchen", "kitchen", "Counter, sink, chimney & cabinet deep cleaning", 1),
        ServiceCategory("cat_fullhome", "Full Home", "home", "1 BHK to 4 BHK flat deep cleaning", 1),
        ServiceCategory("cat_balcony", "Balcony", "balcony", "Small & big balcony cleaning", 1),
        ServiceCategory("cat_other", "Other Cleaning", "cleaning", "Fans, windows & doors cleaning", 1)
    )

    fun observeAllServices(): Flow<List<ServiceItem>> {
        return backendService.observeServicesCatalog()
    }

    fun observeServicesByCategory(categoryId: String): Flow<List<ServiceItem>> {
        return backendService.observeServicesCatalog().map { list ->
            if (categoryId == "cat_all" || categoryId.isBlank()) {
                list
            } else {
                list.filter { it.categoryId == categoryId }
            }
        }
    }

    suspend fun getServiceById(serviceId: String): ServiceItem? {
        return backendService.defaultServicesCatalog.firstOrNull { it.id == serviceId }
    }
}
