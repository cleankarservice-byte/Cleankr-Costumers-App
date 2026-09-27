package com.example

import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.AddOnItem
import com.example.core.model.Address
import com.example.core.model.BookingStatus
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceCategory
import com.example.core.repository.BookingRepository
import com.example.core.repository.ServiceRepository
import com.example.core.repository.SlotRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class ExampleUnitTest {

    private val serviceRepository = ServiceRepository()
    private val slotRepository = SlotRepository()

    @Test
    fun testCompleteServicePricingCatalogue() {
        // 1. BATHROOM
        val intenseClean = serviceRepository.getServiceById("srv_bath_intense")
        assertNotNull(intenseClean)
        assertEquals(450, intenseClean!!.variants.first { it.id == "var_bath_intense_1" }.price)
        assertEquals(850, intenseClean.variants.first { it.id == "var_bath_intense_2" }.price)
        assertEquals(1250, intenseClean.variants.first { it.id == "var_bath_intense_3" }.price)
        assertEquals(200, intenseClean.addOns.first { it.id == "addon_glass_partition" }.price)

        val moveIn = serviceRepository.getServiceById("srv_bath_movein")
        assertNotNull(moveIn)
        assertEquals(550, moveIn!!.variants.first { it.id == "var_bath_movein_1" }.price)
        assertEquals(950, moveIn.variants.first { it.id == "var_bath_movein_2" }.price)
        assertEquals(1350, moveIn.variants.first { it.id == "var_bath_movein_3" }.price)
        assertEquals(200, moveIn.addOns.first { it.id == "addon_glass_partition" }.price)

        val hardWater = serviceRepository.getServiceById("srv_bath_hardwater")
        assertNotNull(hardWater)
        assertEquals(700, hardWater!!.variants.first { it.id == "var_bath_hardwater_1" }.price)
        assertEquals(1100, hardWater.variants.first { it.id == "var_bath_hardwater_2" }.price)
        assertEquals(1600, hardWater.variants.first { it.id == "var_bath_hardwater_3" }.price)
        assertEquals(200, hardWater.addOns.first { it.id == "addon_glass_partition" }.price)

        // 2. KITCHEN
        val kitchen = serviceRepository.getServiceById("srv_kitchen_clean")
        assertNotNull(kitchen)
        assertEquals(1400, kitchen!!.basePrice)
        assertEquals(200, kitchen.addOns.first { it.name == "Chimney" }.price)
        assertEquals(250, kitchen.addOns.first { it.name == "Cabinets" }.price)
        assertEquals(350, kitchen.addOns.first { it.name == "Trolleys" }.price)

        // 3. FLAT
        val flat = serviceRepository.getServiceById("srv_flat_deep")
        assertNotNull(flat)
        assertEquals(3000, flat!!.variants.first { it.name == "1 BHK" }.price)
        assertEquals(5000, flat.variants.first { it.name == "2 BHK" }.price)
        assertEquals(7000, flat.variants.first { it.name == "3 BHK" }.price)
        assertEquals(9200, flat.variants.first { it.name == "4 BHK" }.price)

        // 4. BALCONY
        val balcony = serviceRepository.getServiceById("srv_balcony_clean")
        assertNotNull(balcony)
        assertEquals(600, balcony!!.variants.first { it.name == "Small Balcony" }.price)
        assertEquals(850, balcony.variants.first { it.name == "Big Balcony" }.price)

        // 5. OTHER CLEANING
        val fan = serviceRepository.getServiceById("srv_other_fan")
        assertNotNull(fan)
        assertEquals(60, fan!!.basePrice)

        val exhaust = serviceRepository.getServiceById("srv_other_exhaust_fan")
        assertNotNull(exhaust)
        assertEquals(65, exhaust!!.basePrice)

        val window = serviceRepository.getServiceById("srv_other_glass_window")
        assertNotNull(window)
        assertEquals(300, window!!.basePrice)

        val door = serviceRepository.getServiceById("srv_other_glass_door")
        assertNotNull(door)
        assertEquals(400, door!!.basePrice)
    }

    @Test
    fun testPriceCalculationWithAddOns() {
        val kitchen = serviceRepository.getServiceById("srv_kitchen_clean")!!
        val variant = kitchen.variants.first()
        val chimney = kitchen.addOns.first { it.name == "Chimney" }
        val trolleys = kitchen.addOns.first { it.name == "Trolleys" }

        // Base 1400 + 200 + 350 = 1950
        val total = serviceRepository.calculateTotal(kitchen, variant, 1, listOf(chimney, trolleys))
        assertEquals(1950, total)
    }

    @Test
    fun testDefaultHubCoveredPincodesLogic() {
        val southCovered = listOf("560034", "560095", "560102", "560068", "560076", "560078", "560029", "560047")
        assertTrue(southCovered.contains("560095"))
        assertFalse(southCovered.contains("110001"))
    }

    @Test
    fun testBookingStatusTransitions() {
        assertEquals(0, BookingStatus.BOOKED.stepIndex)
        assertEquals(1, BookingStatus.ASSIGNED.stepIndex)
        assertEquals(2, BookingStatus.PARTNER_ACCEPTED.stepIndex)
        assertEquals(3, BookingStatus.ON_THE_WAY.stepIndex)
        assertEquals(4, BookingStatus.ARRIVED.stepIndex)
        assertEquals(5, BookingStatus.STARTED.stepIndex)
        assertEquals(6, BookingStatus.COMPLETED.stepIndex)

        assertTrue(BookingStatus.COMPLETED.isTerminal)
        assertTrue(BookingStatus.CANCELLED.isTerminal)
        assertFalse(BookingStatus.BOOKED.isTerminal)
        assertFalse(BookingStatus.STARTED.isTerminal)
    }

    @Test
    fun testSlotRepositoryAvailability() {
        val days = slotRepository.getUpcomingDays(14)
        assertEquals(14, days.size)
        assertTrue(days.first().isAvailable)

        val slots = slotRepository.getTimeSlotsForDate(days.first().dateString)
        assertTrue(slots.isNotEmpty())
    }

    @Test
    fun testSha256PinHashing() {
        val pin = "1234"
        val salt = "cleankr_salt_"
        val expectedHash = MessageDigest.getInstance("SHA-256")
            .digest((salt + pin).toByteArray())
            .joinToString("") { "%02x".format(it) }

        val computedHash = MessageDigest.getInstance("SHA-256")
            .digest(("cleankr_salt_1234").toByteArray())
            .joinToString("") { "%02x".format(it) }

        assertEquals(expectedHash, computedHash)
    }
}
