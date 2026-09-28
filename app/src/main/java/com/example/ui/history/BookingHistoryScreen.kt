package com.example.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Booking
import com.example.core.repository.BookingRepository
import com.example.ui.components.InvoiceShareHelper
import com.example.ui.components.RatingReviewDialog
import com.example.ui.components.RescheduleBookingDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BookingHistoryScreen(
    bookings: List<Booking>,
    bookingRepository: BookingRepository,
    onBookingClick: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Upcoming, 1: History

    var selectedBookingForReschedule by remember { mutableStateOf<Booking?>(null) }
    var selectedBookingForRating by remember { mutableStateOf<Booking?>(null) }

    val activeBookings = remember(bookings) {
        bookings.filter { it.status !in listOf("COMPLETED", "CANCELLED") }
    }
    val pastBookings = remember(bookings) {
        bookings.filter { it.status in listOf("COMPLETED", "CANCELLED") }
    }

    if (selectedBookingForReschedule != null) {
        val b = selectedBookingForReschedule!!
        RescheduleBookingDialog(
            currentDate = b.bookingDate,
            currentSlot = b.slotTime,
            onDismiss = { selectedBookingForReschedule = null },
            onConfirm = { newDate, newSlot ->
                coroutineScope.launch {
                    bookingRepository.rescheduleBooking(b.id, newDate, newSlot)
                    selectedBookingForReschedule = null
                }
            }
        )
    }

    if (selectedBookingForRating != null) {
        val b = selectedBookingForRating!!
        RatingReviewDialog(
            bookingTitle = b.serviceTitle,
            onDismiss = { selectedBookingForRating = null },
            onSubmit = { rating, review ->
                coroutineScope.launch {
                    bookingRepository.submitRating(b.id, rating, review)
                    selectedBookingForRating = null
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CleankrCardSurface,
            contentColor = CleankrTeal
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Upcoming (${activeBookings.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_upcoming_bookings")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Past & Completed (${pastBookings.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_past_bookings")
            )
        }

        val displayList = if (selectedTab == 0) activeBookings else pastBookings

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = CleankrSlate,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTab == 0) "No upcoming bookings" else "No past bookings yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                    Text(
                        text = "Your hygiene and home cleaning appointments will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleankrSlate
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayList) { booking ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookingClick(booking.id) }
                            .testTag("booking_card_${booking.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = booking.serviceTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavy
                                )
                                Surface(
                                    color = when (booking.status) {
                                        "COMPLETED" -> Color(0xFFDCFCE7)
                                        "CANCELLED" -> Color(0xFFFEE2E2)
                                        else -> CleankrTealLight
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = booking.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (booking.status) {
                                            "COMPLETED" -> CleankrSuccess
                                            "CANCELLED" -> CleankrError
                                            else -> CleankrTeal
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Variant: ${booking.variantName} (Qty: ${booking.quantity})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleankrSlate,
                                fontSize = 13.sp
                            )
                            if (booking.hubName != null) {
                                Text(
                                    text = "Hub: ${booking.hubName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrSlate
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${booking.bookingDate} • ${booking.slotTime}",
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavy,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = CleankrBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Amount Paid", fontSize = 11.sp, color = CleankrSlate)
                                    Text(
                                        text = "₹${booking.totalAmount}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CleankrTeal
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Share Bill / Invoice
                                    IconButton(
                                        onClick = { InvoiceShareHelper.shareInvoice(context, booking) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Receipt, contentDescription = "Share Bill", tint = CleankrTeal)
                                    }

                                    // Reschedule button if upcoming
                                    if (selectedTab == 0 && booking.status in listOf("CONFIRMED", "ASSIGNED")) {
                                        OutlinedButton(
                                            onClick = { selectedBookingForReschedule = booking },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reschedule", fontSize = 12.sp, color = CleankrTeal)
                                        }
                                    }

                                    // Rating button if past
                                    if (selectedTab == 1 && booking.status == "COMPLETED") {
                                        Button(
                                            onClick = { selectedBookingForRating = booking },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF3C7)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (booking.userRating != null) "${booking.userRating}★" else "Rate", fontSize = 12.sp, color = Color(0xFF92400E))
                                        }
                                    }

                                    Button(
                                        onClick = { onBookingClick(booking.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Track", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
