package com.example.ui.tracking

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import com.example.core.model.Booking
import com.example.core.repository.BookingRepository
import com.example.ui.components.DoorstepPinCard
import com.example.ui.components.InvoiceShareHelper
import com.example.ui.components.RatingReviewDialog
import com.example.ui.components.RescheduleBookingDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BookingTrackingScreen(
    booking: Booking,
    bookingRepository: BookingRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showRescheduleDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }

    if (showRescheduleDialog) {
        RescheduleBookingDialog(
            currentDate = booking.bookingDate,
            currentSlot = booking.slotTime,
            onDismiss = { showRescheduleDialog = false },
            onConfirm = { newDate, newSlot ->
                coroutineScope.launch {
                    bookingRepository.rescheduleBooking(booking.id, newDate, newSlot)
                    showRescheduleDialog = false
                }
            }
        )
    }

    if (showRatingDialog) {
        RatingReviewDialog(
            bookingTitle = booking.serviceTitle,
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, review ->
                coroutineScope.launch {
                    bookingRepository.submitRating(booking.id, rating, review)
                    showRatingDialog = false
                }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Booking", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Are you sure you want to cancel this booking? Free cancellation is available.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        placeholder = { Text("Reason for cancellation...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            bookingRepository.cancelBooking(booking.id, cancelReason.ifBlank { "Customer requested cancellation" })
                            showCancelDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CleankrError)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Booking")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
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
                        Column {
                            Text(
                                text = "Booking #${booking.id}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleankrNavy
                            )
                            Text(
                                text = "${booking.bookingDate} • ${booking.slotTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleankrSlate
                            )
                        }
                        Surface(
                            color = when (booking.status) {
                                "COMPLETED" -> Color(0xFFDCFCE7)
                                "CANCELLED" -> Color(0xFFFEE2E2)
                                else -> CleankrTealLight
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = booking.status,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when (booking.status) {
                                    "COMPLETED" -> CleankrSuccess
                                    "CANCELLED" -> CleankrError
                                    else -> CleankrTeal
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Assigned Hub: ${booking.hubName ?: "Cleankr Central Hub"}",
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavy,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Doorstep Safety PIN (Feature 1)
        if (booking.status != "CANCELLED") {
            item {
                DoorstepPinCard(startPin = booking.startPin)
            }
        }

        // Assigned Cleaning Partner Card
        if (booking.partnerName != null && booking.status != "CANCELLED") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Assigned Cleaning Specialist",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CleankrSlate
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(CleankrTealLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = booking.partnerName ?: "Cleankr Pro",
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavy,
                                    fontSize = 15.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = CleankrAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${booking.partnerRating ?: 4.9f} (${booking.partnerJobs ?: 300}+ cleans)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CleankrSlate
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+918000012345"))
                                    context.startActivity(dialIntent)
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = CleankrTeal)
                            }
                        }
                    }
                }
            }
        }

        // Tracking Milestones Pipeline
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Service Pipeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val steps = listOf(
                        "Booking Confirmed" to (booking.status != "CANCELLED"),
                        "Partner Assigned" to (booking.partnerId != null && booking.status != "CANCELLED"),
                        "Arrived at Doorstep (PIN Verification)" to (booking.status in listOf("PARTNER_ARRIVED", "IN_PROGRESS", "COMPLETED")),
                        "Hygiene Cleaning In Progress" to (booking.status in listOf("IN_PROGRESS", "COMPLETED")),
                        "Service Completed" to (booking.status == "COMPLETED")
                    )

                    steps.forEachIndexed { index, (label, isDone) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) CleankrSuccess else Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Default.Check else Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDone) CleankrNavy else CleankrSlate,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons (Invoice, Reschedule, Rate, Cancel)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Share Bill / Invoice Button (Feature 6)
                Button(
                    onClick = { InvoiceShareHelper.shareInvoice(context, booking) },
                    colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_invoice_button")
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Bill / Invoice (Receipt)", fontWeight = FontWeight.Bold)
                }

                // Reschedule Button (Feature 5) - only if active
                if (booking.status in listOf("CONFIRMED", "ASSIGNED")) {
                    OutlinedButton(
                        onClick = { showRescheduleDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reschedule_booking_button")
                    ) {
                        Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reschedule Date & Time Slot", color = CleankrTeal, fontWeight = FontWeight.Bold)
                    }
                }

                // Rate & Review Service (Feature 3) - if completed or already in progress
                Button(
                    onClick = { showRatingDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rate_service_button")
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (booking.userRating != null) "Review Submitted (${booking.userRating}★) - Update" else "Rate & Review Service",
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cancel button - only if not already cancelled or completed
                if (booking.status in listOf("CONFIRMED", "ASSIGNED")) {
                    TextButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Booking", color = CleankrError, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
