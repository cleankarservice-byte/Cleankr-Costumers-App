package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.session.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val phoneNumber: String = "",
    val otp: String = "",
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = ""
)

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updatePhone(phone: String) {
        if (phone.length <= 10) {
            _uiState.value = _uiState.value.copy(phoneNumber = phone, errorMessage = null)
        }
    }

    fun updateOtp(otp: String) {
        if (otp.length <= 6) {
            _uiState.value = _uiState.value.copy(otp = otp, errorMessage = null)
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
    }

    fun sendOtp() {
        val phone = _uiState.value.phoneNumber
        if (phone.length != 10) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid 10-digit mobile number")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(1000)
            _uiState.value = _uiState.value.copy(isLoading = false, isOtpSent = true, otp = "123456")
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        val otp = _uiState.value.otp
        if (otp.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter the OTP")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            delay(800)
            val phone = _uiState.value.phoneNumber
            val uid = "user_${phone.takeLast(6)}"
            val name = _uiState.value.userName.ifBlank { "Customer (${phone.takeLast(4)})" }
            sessionManager.saveLoginSession(uid = uid, phone = "+91 $phone", name = name)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        }
    }

    fun handleGoogleAuthSuccess(uid: String, name: String, email: String, onSuccess: () -> Unit) {
        sessionManager.saveLoginSession(uid = uid, phone = "+91 98765 43210", name = name, email = email)
        onSuccess()
    }
}
