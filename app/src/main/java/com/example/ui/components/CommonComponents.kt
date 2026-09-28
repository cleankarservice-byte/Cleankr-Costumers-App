package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Booking
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleankrTopAppBar(
    title: String,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    currentLanguage: String = "en",
    onToggleLanguage: () -> Unit = {},
    unreadNotificationsCount: Int = 0,
    onNotificationsClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CleankrNavy
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CleankrNavy
                    )
                }
            }
        },
        actions = {
            // Language Switcher Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CleankrTealLight,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onToggleLanguage() }
                    .testTag("language_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Toggle Language",
                        tint = CleankrTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLanguage == "en") "हिन्दी" else "ENG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CleankrTeal
                    )
                }
            }

            // Notifications
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("top_bar_notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(
                                containerColor = CleankrError,
                                contentColor = Color.White
                            ) {
                                Text("$unreadNotificationsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = CleankrNavy
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CleankrCardSurface)
    )
}

@Composable
fun DoorstepPinCard(
    startPin: String,
    lang: String = "en",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(16.dp))
            .testTag("doorstep_pin_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security PIN",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = CleankrStrings.get("doorstep_otp", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF16A34A)),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                Text(
                    text = startPin.ifBlank { "----" },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp,
                    color = Color(0xFF15803D),
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .testTag("start_pin_text")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CleankrStrings.get("doorstep_otp_desc", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF166534),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun ReferAndEarnCard(
    referralCode: String,
    lang: String = "en",
    onShare: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(16.dp))
            .testTag("refer_earn_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Gift",
                        tint = Color(0xFFD97706)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = CleankrStrings.get("refer_earn", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Invite Friends • Instant Discount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = CleankrStrings.get("refer_desc", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF78350F)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Text(
                        text = referralCode,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("share_whatsapp_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = CleankrStrings.get("share_on_whatsapp", lang),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RatingReviewDialog(
    bookingTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Float, review: String) -> Unit
) {
    var selectedStars by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val tags = listOf("Punctual", "Polite Partner", "Thorough Scrubbing", "Spotless Hygiene", "Smells Fresh")
    val selectedTags = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rate & Review Service",
                fontWeight = FontWeight.Bold,
                color = CleankrNavy
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bookingTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CleankrSlate
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Star Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedStars = i },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= selectedStars) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "$i Stars",
                                tint = if (i <= selectedStars) CleankrAccent else CleankrSlate,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "What did you like?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            },
                            label = { Text(tag, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Write additional feedback (optional)...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("review_input_field"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val combinedReview = if (selectedTags.isNotEmpty()) {
                        "Tags: ${selectedTags.joinToString(", ")}. $reviewText".trim()
                    } else reviewText
                    onSubmit(selectedStars.toFloat(), combinedReview)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                modifier = Modifier.testTag("submit_rating_button")
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RescheduleBookingDialog(
    currentDate: String,
    currentSlot: String,
    onDismiss: () -> Unit,
    onConfirm: (newDate: String, newSlot: String) -> Unit
) {
    val dates = listOf("Tomorrow", "Day After Tomorrow", "This Weekend")
    val slots = listOf("08:00 AM - 10:00 AM", "11:00 AM - 01:00 PM", "02:00 PM - 04:00 PM", "05:00 PM - 07:00 PM")
    var selectedDate by remember { mutableStateOf(dates[0]) }
    var selectedSlot by remember { mutableStateOf(slots[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reschedule Booking",
                fontWeight = FontWeight.Bold,
                color = CleankrNavy
            )
        },
        text = {
            Column {
                Text(
                    text = "Current: $currentDate at $currentSlot",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CleankrSlate
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text("Select New Date:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    dates.forEach { d ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDate = d }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = (selectedDate == d), onClick = { selectedDate = d })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(d)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Select New Slot:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    slots.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSlot = s }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = (selectedSlot == s), onClick = { selectedSlot = s })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDate, selectedSlot) },
                colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                modifier = Modifier.testTag("confirm_reschedule_button")
            ) {
                Text("Confirm Slot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Existing")
            }
        }
    )
}

object InvoiceShareHelper {
    fun shareInvoice(context: Context, booking: Booking) {
        val invoiceText = """
========================================
        CLEANKR BILL / INVOICE
    Professional Home Cleaning Services
========================================
Booking ID : #${booking.id}
Date & Slot: ${booking.bookingDate} (${booking.slotTime})
Status     : ${booking.status}
Hub        : ${booking.hubName ?: "Cleankr Operations Hub"}

CUSTOMER & ADDRESS:
----------------------------------------
Name       : Customer
Phone      : ${booking.address.contactPhone}
Address    : ${booking.address.flatNo}, ${booking.address.street}, ${booking.address.city} - ${booking.address.pincode}

SERVICE BREAKDOWN:
----------------------------------------
Service    : ${booking.serviceTitle}
Variant    : ${booking.variantName} (Qty: ${booking.quantity})
Base Price : ₹${booking.basePrice}
Add-ons    : ₹${booking.addOnPrice}
${if (booking.selectedAddOns.isNotEmpty()) booking.selectedAddOns.joinToString("\n") { " - " + it.name + ": ₹" + it.price } else ""}
Taxes (GST): ₹0 (0% Tax / Zero Extra Charges)
----------------------------------------
TOTAL PAID : ₹${booking.totalAmount} (All Inclusive)
Payment    : ${booking.paymentMethod} (${booking.paymentStatus})
Safety PIN : ${booking.startPin}
========================================
Thank you for choosing Cleankr!
Support: +91 80000 12345 | cleankarservice@gmail.com
========================================
""".trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, invoiceText)
            putExtra(Intent.EXTRA_SUBJECT, "Cleankr Invoice #${booking.id}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Cleankr Bill / Invoice")
        context.startActivity(shareIntent)
    }

    fun shareReferral(context: Context, referralCode: String) {
        val text = """
Hey! I use Cleankr for professional home & deep cleaning services.
Use my invite code *$referralCode* to get ₹100 OFF on your first booking!
Clean home, trusted partners, 100% verified.
Book now on Cleankr!
""".trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Invite Friends via WhatsApp")
        context.startActivity(shareIntent)
    }
}
