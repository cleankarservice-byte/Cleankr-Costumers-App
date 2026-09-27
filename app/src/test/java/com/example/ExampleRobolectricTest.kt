package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.local.AppDatabase
import com.example.core.model.Address
import com.example.core.model.PaymentMethod
import com.example.core.repository.BookingRepository
import com.example.core.repository.ServiceRepository
import com.example.core.repository.SlotRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Cleankr", appName)
    }

    @Test
    fun `verify active Hub detection and unavailable area blocking`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firebaseBackend = FirebaseBackendService(context)

        // 1. Covered pincode
        val coveredHub = firebaseBackend.findActiveHubForPincode("560095")
        assertNotNull(coveredHub)
        assertEquals("hub_blr_south", coveredHub!!.id)
        assertEquals("Koramangala & South Hub", coveredHub.name)

        // 2. Uncovered pincode
        val uncoveredHub = firebaseBackend.findActiveHubForPincode("110001")
        assertNull(uncoveredHub)
    }

    @Test
    fun `verify backend price validation and snapshot calculation`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firebaseBackend = FirebaseBackendService(context)

        // Validate Bathroom Intense Clean 2 Bathrooms (850) + Glass Partition (200)
        val result = firebaseBackend.validateAndCalculatePrice(
            serviceId = "srv_bath_intense",
            variantName = "2 Bathrooms",
            quantity = 1,
            selectedAddOnNames = listOf("Glass Partition Add-on")
        )

        assertEquals("Bathroom Intense Clean", result.serviceTitle)
        assertEquals(850, result.basePrice)
        assertEquals(200, result.addOnPrice)
        assertEquals(1050, result.totalAmount)
    }

    @Test
    fun `verify booking creation enforces active Hub and stores hubId and hubName`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = AppDatabase.getDatabase(context)
        val firebaseBackend = FirebaseBackendService(context)
        val serviceRepo = ServiceRepository(firebaseBackend)
        val slotRepo = SlotRepository()
        val bookingRepo = BookingRepository(
            bookingDao = database.bookingDao(),
            notificationDao = database.notificationDao(),
            slotRepository = slotRepo,
            serviceRepository = serviceRepo,
            firebaseBackend = firebaseBackend
        )

        val service = serviceRepo.getServiceById("srv_bath_intense")!!
        val variant = service.variants.first { it.id == "var_bath_intense_1" } // 1 Bathroom: 450

        // Case A: Uncovered area -> Must prevent booking
        val uncoveredAddress = Address(
            id = "addr_delhi",
            label = "Work",
            flatNo = "Flat 101",
            street = "Connaught Place",
            landmark = "Central",
            city = "New Delhi",
            pincode = "110001"
        )
        val failedResult = bookingRepo.createBooking(
            service = service,
            variant = variant,
            quantity = 1,
            selectedAddOnNames = emptyList(),
            dateString = "2026-10-01",
            slotTime = "10:00 AM - 12:00 PM",
            address = uncoveredAddress,
            instructions = "None",
            paymentMethod = PaymentMethod.ONLINE
        )
        assertTrue(failedResult.isFailure)
        assertEquals("Cleankr service is currently unavailable in your area.", failedResult.exceptionOrNull()?.message)

        // Case B: Covered area -> Must succeed and store hubId/hubName
        val coveredAddress = Address(
            id = "addr_blr",
            label = "Home",
            flatNo = "Flat 402, Block B",
            street = "17th Main, Koramangala",
            landmark = "Near Sony World",
            city = "Bengaluru",
            pincode = "560095"
        )
        val successResult = bookingRepo.createBooking(
            service = service,
            variant = variant,
            quantity = 1,
            selectedAddOnNames = listOf("Glass Partition Add-on"),
            dateString = "2026-10-01",
            slotTime = "10:00 AM - 12:00 PM",
            address = coveredAddress,
            instructions = "Doorbell working",
            paymentMethod = PaymentMethod.ONLINE
        )
        assertTrue(successResult.isSuccess)
        val booking = successResult.getOrNull()!!
        assertEquals("hub_blr_south", booking.hubId)
        assertEquals("Koramangala & South Hub", booking.hubName)
        assertEquals(450, booking.basePrice)
        assertEquals(200, booking.addOnPrice)
        assertEquals(650, booking.totalAmount)
    }
}
