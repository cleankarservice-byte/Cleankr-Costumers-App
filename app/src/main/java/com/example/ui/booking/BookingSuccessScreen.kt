package com.example.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.repository.BookingRepository
import com.example.ui.components.CleankrButton
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
import com.example.ui.theme.CleankrTealContainer
import com.example.ui.theme.CleankrTealDark

@Composable
fun BookingSuccessScreen(
    bookingId: String,
    bookingRepository: BookingRepository,
    onTrackBooking: (String) -> Unit,
    onGoToHome: () -> Unit
) {
    val booking by bookingRepository.getBookingById(bookingId).collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(30.dp))

            // Animated Success Circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(CleankrGreenLight)
                    .border(2.dp, CleankrGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = CleankrGreen,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Booking Confirmed! 🎉",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Booking ID: $bookingId",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrTealDark
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Start PIN Security Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_pin_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrOrangeContainer.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CleankrOrange))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = CleankrOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOUR CONFIDENTIAL START PIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CleankrOrange,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = booking?.startPin ?: "4821",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleankrNavyDark,
                        letterSpacing = 8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Share this 4-digit code ONLY with your partner when they arrive at your doorstep to start the service.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleankrSlate,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Booking Details Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Service Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavyDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = booking?.serviceTitle ?: "Bathroom Intense Clean",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CleankrTealDark
                    )
                    Text(
                        text = "Package: ${booking?.variantName ?: "1 Bathroom"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleankrSlate
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CleankrBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Scheduled For", style = MaterialTheme.typography.bodySmall, color = CleankrMuted)
                        Text(
                            text = "${booking?.bookingDate} • ${booking?.slotTime}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Amount Paid", style = MaterialTheme.typography.bodySmall, color = CleankrMuted)
                        Text(
                            text = "₹${booking?.totalAmount ?: 0} (${booking?.paymentMethod?.displayName ?: "Online"})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CleankrOrange
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            CleankrButton(
                text = "Track Live Status",
                onClick = { onTrackBooking(bookingId) },
                modifier = Modifier.testTag("track_live_status_btn")
            )

            Spacer(modifier = Modifier.height(10.dp))

            CleankrButton(
                text = "Back to Home",
                onClick = onGoToHome,
                isSecondary = true,
                modifier = Modifier.testTag("back_to_home_btn")
            )
        }
    }
}
