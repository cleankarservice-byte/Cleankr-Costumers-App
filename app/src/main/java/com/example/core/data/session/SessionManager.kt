package com.example.core.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.CustomerUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cleankr_customer_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<CustomerUser?>(loadUserFromPrefs())
    val currentUser: StateFlow<CustomerUser?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private fun loadUserFromPrefs(): CustomerUser? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val id = prefs.getString(KEY_USER_ID, "cust_001") ?: "cust_001"
        val name = prefs.getString(KEY_USER_NAME, "Customer") ?: "Customer"
        val phone = prefs.getString(KEY_USER_PHONE, "+91 98765 43210") ?: "+91 98765 43210"
        val email = prefs.getString(KEY_USER_EMAIL, "customer@cleankr.com") ?: "customer@cleankr.com"
        val hasPin = prefs.getBoolean(KEY_HAS_PIN, false)
        val pinHash = prefs.getString(KEY_PIN_HASH, "") ?: ""

        return CustomerUser(
            id = id,
            name = name,
            phone = phone,
            email = email,
            isVerified = true,
            hasPinSet = hasPin,
            pinHash = pinHash
        )
    }

    fun saveLoginSession(phone: String, name: String = "Rajesh Sharma", email: String = "rajesh.cleankr@gmail.com") {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, "cust_" + phone.takeLast(6))
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_EMAIL, email)
            .apply()

        _currentUser.value = loadUserFromPrefs()
        _isLoggedIn.value = true
    }

    fun setQuickPin(pin: String): Boolean {
        if (pin.length != 4) return false
        val hash = hashPin(pin)
        prefs.edit()
            .putBoolean(KEY_HAS_PIN, true)
            .putString(KEY_PIN_HASH, hash)
            .apply()

        _currentUser.value = loadUserFromPrefs()
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, "") ?: return false
        return hashPin(pin) == storedHash
    }

    fun updateProfile(name: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()

        _currentUser.value = loadUserFromPrefs()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun deleteAccount() {
        prefs.edit().clear().apply()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("cleankr_salt_$pin".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_HAS_PIN = "has_pin"
        private const val KEY_PIN_HASH = "pin_hash"
    }
}
