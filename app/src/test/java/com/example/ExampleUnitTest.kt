package com.example

import com.example.core.model.AddOnItem
import com.example.core.model.BookingStatus
import com.example.core.model.ServiceCategory
import com.example.core.repository.ServiceRepository
import com.example.core.repository.SlotRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class ExampleUnitTest {

    private val serviceRepository = ServiceRepository()
    private val slotRepository = SlotRepository()

    @Test
    fun testBathroomPricingLogic() {
        val intenseClean = serviceRepository.getServiceById("srv_bath_intense")
        assertNotNull(intenseClean)
        assertEquals("Bathroom Intense Clean", intenseClean!!.title)
        assertEquals(ServiceCategory.BATHROOM, intenseClean.category)

        // 1 Bath: 450, 2 Bath: 850, 3 Bath: 1250
        val v1 = intenseClean.variants.first { it.id == "var_bath_intense_1" }
        val v2 = intenseClean.variants.first { it.id == "var_bath_intense_2" }
        val v3 = intenseClean.variants.first { it.id == "var_bath_intense_3" }
        assertEquals(450, v1.price)
        assertEquals(850, v2.price)
        assertEquals(1250, v3.price)

        // Add-on: Glass Partition (+200)
        val glassAddon = intenseClean.addOns.first { it.id == "addon_glass_partition" }
        assertEquals(200, glassAddon.price)

        // Total calculation
        val totalWithAddon = serviceRepository.calculateTotal(intenseClean, v1, 1, listOf(glassAddon))
        assertEquals(650, totalWithAddon)
    }

    @Test
    fun testMoveInCleanAndHardWaterPricing() {
        val moveIn = serviceRepository.getServiceById("srv_bath_movein")
        assertNotNull(moveIn)
        assertEquals(550, moveIn!!.variants.first { it.id == "var_bath_movein_1" }.price)
        assertEquals(950, moveIn.variants.first { it.id == "var_bath_movein_2" }.price)
        assertEquals(1350, moveIn.variants.first { it.id == "var_bath_movein_3" }.price)

        val hardWater = serviceRepository.getServiceById("srv_bath_hardwater")
        assertNotNull(hardWater)
        assertEquals(700, hardWater!!.variants.first { it.id == "var_bath_hardwater_1" }.price)
        assertEquals(1100, hardWater.variants.first { it.id == "var_bath_hardwater_2" }.price)
        assertEquals(1600, hardWater.variants.first { it.id == "var_bath_hardwater_3" }.price)
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
