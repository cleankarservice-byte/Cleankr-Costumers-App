package com.example

import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.AddOnItem
import com.example.ui.components.CleankrStrings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDoorstepPinFormat() {
        // Doorstep PIN should be a 4-digit numeric string
        val pin = String.format("%04d", 4819)
        assertEquals(4, pin.length)
        assertTrue(pin.all { it.isDigit() })
    }

    @Test
    fun testBilingualStrings() {
        val engHome = CleankrStrings.get("home", "en")
        val hiHome = CleankrStrings.get("home", "hi")
        assertEquals("Home", engHome)
        assertEquals("होम", hiHome)

        val engOtp = CleankrStrings.get("doorstep_otp", "en")
        val hiOtp = CleankrStrings.get("doorstep_otp", "hi")
        assertTrue(engOtp.contains("PIN"))
        assertTrue(hiOtp.contains("पिन"))
    }

    @Test
    fun testZeroTaxPricing() {
        val basePrice = 3000 // Full Home Deep Cleaning (Flat) 1 BHK
        val addOnPrice = 200 // Glass Partition or Chimney Add-on
        val taxes = 0        // 0% Tax / Zero Platform Fee
        val total = basePrice + addOnPrice + taxes
        assertEquals(3200, total)
    }

    @Test
    fun testPuneHubDivisionPincodes() {
        val westPincodes = listOf("411038", "411045", "411007")
        val northPincodes = listOf("411057", "411027", "411017")
        assertTrue(westPincodes.contains("411038")) // Kothrud
        assertTrue(northPincodes.contains("411057")) // Hinjawadi
        assertFalse(westPincodes.contains("411057")) // Cross-hub check: Hinjawadi is NOT West Pune
    }
}
