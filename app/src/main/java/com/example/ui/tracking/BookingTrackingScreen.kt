package com.example.ui.tracking

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Booking
import com.example.core.model.BookingStatus
import com.example.core.repository.BookingRepository
import com.example.core.repository.SlotRepository
import com.example.ui.components.CleankrButton
import com.example.ui.components.CleankrStandardTopBar
import com.example.ui.components.ErrorBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CleankrBackground
import com.example.ui.theme.CleankrBorder
import com.example.ui.theme.CleankrGreen
import com.example.ui.theme.CleankrGreenLight
import com.example.ui.theme.CleankrMuted
import com.example.ui.theme.CleankrNavyDark
import com.example.ui.theme.CleankrOrange
import com.example.ui.theme.CleankrOrangeContainer
import com.example.ui.theme.CleankrRed
import com.example.ui.theme.CleankrRedLight
import com.example.ui.theme.CleankrSlate
import com.example.ui.theme.CleankrTeal
import com.example.ui.theme.CleankrTealContainer
import com.example.ui.theme.CleankrTealDark
import kotlinx.coroutines.launch

@Composable
fun BookingTrackingScreen(
    bookingId: String,
    bookingRepository: BookingRepository,
    slotRepository: SlotRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val booking by bookingRepository.getBookingById(bookingId).collectAsState(initial = null)

    var showCancelDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    if (booking == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleankrBackground)
        ) {
            CleankrStandardTopBar(title = "Booking Tracking", onBackClick = onBackClick)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Booking not found.", color = CleankrMuted)
            }
        }
        return
    }

    val b = booking!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(
            title = "Track Booking #${b.id}",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (statusMessage != null) {
                Surface(
                    color = CleankrTealContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = CleankrTealDark,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Current Status Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = b.serviceTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark
                        )
                        StatusBadge(status = b.status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${b.variantName} • Qty: ${b.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleankrSlate
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "📅 ${b.bookingDate}  |  ⏰ ${b.slotTime}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CleankrTealDark
                    )
                }
            }

            // Start PIN Card (Show prominently if active and not completed/cancelled)
            if (!b.status.isTerminal) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tracking_start_pin_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CleankrOrangeContainer.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CleankrOrange))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CleankrOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Start PIN: ${b.startPin}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CleankrNavyDark,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "Share with partner on doorstep arrival",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrSlate
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Step Tracker Pipeline
            Text(
                text = "Service Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            TrackingPipelineView(currentStatus = b.status)

            // Partner Profile Card (If partner assigned)
            if (b.partner != null || b.status != BookingStatus.BOOKED && b.status != BookingStatus.CANCELLED) {
                val partner = b.partner ?: com.example.core.model.PartnerInfo(
                    id = "prt_409",
                    name = "Suresh Kumar",
                    rating = 4.92f,
                    jobsCompleted = 214,
                    maskedPhone = "+91 80 4719 3200"
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Assigned Service Professional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(CleankrTealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CleankrTealDark,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = partner.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CleankrNavyDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = CleankrTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = CleankrOrange,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${partner.rating} (${partner.jobsCompleted} completed jobs)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CleankrSlate
                                    )
                                }
                                Text(
                                    text = partner.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrTealDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Masked secure call button
                        IconButton(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${partner.maskedPhone}")
                                }
                                context.startActivity(callIntent)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CleankrGreenLight)
                                .testTag("call_partner_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Partner",
                                tint = CleankrGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Interactive Status Advancement for Review/Demo
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = CleankrTealContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cleankr Ecosystem Sync",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrTealDark
                        )
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = null, tint = CleankrTealDark, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time updates from Cleankr Partner App & Admin Panel. In test/offline mode, advance status below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleankrSlate,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val nextStatus = when (b.status) {
                        BookingStatus.BOOKED -> BookingStatus.ASSIGNED
                        BookingStatus.ASSIGNED -> BookingStatus.PARTNER_ACCEPTED
                        BookingStatus.PARTNER_ACCEPTED -> BookingStatus.ON_THE_WAY
                        BookingStatus.ON_THE_WAY -> BookingStatus.ARRIVED
                        BookingStatus.ARRIVED -> BookingStatus.STARTED
                        BookingStatus.STARTED -> BookingStatus.COMPLETED
                        else -> null
                    }

                    if (nextStatus != null) {
                        CleankrButton(
                            text = "Advance to: ${nextStatus.displayName}",
                            onClick = {
                                coroutineScope.launch {
                                    bookingRepository.advanceStatus(b.id, nextStatus)
                                    statusMessage = "Order transitioned to ${nextStatus.displayName}"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (b.status == BookingStatus.COMPLETED && b.userRating == null) {
                        CleankrButton(
                            text = "Rate This Service ⭐",
                            onClick = { showRatingDialog = true }
                        )
                    }
                }
            }

            // Actions: Reschedule & Cancel (Available if not started/completed/cancelled)
            if (!b.status.isTerminal && b.status != BookingStatus.STARTED && b.status != BookingStatus.ARRIVED) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRescheduleDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reschedule_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EventRepeat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Reschedule", color = CleankrTeal, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_booking_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CleankrRed)
                    ) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Cancel", color = CleankrRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Cancel Dialog
    if (showCancelDialog) {
        CancelBookingDialog(
            bookingId = b.id,
            onDismiss = { showCancelDialog = false },
            onConfirmCancel = { reason ->
                coroutineScope.launch {
                    bookingRepository.cancelBooking(b.id, reason)
                    showCancelDialog = false
                    statusMessage = "Booking has been cancelled."
                }
            }
        )
    }

    // Reschedule Dialog
    if (showRescheduleDialog) {
        RescheduleBookingDialog(
            booking = b,
            slotRepository = slotRepository,
            onDismiss = { showRescheduleDialog = false },
            onConfirmReschedule = { newDate, newSlot ->
                coroutineScope.launch {
                    bookingRepository.rescheduleBooking(b.id, newDate, newSlot)
                    showRescheduleDialog = false
                    statusMessage = "Booking rescheduled to $newDate, $newSlot."
                }
            }
        )
    }

    // Rating Dialog
    if (showRatingDialog) {
        RatingFeedbackDialog(
            bookingId = b.id,
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, review ->
                coroutineScope.launch {
                    bookingRepository.submitRating(b.id, rating, review)
                    showRatingDialog = false
                    statusMessage = "Thank you for your rating! ⭐"
                }
            }
        )
    }
}

@Composable
fun TrackingPipelineView(currentStatus: BookingStatus) {
    val steps = listOf(
        BookingStatus.BOOKED to "Booked",
        BookingStatus.ASSIGNED to "Assigned",
        BookingStatus.PARTNER_ACCEPTED to "Partner Accepted",
        BookingStatus.ON_THE_WAY to "On the Way",
        BookingStatus.ARRIVED to "Arrived",
        BookingStatus.STARTED to "Started",
        BookingStatus.COMPLETED to "Completed"
    )

    val currentStepIndex = if (currentStatus == BookingStatus.CANCELLED) -1 else currentStatus.stepIndex

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (currentStatus == BookingStatus.CANCELLED) {
                Surface(
                    color = CleankrRedLight,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "This booking was cancelled.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrRed,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                steps.forEachIndexed { index, (stepStatus, label) ->
                    val isDone = index < currentStepIndex
                    val isCurrent = index == currentStepIndex

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> CleankrGreen
                                            isCurrent -> CleankrOrange
                                            else -> CleankrBorder
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }

                            if (index < steps.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(24.dp)
                                        .background(if (index < currentStepIndex) CleankrGreen else CleankrBorder)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 18.dp else 0.dp)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = when {
                                    isCurrent -> CleankrOrange
                                    isDone -> CleankrNavyDark
                                    else -> CleankrMuted
                                }
                            )
                            if (isCurrent) {
                                Text(
                                    text = "In Progress",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CancelBookingDialog(
    bookingId: String,
    onDismiss: () -> Unit,
    onConfirmCancel: (reason: String) -> Unit
) {
    val reasons = listOf(
        "Change of plans / Emergency",
        "Booked wrong service or package",
        "Slot timing conflict",
        "Partner was delayed",
        "Found alternate service"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cancel Booking?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
        },
        text = {
            Column {
                Text(
                    text = "Free cancellation is eligible up to 2 hours before the scheduled slot. Any refund will be credited within 3-5 business days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleankrSlate
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Reason for cancellation:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = CleankrRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reason, style = MaterialTheme.typography.bodySmall, color = CleankrSlate)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmCancel(selectedReason) },
                colors = ButtonDefaults.textButtonColors(contentColor = CleankrRed)
            ) {
                Text("Confirm Cancel", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Booking", color = CleankrNavyDark)
            }
        }
    )
}

@Composable
fun RescheduleBookingDialog(
    booking: Booking,
    slotRepository: SlotRepository,
    onDismiss: () -> Unit,
    onConfirmReschedule: (newDate: String, newSlot: String) -> Unit
) {
    val upcomingDays = remember { slotRepository.getUpcomingDays(10) }
    var selectedDay by remember { mutableStateOf(upcomingDays.firstOrNull { it.isAvailable } ?: upcomingDays.first()) }
    var slots by remember { mutableStateOf(slotRepository.getTimeSlotsForDate(selectedDay.dateString)) }
    var selectedSlot by remember { mutableStateOf(slots.firstOrNull { it.isAvailable } ?: slots.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reschedule Booking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Select a new date and time slot for your ${booking.serviceTitle}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleankrMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Select Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                upcomingDays.forEach { day ->
                    if (day.isAvailable) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDay = day
                                    slots = slotRepository.getTimeSlotsForDate(day.dateString)
                                    val valid = slots.firstOrNull { it.isAvailable }
                                    if (valid != null) selectedSlot = valid
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDay.dateString == day.dateString,
                                onClick = {
                                    selectedDay = day
                                    slots = slotRepository.getTimeSlotsForDate(day.dateString)
                                    val valid = slots.firstOrNull { it.isAvailable }
                                    if (valid != null) selectedSlot = valid
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = CleankrTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${day.dayOfWeekText}, ${day.dayOfMonthText} ${day.monthText}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Select Slot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                slots.forEach { slot ->
                    if (slot.isAvailable) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSlot = slot }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSlot.id == slot.id,
                                onClick = { selectedSlot = slot },
                                colors = RadioButtonDefaults.colors(selectedColor = CleankrOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = slot.timeDisplay, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmReschedule(selectedDay.dateString, selectedSlot.timeDisplay) }
            ) {
                Text("Confirm Reschedule", fontWeight = FontWeight.Bold, color = CleankrTeal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CleankrMuted)
            }
        }
    )
}

@Composable
fun RatingFeedbackDialog(
    bookingId: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Float, review: String) -> Unit
) {
    var rating by remember { mutableFloatStateOf(5.0f) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rate Your Experience",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "How was the cleaning service?",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleankrSlate
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star.toFloat() }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= rating) CleankrOrange else CleankrBorder,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write a quick review for your partner...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(rating, reviewText) }
            ) {
                Text("Submit Rating", fontWeight = FontWeight.Bold, color = CleankrOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip", color = CleankrMuted)
            }
        }
    )
}
