package com.example.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Booking
import com.example.ui.components.DoorstepPinCard
import com.example.ui.theme.*

@Composable
fun BookingSuccessScreen(
    booking: Booking,
    onTrackBooking: () -> Unit,
    onBackToHome: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CleankrBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = CleankrSuccess,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Booking Confirmed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavy
                )
                Text(
                    text = "Booking ID #${booking.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CleankrSlate
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Doorstep Safety PIN Card
                DoorstepPinCard(startPin = booking.startPin)

                Spacer(modifier = Modifier.height(16.dp))

                // Booking summary card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(booking.serviceTitle, fontWeight = FontWeight.Bold, color = CleankrNavy)
                        Text(
                            text = "${booking.bookingDate} • ${booking.slotTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CleankrSlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Assigned Hub: ${booking.hubName ?: "Cleankr Central Hub"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleankrTeal
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTrackBooking,
                    colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("track_booking_button")
                ) {
                    Text("Track Booking & Cleaner", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("back_to_home_button")
                ) {
                    Text("Back to Home", color = CleankrNavy, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
