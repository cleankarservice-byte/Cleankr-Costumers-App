package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.Booking
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.core.model.UserProfile
import com.example.ui.components.CleankrStrings
import com.example.ui.components.DoorstepPinCard
import com.example.ui.components.InvoiceShareHelper
import com.example.ui.components.ReferAndEarnCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    categories: List<ServiceCategory>,
    services: List<ServiceItem>,
    activeBookings: List<Booking>,
    backendService: FirebaseBackendService,
    onCategoryClick: (String) -> Unit,
    onServiceClick: (String) -> Unit,
    onBookingClick: (String) -> Unit,
    onViewAllServices: () -> Unit,
    onNavigateToAdminHub: () -> Unit = {},
    unresolvedAlertsCount: Int = 0
) {
    val context = LocalContext.current
    val lang = userProfile.language

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = CleankrTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pune Hubs Active (5 Divisions)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavy
                                )
                            }
                            Text(
                                text = "Kothrud, Viman Nagar, Hinjawadi, Hadapsar, Swargate",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleankrSlate
                            )
                        }

                        // Quick Admin Panel Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (unresolvedAlertsCount > 0) Color(0xFFFEF2F2) else CleankrTealLight,
                            modifier = Modifier
                                .clickable { onNavigateToAdminHub() }
                                .testTag("home_admin_panel_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    tint = if (unresolvedAlertsCount > 0) CleankrError else CleankrTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (unresolvedAlertsCount > 0) "$unresolvedAlertsCount 🚨" else "Admin",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unresolvedAlertsCount > 0) CleankrError else CleankrTeal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Banner Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.home_cleaning_banner),
                            contentDescription = "Deep Cleaning Offer",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                color = CleankrAccent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ADMIN VERIFIED PRICES",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CleankrNavy,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Professional Home\n& Deep Cleaning",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Starts at ₹450 • 100% Background Verified",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Active Booking Highlight (Doorstep Safety PIN)
        if (activeBookings.isNotEmpty()) {
            val active = activeBookings.first()
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Booking In Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookingClick(active.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = active.serviceTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavy
                                )
                                Surface(
                                    color = CleankrTealLight,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = active.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CleankrTeal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${active.bookingDate} • ${active.slotTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleankrSlate
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Doorstep Safety PIN Display
                            DoorstepPinCard(startPin = active.startPin, lang = lang)
                        }
                    }
                }
            }
        }

        // Refer & Earn Banner (Feature 7)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ReferAndEarnCard(
                    referralCode = userProfile.referralCode,
                    lang = lang,
                    onShare = {
                        InvoiceShareHelper.shareReferral(context, userProfile.referralCode)
                    }
                )
            }
        }

        // Categories Grid / List
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CleankrStrings.get("services", lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                    TextButton(onClick = onViewAllServices) {
                        Text(
                            text = "View All",
                            color = CleankrTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { onCategoryClick(category.id) }
                                .testTag("category_card_${category.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(CleankrTealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (category.id) {
                                            "cat_bathroom" -> Icons.Default.Bathtub
                                            "cat_kitchen" -> Icons.Default.Kitchen
                                            "cat_fullhome" -> Icons.Default.Home
                                            "cat_sofa" -> Icons.Default.Chair
                                            else -> Icons.Default.CleaningServices
                                        },
                                        contentDescription = category.name,
                                        tint = CleankrTeal
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavy,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Popular Services
        item {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    text = "Popular Deep Cleaning Services",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavy
                )
                Text(
                    text = "Direct booking with guaranteed pricing",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleankrSlate
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        items(services) { service ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onServiceClick(service.id) }
                    .testTag("service_item_${service.id}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CleankrTealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (service.categoryId) {
                                "cat_bathroom" -> Icons.Default.Bathtub
                                "cat_kitchen" -> Icons.Default.Kitchen
                                "cat_fullhome" -> Icons.Default.Home
                                else -> Icons.Default.CleaningServices
                            },
                            contentDescription = service.title,
                            tint = CleankrTeal,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = service.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleankrNavy
                            )
                            if (service.isPopular) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "HOT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = service.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CleankrSlate,
                            maxLines = 2,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Starts at ₹${service.basePrice}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = CleankrTeal
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = CleankrAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${service.rating} (${service.reviewCount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrNavy
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
