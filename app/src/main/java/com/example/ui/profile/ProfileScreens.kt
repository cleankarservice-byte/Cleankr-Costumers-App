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
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.config.CleankrLegalConfig
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
    val context = LocalContext.current
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = CleankrTealContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Role: Customer",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleankrTealDark,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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

            // Support & Help Section
            Text(
                text = "Support & Help",
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
                        modifier = Modifier.testTag("help_support_item"),
                        onClick = onNavigateToSupport
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Legal Section
            Text(
                text = "Privacy & Legal",
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
                    // 1. Privacy Policy
                    ProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "Published privacy policy & personal data protection",
                        modifier = Modifier.testTag("privacy_policy_item"),
                        onClick = {
                            CleankrLegalConfig.openWebUrl(
                                context = context,
                                url = CleankrLegalConfig.PRIVACY_POLICY_URL
                            )
                        }
                    )
                    HorizontalDivider(color = CleankrBorder.copy(alpha = 0.5f))

                    // 2. Terms & Conditions
                    ProfileMenuRow(
                        icon = Icons.Default.Description,
                        title = "Terms & Conditions",
                        subtitle = "Service agreements, fixed pricing rules & user terms",
                        modifier = Modifier.testTag("terms_conditions_item"),
                        onClick = {
                            val url = CleankrLegalConfig.TERMS_CONDITIONS_URL
                            if (!url.isNullOrBlank()) {
                                CleankrLegalConfig.openWebUrl(context, url)
                            } else {
                                onNavigateToPolicies("terms")
                            }
                        }
                    )
                    HorizontalDivider(color = CleankrBorder.copy(alpha = 0.5f))

                    // 3. Refund & Cancellation Policy
                    ProfileMenuRow(
                        icon = Icons.Default.Policy,
                        title = "Refund & Cancellation Policy",
                        subtitle = "Transparent 100% free cancellation & refund guidelines",
                        modifier = Modifier.testTag("refund_cancellation_item"),
                        onClick = {
                            val url = CleankrLegalConfig.REFUND_CANCELLATION_URL
                            if (!url.isNullOrBlank()) {
                                CleankrLegalConfig.openWebUrl(context, url)
                            } else {
                                onNavigateToPolicies("cancellation")
                            }
                        }
                    )
                    HorizontalDivider(color = CleankrBorder.copy(alpha = 0.5f))

                    // 4. Account Deletion
                    ProfileMenuRow(
                        icon = Icons.Default.DeleteForever,
                        iconColor = CleankrRed,
                        title = "Account Deletion",
                        subtitle = "Permanently remove your account, data and history",
                        modifier = Modifier.testTag("account_deletion_item"),
                        onClick = onNavigateToAccountDeletion
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
                        modifier = Modifier.testTag("logout_item"),
                        onClick = { showLogoutDialog = true }
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
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
                            text = "Deleting your account permanently deactivates your credentials and erases saved addresses, profile data, and active credits. Completed payment invoices and statutory records are securely retained in compliance with applicable tax, accounting, and dispute retention regulations under Cleankr's published Privacy Policy.",
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
    val context = LocalContext.current
    val title = when (policyType) {
        "cancellation" -> "Refund & Cancellation Policy"
        "terms" -> "Terms & Conditions"
        else -> "Privacy Policy"
    }

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
            if (policyType == "privacy") {
                Surface(
                    color = CleankrTealContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CleankrTealDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Official Published Policy",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleankrTealDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cleankr respects your personal data. View our full published policy document online.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrNavyDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CleankrButton(
                            text = "Open Online Policy Document",
                            onClick = {
                                CleankrLegalConfig.openWebUrl(context, CleankrLegalConfig.PRIVACY_POLICY_URL)
                            },
                            modifier = Modifier.testTag("open_online_privacy_policy_btn")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    when (policyType) {
                        "cancellation" -> {
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
                        }
                        "terms" -> {
                            Text(text = "1. Transparent Fixed Pricing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All bathroom cleaning packages have fixed standardized rates (Intense: ₹450, Move-in: ₹550, Stain Removal: ₹700). Partners are strictly prohibited from demanding cash tips or hidden charges.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "2. 7-Day Service Guarantee", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "If you are unsatisfied with any aspect of the cleaning, report it via Customer Support within 7 days for a free re-cleaning or proportionate refund.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "3. Verified Partners & Safety", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All service partners are background-verified and adhere to Cleankr safety standards. Customers must provide access to running water and electricity for high-pressure machine operations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )
                        }
                        else -> {
                            Text(text = "1. Mobile Number & Account Verification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your mobile phone number is collected to authenticate your customer account via one-time passcodes (OTP), prevent unauthorized logins, and protect your bookings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "2. Customer Profile Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your name and email address are used to personalize service confirmations, issue tax invoices, and facilitate account recovery.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "3. Booking & Scheduling Data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Service selections, chosen time slots, specialized cleaning add-ons, and customer special instructions are shared strictly with the assigned service professional to deliver your requested service.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "4. Address & Location Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your building flat number, street address, and landmarks are used solely for partner route dispatch and navigation during the scheduled service window.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "5. Payment Information & Invoicing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Payments are processed through secure, PCI-DSS compliant payment gateways. Cleankr never stores raw credit/debit card numbers or UPI PINs on its servers. We retain transaction IDs and GST invoice records as required by tax laws.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "6. Partner Assignment & Number Masking", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "For mutual privacy, telephone communications between customers and hygiene partners are conducted via virtual masked telephony. Partners never receive your personal mobile number.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "7. Push Notifications & FCM", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "We use Firebase Cloud Messaging (FCM) to transmit real-time booking alerts, partner dispatch notices, and service completion confirmations. You can control notification permissions in your device settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "8. Support & Dispute Records", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customer care inquiries, feedback tickets, and resolution correspondence are maintained to guarantee service quality and resolve any customer concerns.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "9. Security & Fraud Prevention", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Firebase App Check, SSL/TLS transport encryption, and granular Firestore security rules protect customer records against unauthorized tampering and fraud.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "10. Statutory Data Retention", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Completed billing receipts, tax records, and legal transaction entries are archived for the duration mandated by applicable statutory tax and accounting regulations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "11. Account Deletion & Data Rights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You have the right to delete your customer account at any time through Account Settings. Deletion irreversibly anonymizes personal profile data and removes saved addresses. Statutory financial records are preserved as required by law.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "12. Children's Privacy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cleankr services are intended solely for individuals aged 18 and older. We do not knowingly collect personal information from minors.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "13. Policy Changes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "We may update our Privacy Policy periodically. Significant changes are notified through the Customer App or via registered contact channels.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "14. Contact & Grievance Officer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "For data privacy inquiries or grievance redressal, contact our Data Protection team at privacy@cleankr.com or through the Customer Support helpline.",
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
}
