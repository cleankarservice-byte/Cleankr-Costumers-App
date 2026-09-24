package com.example.ui.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.session.SessionManager
import com.example.ui.auth.AuthViewModel
import com.example.ui.components.CleankrButton
import com.example.ui.components.CleankrStandardTopBar
import com.example.ui.theme.CleankrBackground
import com.example.ui.theme.CleankrBorder
import com.example.ui.theme.CleankrGreen
import com.example.ui.theme.CleankrGreenLight
import com.example.ui.theme.CleankrMuted
import com.example.ui.theme.CleankrNavyDark
import com.example.ui.theme.CleankrOrange
import com.example.ui.theme.CleankrRed
import com.example.ui.theme.CleankrRedLight
import com.example.ui.theme.CleankrSlate
import com.example.ui.theme.CleankrTeal
import com.example.ui.theme.CleankrTealContainer
import com.example.ui.theme.CleankrTealDark
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    onNavigateToAddresses: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAccountDeletion: () -> Unit,
    onNavigateToPolicies: (String) -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by sessionManager.currentUser.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(title = "Account & Settings")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Customer Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CleankrTealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CleankrTealDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Customer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark
                        )
                        Text(
                            text = currentUser?.phone ?: "+91 98765 43210",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate
                        )
                        Text(
                            text = currentUser?.email ?: "customer@cleankr.com",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleankrMuted
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = CleankrTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security & Settings Section
            Text(
                text = "Security & Convenience",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrSlate
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Lock,
                        title = if (currentUser?.hasPinSet == true) "Update 4-Digit Quick PIN" else "Set 4-Digit Quick PIN",
                        subtitle = if (currentUser?.hasPinSet == true) "Active • Instant biometric/PIN access" else "Setup faster logins without SMS OTP",
                        onClick = { showSetPinDialog = true }
                    )
                    HorizontalDivider(color = CleankrBorder.copy(alpha = 0.5f))
                    ProfileMenuRow(
                        icon = Icons.Default.LocationOn,
                        title = "Saved Addresses",
                        subtitle = "Manage Home, Work, and Service locations",
                        onClick = onNavigateToAddresses
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Help & Policies Section
            Text(
                text = "Support & Legal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrSlate
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Headphones,
                        title = "Help & Customer Support",
                        subtitle = "FAQs, tickets, order assistance, and live hotline",
                        onClick = onNavigateToSupport
                    )
                    HorizontalDivider(color = CleankrBorder.copy(alpha = 0.5f))
                    ProfileMenuRow(
                        icon = Icons.Default.Policy,
                        title = "Cancellation & Refund Policy",
                        subtitle = "Transparent 100% free cancellation rules",
                        onClick = { onNavigateToPolicies("cancellation") }
                    )
                    HorizontalDivider(color = CleankrBorder.copy(alpha = 0.5f))
                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy & Terms",
                        subtitle = "Data security & partner safety guidelines",
                        onClick = { onNavigateToPolicies("privacy") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Logout,
                        iconColor = CleankrNavyDark,
                        title = "Logout",
                        subtitle = "Sign out from this device",
                        onClick = { showLogoutDialog = true }
                    )
                    HorizontalDivider(color = CleankrBorder.copy(alpha = 0.5f))
                    ProfileMenuRow(
                        icon = Icons.Default.DeleteForever,
                        iconColor = CleankrRed,
                        title = "Delete Account",
                        subtitle = "Permanently remove your account and personal data",
                        onClick = onNavigateToAccountDeletion
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Cleankr Customer App v1.0.0 (Build 36)",
                style = MaterialTheme.typography.labelSmall,
                color = CleankrMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(currentUser?.name ?: "") }
        var email by remember { mutableStateOf(currentUser?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = CleankrNavyDark) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sessionManager.updateProfile(name, email)
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = CleankrTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = CleankrMuted)
                }
            }
        )
    }

    // Set PIN Dialog
    if (showSetPinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = { Text("Configure 4-Digit PIN", fontWeight = FontWeight.Bold, color = CleankrNavyDark) },
            text = {
                Column {
                    if (error != null) {
                        Text(text = error!!, color = CleankrRed, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                        label = { Text("New PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPin = it },
                        label = { Text("Confirm PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPin.length != 4) {
                            error = "PIN must be 4 digits"
                        } else if (newPin != confirmPin) {
                            error = "PINs do not match"
                        } else {
                            sessionManager.setQuickPin(newPin)
                            showSetPinDialog = false
                        }
                    }
                ) {
                    Text("Set PIN", fontWeight = FontWeight.Bold, color = CleankrTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false }) {
                    Text("Cancel", color = CleankrMuted)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?", fontWeight = FontWeight.Bold, color = CleankrNavyDark) },
            text = { Text("Are you sure you want to log out of Cleankr?", color = CleankrSlate) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CleankrRed)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = CleankrSlate)
                }
            }
        )
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    iconColor: Color = CleankrTeal,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = CleankrNavyDark)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = CleankrMuted, fontSize = 11.sp)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = CleankrMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun AccountDeletionScreen(
    authViewModel: AuthViewModel,
    onDeleted: () -> Unit,
    onBackClick: () -> Unit
) {
    val reasons = listOf(
        "I am moving to another city",
        "Services are too expensive",
        "Found alternate local cleaners",
        "Privacy concerns",
        "Other reasons"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var confirmText by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(title = "Delete Account", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Surface(
                color = CleankrRedLight,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = CleankrRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Irreversible Action", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrRed)
                        Text(
                            text = "Deleting your account will erase your booking history, saved addresses, active credits, and profile data from Cleankr servers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Please tell us why you are leaving:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
            Spacer(modifier = Modifier.height(8.dp))

            reasons.forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReason = r }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedReason == r,
                        onClick = { selectedReason = r },
                        colors = RadioButtonDefaults.colors(selectedColor = CleankrRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = r, style = MaterialTheme.typography.bodyMedium, color = CleankrSlate)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Type DELETE to confirm account deletion:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmText,
                onValueChange = { confirmText = it },
                placeholder = { Text("DELETE") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_account_confirm_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CleankrButton(
                text = "Permanently Delete My Account",
                onClick = {
                    isDeleting = true
                    authViewModel.deleteAccount(selectedReason, onDeleted)
                },
                enabled = confirmText.trim().uppercase() == "DELETE",
                isLoading = isDeleting
            )
        }
    }
}

@Composable
fun PoliciesScreen(
    policyType: String,
    onBackClick: () -> Unit
) {
    val isCancellation = policyType == "cancellation"
    val title = if (isCancellation) "Cancellation & Refund Policy" else "Privacy Policy & Terms"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(title = title, onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (isCancellation) {
                        Text(text = "1. Free Cancellation Window", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You can cancel any booking free of charge up to 2 hours prior to the scheduled service time slot. 100% of the paid amount will be refunded directly to your payment source.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "2. Late Cancellation Fee", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "If a cancellation is requested within 2 hours of the scheduled time or after a partner has been dispatched, a nominal partner convenience fee of ₹100 is deducted to compensate our travel professional.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "3. Refund Processing Timeline", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "UPI refunds are processed instantly. Credit/Debit card refunds typically reflect in your account within 3 to 5 banking days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate,
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(text = "1. Data Collection & Usage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cleankr collects your mobile number and service address solely to fulfill cleaning appointments. We never sell your personal information or spam you with unauthorized third-party marketing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "2. Masked Calling & Safety", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "For your privacy, calls between customers and service partners are routed via virtual proxy numbers. Your real personal phone number is never disclosed to service partners.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "3. Background Verified Staff", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every Cleankr professional undergoes Aadhaar/KYC identity checks, police background verification, and formal hygiene training.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
