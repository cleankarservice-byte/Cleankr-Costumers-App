package com.example.ui.support

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.repository.FaqItem
import com.example.core.repository.SupportRepository
import com.example.ui.components.CleankrButton
import com.example.ui.components.CleankrStandardTopBar
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
import kotlinx.coroutines.launch

@Composable
fun HelpSupportScreen(
    supportRepository: SupportRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val faqs = remember { supportRepository.getFaqs() }
    val tickets by supportRepository.tickets.collectAsState(initial = emptyList())

    val expandedFaqs = remember { mutableStateListOf<String>() }
    var showCreateTicketDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(
            title = "Help & Support",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Direct Contact Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:18002092532")
                            }
                            context.startActivity(intent)
                        }
                        .testTag("call_support_hotline_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CleankrGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = CleankrGreen, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Call Cleankr Care", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Text(text = "Toll-Free 8am - 10pm", style = MaterialTheme.typography.labelSmall, color = CleankrMuted, fontSize = 10.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@cleankr.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Customer App Support Query")
                            }
                            context.startActivity(intent)
                        }
                        .testTag("email_support_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CleankrTealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = CleankrTealDark, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Email Us", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Text(text = "support@cleankr.com", style = MaterialTheme.typography.labelSmall, color = CleankrMuted, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Create Ticket CTA Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrOrangeContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CleankrOrange))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Need help with a booking?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Text(text = "Raise a support ticket and get resolved in 30 mins", style = MaterialTheme.typography.bodySmall, color = CleankrSlate)
                    }
                    TextButton(
                        onClick = { showCreateTicketDialog = true },
                        modifier = Modifier.testTag("open_ticket_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CleankrOrange)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Raise Ticket", fontWeight = FontWeight.Bold, color = CleankrOrange)
                    }
                }
            }

            // My Support Tickets (if any)
            if (tickets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "My Recent Support Tickets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                tickets.forEach { ticket ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = ticket.id, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CleankrTeal)
                                Surface(
                                    color = CleankrTealContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = ticket.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CleankrTealDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = ticket.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                            Text(text = ticket.description, style = MaterialTheme.typography.bodySmall, color = CleankrSlate)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Frequently Asked Questions
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                faqs.forEach { faq ->
                    val isExpanded = expandedFaqs.contains(faq.question)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isExpanded) expandedFaqs.remove(faq.question)
                                else expandedFaqs.add(faq.question)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = faq.question,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CleankrNavyDark,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = CleankrTeal
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    HorizontalDivider(color = CleankrBorder)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = faq.answer,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CleankrSlate,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Ticket Dialog
    if (showCreateTicketDialog) {
        var subject by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateTicketDialog = false },
            title = { Text("Raise Support Ticket", fontWeight = FontWeight.Bold, color = CleankrNavyDark) },
            text = {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. Reschedule help, invoice)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Describe your issue...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (subject.isNotBlank() && description.isNotBlank()) {
                            coroutineScope.launch {
                                supportRepository.createTicket(null, "GENERAL", subject, description)
                                showCreateTicketDialog = false
                            }
                        }
                    },
                    enabled = subject.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Submit Ticket", fontWeight = FontWeight.Bold, color = CleankrOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTicketDialog = false }) {
                    Text("Cancel", color = CleankrMuted)
                }
            }
        )
    }
}
