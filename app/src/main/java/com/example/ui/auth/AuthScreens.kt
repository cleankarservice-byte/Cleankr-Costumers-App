package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CleankrButton
import com.example.ui.components.CleankrLogoBadge
import com.example.ui.components.ErrorBanner
import com.example.ui.theme.CleankrBackground
import com.example.ui.theme.CleankrBorder
import com.example.ui.theme.CleankrMuted
import com.example.ui.theme.CleankrNavyDark
import com.example.ui.theme.CleankrOrange
import com.example.ui.theme.CleankrSlate
import com.example.ui.theme.CleankrTeal
import com.example.ui.theme.CleankrTealContainer
import com.example.ui.theme.CleankrTealDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToPinLogin: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        delay(1400)
        if (isLoggedIn) {
            if (currentUser?.hasPinSet == true) {
                onNavigateToPinLogin()
            } else {
                onNavigateToMain()
            }
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrNavyDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cleankr_logo),
                contentDescription = "Cleankr Logo",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CLEAN",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "KR",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CleankrOrange,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sparkling Homes • Verified Professionals",
                style = MaterialTheme.typography.bodyMedium,
                color = CleankrMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = CleankrTeal,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
        }
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onOtpSent: (phone: String) -> Unit,
    onNavigateToPinLogin: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(40.dp))
            CleankrLogoBadge(size = 56, showText = true)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to Cleankr",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your mobile number to receive a one-time verification password",
                style = MaterialTheme.typography.bodyMedium,
                color = CleankrMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                ErrorBanner(errorMessage = errorMessage, onRetry = { authViewModel.clearError() })
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Mobile Number",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CleankrSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                phoneNumber = it
                                authViewModel.clearError()
                            }
                        },
                        placeholder = { Text("Enter 10-digit number") },
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            ) {
                                Text(
                                    text = "🇮🇳 +91",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavyDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(20.dp)
                                        .background(CleankrBorder)
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = CleankrTeal
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (phoneNumber.length == 10) {
                                    authViewModel.sendOtp(phoneNumber) { onOtpSent(phoneNumber) }
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleankrTeal,
                            unfocusedBorderColor = CleankrBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CleankrButton(
                        text = "Send OTP",
                        onClick = {
                            authViewModel.sendOtp(phoneNumber) { onOtpSent(phoneNumber) }
                        },
                        enabled = phoneNumber.length == 10,
                        isLoading = isLoading,
                        leadingIcon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                }
            }

            if (currentUser?.hasPinSet == true) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onNavigateToPinLogin,
                    modifier = Modifier.testTag("login_with_pin_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CleankrTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Login with 4-Digit Quick PIN",
                        style = MaterialTheme.typography.labelLarge,
                        color = CleankrTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Trust badge & Security note
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = CleankrTealDark,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Protected by 256-bit Cleankr Secure Gateway",
                style = MaterialTheme.typography.labelSmall,
                color = CleankrMuted
            )
        }
    }
}

