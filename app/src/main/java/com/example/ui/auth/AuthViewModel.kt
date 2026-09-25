package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.data.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class OtpSent(val phone: String, val resendCountdown: Int) : AuthUiState()
    data object Authenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val firebaseBackend: FirebaseBackendService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _resendTimer = MutableStateFlow(30)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()

    private var timerJob: Job? = null

    val isLoggedIn = sessionManager.isLoggedIn
    val currentUser = sessionManager.currentUser

    fun sendOtp(phoneNumber: String, onSuccess: () -> Unit) {
        val cleanPhone = phoneNumber.trim().replace(" ", "").replace("-", "")
        if (cleanPhone.length < 10) {
            _uiState.value = AuthUiState.Error("Please enter a valid 10-digit mobile number.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            // Simulate secure Firebase Phone Auth handshake
            delay(800)
            startResendTimer()
            _uiState.value = AuthUiState.OtpSent(cleanPhone, 30)
            onSuccess()
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _resendTimer.value = 30
        timerJob = viewModelScope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    fun resendOtp(phone: String) {
        if (_resendTimer.value > 0) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(600)
            startResendTimer()
            _uiState.value = AuthUiState.OtpSent(phone, 30)
        }
    }

    fun verifyOtp(phone: String, enteredOtp: String, onSuccess: (isNewUser: Boolean) -> Unit) {
        if (enteredOtp.length != 6 && enteredOtp.length != 4) {
            _uiState.value = AuthUiState.Error("Please enter the complete verification code.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            delay(900)

            // For testing/mocking without live SMS gateway cost: any 4 or 6 digit code except all zeros
            if (enteredOtp == "0000" || enteredOtp == "000000") {
                _uiState.value = AuthUiState.Error("Invalid OTP entered. Please check the SMS and try again.")
                return@launch
            }

            // Save authenticated session
            val finalPhone = if (phone.startsWith("+91")) phone else "+91 $phone"
            sessionManager.saveLoginSession(phone = finalPhone)

            val user = sessionManager.currentUser.value
            val customerId = user?.id ?: "cust_${phone.takeLast(6)}"

            // Synchronize with shared Cleankr ecosystem (strictly Customer role)
            firebaseBackend?.syncCustomerProfile(
                customerId = customerId,
                name = user?.name ?: "Cleankr Customer",
                phone = finalPhone,
                email = user?.email ?: "customer@cleankr.com"
            )

            // Proactively sync FCM device push notification token
            firebaseBackend?.syncCurrentFcmToken(customerId)

            _uiState.value = AuthUiState.Authenticated
            val needsPinSetup = user?.hasPinSet != true
            onSuccess(needsPinSetup)
        }
    }

    fun setQuickPin(pin: String, onDone: () -> Unit) {
        if (pin.length != 4) {
            _uiState.value = AuthUiState.Error("PIN must be exactly 4 digits.")
            return
        }
        sessionManager.setQuickPin(pin)
        _uiState.value = AuthUiState.Authenticated
        onDone()
    }

    fun verifyQuickPin(pin: String, onSuccess: () -> Unit) {
        if (pin.length != 4) {
            _uiState.value = AuthUiState.Error("Please enter 4 digits.")
            return
        }
        val isValid = sessionManager.verifyPin(pin)
        if (isValid) {
            _uiState.value = AuthUiState.Authenticated
            onSuccess()
        } else {
            _uiState.value = AuthUiState.Error("Incorrect PIN. Please re-enter or login via OTP.")
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        val customerId = sessionManager.currentUser.value?.id
        viewModelScope.launch {
            if (!customerId.isNullOrBlank()) {
                firebaseBackend?.removeFcmToken(customerId)
            }
            sessionManager.logout()
            _uiState.value = AuthUiState.Idle
            onLoggedOut()
        }
    }

    fun deleteAccount(reason: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val customerId = sessionManager.currentUser.value?.id
            if (!customerId.isNullOrBlank()) {
                firebaseBackend?.removeFcmToken(customerId)
                firebaseBackend?.deleteCustomerAccountInBackend(customerId, reason)
            }
            delay(500)
            sessionManager.deleteAccount()
            _uiState.value = AuthUiState.Idle
            onDeleted()
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
