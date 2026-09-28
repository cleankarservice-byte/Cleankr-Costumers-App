package com.example.core.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.core.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cleankr_customer_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "en") ?: "en")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private fun loadProfile(): UserProfile {
        val phone = prefs.getString("user_phone", "") ?: ""
        val last4 = if (phone.length >= 4) phone.takeLast(4) else "5600"
        return UserProfile(
            uid = prefs.getString("user_uid", "cust_default") ?: "cust_default",
            name = prefs.getString("user_name", "Valued Customer") ?: "Valued Customer",
            phone = phone,
            email = prefs.getString("user_email", "") ?: "",
            referralCode = prefs.getString("referral_code", "CLEAN-$last4") ?: "CLEAN-$last4",
            referralEarnings = prefs.getInt("referral_earnings", 200),
            language = prefs.getString("app_language", "en") ?: "en"
        )
    }

    fun saveLoginSession(uid: String, phone: String, name: String, email: String = "") {
        val last4 = if (phone.length >= 4) phone.takeLast(4) else "5600"
        val referralCode = "CLEAN-$last4"
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_uid", uid)
            .putString("user_phone", phone)
            .putString("user_name", name.ifBlank { "Cleankr User" })
            .putString("user_email", email)
            .putString("referral_code", referralCode)
            .apply()

        _isLoggedIn.value = true
        _userProfile.value = UserProfile(
            uid = uid,
            name = name.ifBlank { "Cleankr User" },
            phone = phone,
            email = email,
            referralCode = referralCode,
            referralEarnings = prefs.getInt("referral_earnings", 200),
            language = _appLanguage.value
        )
    }

    fun updateProfile(name: String, email: String) {
        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()
        _userProfile.value = _userProfile.value.copy(name = name, email = email)
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
        _userProfile.value = _userProfile.value.copy(language = lang)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _userProfile.value = loadProfile()
    }
}