@Composable
fun OtpVerificationScreen(
    phone: String,
    authViewModel: AuthViewModel,
    onSuccess: (needsPinSetup: Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()
    val resendCountdown by authViewModel.resendTimer.collectAsState()

    val isLoading = uiState is AuthUiState.Loading
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(30.dp))
            CleankrLogoBadge(size = 48, showText = true)
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Verify Mobile Number",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We sent an OTP code to +91 $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = CleankrMuted,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Edit Number",
                style = MaterialTheme.typography.labelMedium,
                color = CleankrTeal,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onBackClick)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                ErrorBanner(errorMessage = errorMessage, onRetry = { authViewModel.clearError() })
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter 6-Digit OTP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CleankrSlate
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                otpCode = it
                                authViewModel.clearError()
                                if (it.length == 6) {
                                    authViewModel.verifyOtp(phone, it, onSuccess)
                                }
                            }
                        },
                        placeholder = { Text("• • • • • •", textAlign = TextAlign.Center) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (otpCode.length == 6 || otpCode.length == 4) {
                                    authViewModel.verifyOtp(phone, otpCode, onSuccess)
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            letterSpacing = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleankrTeal,
                            unfocusedBorderColor = CleankrBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (resendCountdown > 0) "Resend OTP in ${resendCountdown}s" else "Didn't receive code?",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrMuted
                        )

                        TextButton(
                            onClick = { authViewModel.resendOtp(phone) },
                            enabled = resendCountdown == 0 && !isLoading,
                            modifier = Modifier.testTag("resend_otp_btn")
                        ) {
                            Text(
                                text = "Resend OTP",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (resendCountdown == 0) CleankrOrange else CleankrMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CleankrButton(
                        text = "Verify & Proceed",
                        onClick = {
                            authViewModel.verifyOtp(phone, otpCode, onSuccess)
                        },
                        enabled = otpCode.length >= 4,
                        isLoading = isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = CleankrTealContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CleankrTealDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo Mode Hint: Enter any 4 or 6-digit code (e.g. 123456) to verify.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleankrTealDark
                    )
                }
            }
        }

        Text(
            text = "Never share your Cleankr OTP with anyone, including partners.",
            style = MaterialTheme.typography.labelSmall,
            color = CleankrMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun PinSetupScreen(
    authViewModel: AuthViewModel,
    onPinSetSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val uiState by authViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(40.dp))
            CleankrLogoBadge(size = 48, showText = true)
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Set 4-Digit Quick PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enable faster, instant logins without waiting for SMS OTPs every time.",
                style = MaterialTheme.typography.bodyMedium,
                color = CleankrMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (localError != null) {
                ErrorBanner(errorMessage = localError!!, onRetry = { localError = null })
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "New 4-Digit PIN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CleankrSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                pin = it
                                localError = null
                            }
                        },
                        placeholder = { Text("• • • •") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_setup_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleankrTeal,
                            unfocusedBorderColor = CleankrBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Confirm 4-Digit PIN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CleankrSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                confirmPin = it
                                localError = null
                            }
                        },
                        placeholder = { Text("• • • •") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_confirm_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleankrTeal,
                            unfocusedBorderColor = CleankrBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CleankrButton(
                        text = "Set PIN & Continue",
                        onClick = {
                            if (pin.length != 4) {
                                localError = "PIN must be 4 digits."
                            } else if (pin != confirmPin) {
                                localError = "PINs do not match."
                            } else {
                                authViewModel.setQuickPin(pin, onPinSetSuccess)
                            }
                        },
                        enabled = pin.length == 4 && confirmPin.length == 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onSkip,
                modifier = Modifier.testTag("pin_skip_btn")
            ) {
                Text(
                    text = "Skip for Now",
                    style = MaterialTheme.typography.labelLarge,
                    color = CleankrMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PinLoginScreen(
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onFallbackToOtp: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val errorMessage = (uiState as? AuthUiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(50.dp))
            CleankrLogoBadge(size = 56, showText = true)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome Back, ${currentUser?.name ?: "Customer"}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your 4-digit PIN to access your Cleankr account",
                style = MaterialTheme.typography.bodyMedium,
                color = CleankrMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (errorMessage != null) {
                ErrorBanner(errorMessage = errorMessage, onRetry = { authViewModel.clearError() })
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quick PIN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CleankrSlate
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                enteredPin = it
                                authViewModel.clearError()
                                if (it.length == 4) {
                                    authViewModel.verifyQuickPin(it, onSuccess)
                                }
                            }
                        },
                        placeholder = { Text("• • • •", textAlign = TextAlign.Center) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_login_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            letterSpacing = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleankrTeal,
                            unfocusedBorderColor = CleankrBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CleankrButton(
                        text = "Unlock",
                        onClick = { authViewModel.verifyQuickPin(enteredPin, onSuccess) },
                        enabled = enteredPin.length == 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onFallbackToOtp,
                modifier = Modifier.testTag("forgot_pin_btn")
            ) {
                Text(
                    text = "Forgot PIN? Login using Mobile OTP",
                    style = MaterialTheme.typography.labelLarge,
                    color = CleankrTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
