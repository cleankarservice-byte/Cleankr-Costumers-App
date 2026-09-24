package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.CustomerUser
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.core.repository.AddressRepository
import com.example.core.repository.BookingRepository
import com.example.core.repository.NotificationRepository
import com.example.core.repository.ServiceRepository
import com.example.ui.components.CleankrTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CleankrBackground
import com.example.ui.theme.CleankrBorder
import com.example.ui.theme.CleankrGreen
import com.example.ui.theme.CleankrGreenLight
import com.example.ui.theme.CleankrMuted
import com.example.ui.theme.CleankrNavyDark
import com.example.ui.theme.CleankrOrange
import com.example.ui.theme.CleankrOrangeContainer
import com.example.ui.theme.CleankrSlate
import com.example.ui.theme.CleankrTeal
import com.example.ui.theme.CleankrTealLight
import com.example.ui.theme.CleankrTealContainer
import com.example.ui.theme.CleankrTealDark

@Composable
fun HomeScreen(
    currentUser: CustomerUser?,
    serviceRepository: ServiceRepository,
    bookingRepository: BookingRepository,
    addressRepository: AddressRepository,
    notificationRepository: NotificationRepository,
    onCategoryClick: (ServiceCategory) -> Unit,
    onServiceClick: (String) -> Unit,
    onBookingClick: (String) -> Unit,
    onAddressClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val services by serviceRepository.services.collectAsState(initial = emptyList())
    val activeBookings by bookingRepository.activeBookings.collectAsState(initial = emptyList())
    val allBookings by bookingRepository.allBookings.collectAsState(initial = emptyList())
    val defaultAddress by addressRepository.defaultAddress.collectAsState(initial = null)
    val notifications by notificationRepository.notifications.collectAsState(initial = emptyList())

    val unreadNotifsCount = notifications.count { !it.isRead }
    var searchQuery by remember { mutableStateOf("") }

    val filteredServices = remember(searchQuery, services) {
        if (searchQuery.isBlank()) services else serviceRepository.searchServices(searchQuery)
    }

    val upcomingBooking = activeBookings.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        // Sticky Header
        CleankrTopBar(
            customerName = currentUser?.name?.split(" ")?.firstOrNull() ?: "Rajesh",
            currentAddressText = defaultAddress?.let { "${it.label} • ${it.flatNo}" } ?: "Prestige Acropolis, Koramangala",
            unreadNotificationsCount = unreadNotifsCount,
            onAddressClick = onAddressClick,
            onNotificationsClick = onNotificationsClick,
            onProfileClick = onProfileClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Search Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search 'Bathroom clean', 'Kitchen degrease'...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CleankrTeal
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { searchQuery = "" }
                                        .padding(8.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = CleankrTeal,
                            unfocusedBorderColor = CleankrBorder
                        )
                    )
                }
            }

            // If Search query active, show search results directly
            if (searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "Search Results (${filteredServices.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavyDark,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(filteredServices) { service ->
                    ServiceSearchCard(
                        service = service,
                        onClick = { onServiceClick(service.id) }
                    )
                }
            } else {
                // Hero Banner
                item {
                    HeroBanner(onBookNow = { onCategoryClick(ServiceCategory.BATHROOM) })
                }

                // Upcoming Booking Card (if exists)
                if (upcomingBooking != null) {
                    item {
                        UpcomingBookingCard(
                            booking = upcomingBooking,
                            onClick = { onBookingClick(upcomingBooking.id) }
                        )
                    }
                }

                // 5 Main Service Categories
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Service Categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleankrNavyDark
                            )
                            Text(
                                text = "5 Specialized",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleankrMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Grid/Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CategoryCircleItem(
                                category = ServiceCategory.BATHROOM,
                                icon = Icons.Default.Bathtub,
                                color = CleankrTeal,
                                onClick = { onCategoryClick(ServiceCategory.BATHROOM) }
                            )
                            CategoryCircleItem(
                                category = ServiceCategory.KITCHEN,
                                icon = Icons.Default.Kitchen,
                                color = CleankrOrange,
                                onClick = { onCategoryClick(ServiceCategory.KITCHEN) }
                            )
                            CategoryCircleItem(
                                category = ServiceCategory.FLAT,
                                icon = Icons.Default.Apartment,
                                color = CleankrTealDark,
                                onClick = { onCategoryClick(ServiceCategory.FLAT) }
                            )
                            CategoryCircleItem(
                                category = ServiceCategory.BALCONY,
                                icon = Icons.Default.Deck,
                                color = CleankrGreen,
                                onClick = { onCategoryClick(ServiceCategory.BALCONY) }
                            )
                            CategoryCircleItem(
                                category = ServiceCategory.OTHER,
                                icon = Icons.Default.MoreHoriz,
                                color = CleankrSlate,
                                onClick = { onCategoryClick(ServiceCategory.OTHER) }
                            )
                        }
                    }
                }

                // Popular Services
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Services",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark
                        )
                        Text(
                            text = "Fixed Company Pricing",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleankrTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(services.take(4)) { service ->
                            PopularServiceCard(
                                service = service,
                                onClick = { onServiceClick(service.id) }
                            )
                        }
                    }
                }

                // Help & Support Shortcut Card
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable(onClick = onSupportClick)
                            .testTag("home_support_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CleankrNavyDark
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(CleankrTeal.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = "Support",
                                        tint = CleankrTealLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Cleankr Customer Support",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Instant answers, booking help & refunds",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CleankrBorder
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go",
                                tint = CleankrOrange
                            )
                        }
                    }
                }

                // Recent Bookings (if any completed)
                val completedBookings = allBookings.filter { it.status.isTerminal }
                if (completedBookings.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Past Bookings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(completedBookings.take(2)) { b ->
                        RecentBookingCard(
                            booking = b,
                            onClick = { onBookingClick(b.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBanner(onBookNow: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CleankrNavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.home_cleaning_banner),
                contentDescription = "Cleankr Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                CleankrNavyDark.copy(alpha = 0.92f),
                                CleankrNavyDark.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.72f)
            ) {
                Surface(
                    color = CleankrOrange,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "CLEANKR PRO HYGIENE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Spotless Clean.\nGuaranteed Hygiene.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bathroom intense clean starting from ₹450 with German descaling.",
                    fontSize = 12.sp,
                    color = CleankrBorder,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CleankrOrange,
                    modifier = Modifier.clickable(onClick = onBookNow)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Book Bathroom Clean",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCircleItem(
    category: ServiceCategory,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("category_${category.name.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.displayName,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CleankrNavyDark
        )
    }
}

@Composable
fun PopularServiceCard(
    service: ServiceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
            .testTag("popular_service_${service.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CleankrTealContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = service.category.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleankrTealDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = CleankrOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = service.rating.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavyDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = service.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = service.shortDesc,
                style = MaterialTheme.typography.bodySmall,
                color = CleankrMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Starts at",
                        fontSize = 9.sp,
                        color = CleankrMuted
                    )
                    Text(
                        text = "₹${service.basePrice}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleankrNavyDark
                    )
                }

                Surface(
                    color = CleankrOrange,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Book",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingBookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
            .testTag("upcoming_booking_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleankrTealContainer.copy(alpha = 0.6f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CleankrTeal, CleankrTealDark)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CleankrOrange)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UPCOMING SERVICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleankrTealDark,
                        letterSpacing = 0.5.sp
                    )
                }

                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${booking.serviceTitle} (${booking.variantName})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "📅 ${booking.bookingDate} • ⏰ ${booking.slotTime}",
                style = MaterialTheme.typography.bodySmall,
                color = CleankrSlate,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Booking ID: ${booking.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleankrMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "Live Track",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CleankrOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentBookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.serviceTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Text(
                    text = "${booking.bookingDate} • ₹${booking.totalAmount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleankrMuted
                )
            }
            StatusBadge(status = booking.status)
        }
    }
}

@Composable
fun ServiceSearchCard(
    service: ServiceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = CleankrTealContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = service.category.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleankrTealDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Text(
                    text = service.shortDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = CleankrMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${service.basePrice}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = CleankrOrange
                )
                Text(
                    text = "View >",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleankrTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
