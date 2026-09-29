package com.example.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.config.CleankrLegalConfig
import com.example.core.data.session.SessionManager
import com.example.core.model.UserProfile
import com.example.ui.components.CleankrStrings
import com.example.ui.components.InvoiceShareHelper
import com.example.ui.components.ReferAndEarnCard
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    sessionManager: SessionManager,
    onNavigateToAddresses: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToLegal: (String, String) -> Unit,
    onNavigateToAdminHub: () -> Unit = {},
    unresolvedAlertsCount: Int = 0,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val lang = userProfile.language

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(CleankrTealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CleankrTeal,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavy
                        )
                        Text(
                            text = userProfile.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CleankrSlate
                        )
                        if (userProfile.email.isNotBlank()) {
                            Text(
                                text = userProfile.email,
                                style = MaterialTheme.typography.labelSmall,
                                color = CleankrSlate
                            )
                        }
                    }
                }
            }
        }

        // Feature 7: Refer & Earn Card
        item {
            ReferAndEarnCard(
                referralCode = userProfile.referralCode,
                lang = lang,
                onShare = {
                    InvoiceShareHelper.shareReferral(context, userProfile.referralCode)
                }
            )
        }

        // Language Switcher (Feature 8)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("App Language / भाषा", fontWeight = FontWeight.Bold, color = CleankrNavy)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = (lang == "en"),
                            onClick = { sessionManager.setLanguage("en") },
                            label = { Text("English (Default)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = (lang == "hi"),
                            onClick = { sessionManager.setLanguage("hi") },
                            label = { Text("हिन्दी (Hindi)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Manage Settings List
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Pune Hubs Admin Panel 🛡️",
                        subtitle = if (unresolvedAlertsCount > 0) "$unresolvedAlertsCount Other-Hub Alerts Need Action!" else "Division operations & cross-hub monitoring",
                        badge = if (unresolvedAlertsCount > 0) "$unresolvedAlertsCount NEW" else null,
                        badgeColor = CleankrError,
                        onClick = onNavigateToAdminHub
                    )
                    HorizontalDivider(color = CleankrBorder)
                    ProfileMenuRow(
                        icon = Icons.Default.LocationOn,
                        title = "Saved Addresses & Service Hubs",
                        subtitle = "Manage cleaning locations & pin codes",
                        onClick = onNavigateToAddresses
                    )
                    HorizontalDivider(color = CleankrBorder)
                    ProfileMenuRow(
                        icon = Icons.Default.SupportAgent,
                        title = "Help & 24/7 Customer Support",
                        subtitle = "WhatsApp, call & ticket resolution",
                        onClick = onNavigateToSupport
                    )
                }
            }
        }

        // Legal & Policy documents
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        subtitle = "Rules & customer guarantees",
                        onClick = { onNavigateToLegal("Terms of Service", CleankrLegalConfig.TERMS_OF_SERVICE) }
                    )
                    HorizontalDivider(color = CleankrBorder)
                    ProfileMenuRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "Location & user data protection",
                        onClick = { onNavigateToLegal("Privacy Policy", CleankrLegalConfig.PRIVACY_POLICY) }
                    )
                    HorizontalDivider(color = CleankrBorder)
                    ProfileMenuRow(
                        icon = Icons.Default.Policy,
                        title = "Cancellation & Refund Policy",
                        subtitle = "Free 2-hour rescheduling details",
                        onClick = { onNavigateToLegal("Cancellation Policy", CleankrLegalConfig.CANCELLATION_POLICY) }
                    )
                }
            }
        }

        // Sign Out Button
        item {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = CleankrError)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = CleankrError, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = CleankrTeal,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CleankrTealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, color = CleankrNavy)
                if (badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = CleankrSlate)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = CleankrSlate)
    }
}

@Composable
fun LegalDocumentScreen(
    title: String,
    content: String
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CleankrBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CleankrNavy)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(content.trim(), style = MaterialTheme.typography.bodyMedium, color = CleankrNavy, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}
