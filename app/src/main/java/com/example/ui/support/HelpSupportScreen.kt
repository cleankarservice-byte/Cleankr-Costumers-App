package com.example.ui.support

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.core.config.CleankrLegalConfig
import com.example.core.model.SupportTicket
import com.example.core.repository.SupportRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HelpSupportScreen(
    supportRepository: SupportRepository,
    tickets: List<SupportTicket>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showCreateTicketDialog by remember { mutableStateOf(false) }
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    val faqs = listOf(
        "What is the Doorstep Safety PIN?" to "The Doorstep Safety PIN is a unique 4-digit code generated for every booking. Your cleaning partner must verify this code at your door before starting the job, guaranteeing authorized service.",
        "How are service prices calculated?" to "All prices are standard, transparent, and loaded directly from our administrative catalog. What you see is what you pay—no hidden platform surcharges.",
        "Can I reschedule my service?" to "Yes! Free rescheduling is available up to 2 hours before the scheduled time slot from the Booking Details screen.",
        "Are the cleaning materials eco-friendly?" to "Yes, our certified partners carry specialized, non-acidic descalers, microfiber wipers, and hygiene sanitizers safe for children and pets.",
        "What if I'm not satisfied with the clean?" to "You can rate the partner and submit an in-app support ticket immediately. Our quality audit team will resolve or schedule a free touch-up."
    )

    if (showCreateTicketDialog) {
        var subject by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateTicketDialog = false },
            title = { Text("Raise Support Ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        placeholder = { Text("Subject (e.g. Rescheduling inquiry)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Describe the issue in detail...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank()) {
                            coroutineScope.launch {
                                supportRepository.createTicket(null, "GENERAL", subject, description)
                                showCreateTicketDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal)
                ) {
                    Text("Submit Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTicketDialog = false }) { Text("Cancel") }
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
        // Quick Contact Channels
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("24/7 Dedicated Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CleankrNavy)
                    Text("Instant assistance via WhatsApp or phone call", style = MaterialTheme.typography.labelSmall, color = CleankrSlate)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918000012345?text=Hello%20Cleankr%20Support"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("support_whatsapp_button")
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${CleankrLegalConfig.SUPPORT_PHONE}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("support_call_button")
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Us", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // FAQs Accordion
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CleankrNavy)
                    Spacer(modifier = Modifier.height(10.dp))

                    faqs.forEachIndexed { index, (q, a) ->
                        val isExpanded = expandedFaqIndex == index
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedFaqIndex = if (isExpanded) null else index
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(q, fontWeight = FontWeight.SemiBold, color = CleankrNavy, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = CleankrSlate
                                )
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Text(
                                    text = a,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CleankrSlate,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        if (index < faqs.size - 1) {
                            HorizontalDivider(color = CleankrBorder)
                        }
                    }
                }
            }
        }

        // Support Tickets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Support Tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CleankrNavy)
                TextButton(onClick = { showCreateTicketDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CleankrTeal)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Raise Ticket", color = CleankrTeal, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (tickets.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No tickets raised. Have a question? Reach us via WhatsApp or tap Raise Ticket.", style = MaterialTheme.typography.bodyMedium, color = CleankrSlate)
                    }
                }
            }
        } else {
            items(tickets) { t ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(t.id, fontWeight = FontWeight.Bold, color = CleankrTeal, fontSize = 12.sp)
                            Surface(
                                color = if (t.status == "OPEN") Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(t.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (t.status == "OPEN") Color(0xFFB45309) else CleankrSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(t.subject, fontWeight = FontWeight.Bold, color = CleankrNavy)
                        Text(t.description, style = MaterialTheme.typography.bodyMedium, color = CleankrSlate, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
