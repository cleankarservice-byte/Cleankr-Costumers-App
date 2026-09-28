package com.example.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.CleankrHub
import com.example.core.model.CrossHubAttempt
import com.example.core.repository.AdminHubRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHubScreen(
    adminHubRepository: AdminHubRepository,
    backendService: FirebaseBackendService,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val attempts by adminHubRepository.observeAllAttempts().collectAsState(initial = emptyList())
    val puneHubs = remember { backendService.puneHubs }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Cross-Hub Alerts, 1: Pune Hubs, 2: Coverage Checker
    var alertFilter by remember { mutableStateOf("ALL") } // ALL, NEW_ALERT, ROUTED, OUT_OF_COVERAGE
    var showSimulateDialog by remember { mutableStateOf(false) }

    val newAlertsCount = remember(attempts) { attempts.count { it.status == "NEW_ALERT" } }
    val routedCount = remember(attempts) { attempts.count { it.status == "ROUTED" } }

    val filteredAttempts = remember(attempts, alertFilter) {
        when (alertFilter) {
            "NEW_ALERT" -> attempts.filter { it.status == "NEW_ALERT" }
            "ROUTED" -> attempts.filter { it.status == "ROUTED" }
            "OUT_OF_COVERAGE" -> attempts.filter { it.attemptType == "OUT_OF_COVERAGE" }
            else -> attempts
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cleankr Admin Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = CleankrNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CleankrTealLight
                            ) {
                                Text(
                                    text = "Pune Hubs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Cross-Hub Attempts & Division Operations",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleankrSlate
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSimulateDialog = true },
                        modifier = Modifier.testTag("admin_simulate_attempt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Simulate Other Hub Attempt",
                            tint = CleankrTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleankrCardSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CleankrBackground)
        ) {
            // High-priority Alert Banner if any new cross-hub attempts exist
            if (newAlertsCount > 0) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$newAlertsCount Other-Hub Booking Attempt(s) Detected!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Customers attempted booking from different Pune hubs. Route to correct hub below.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "New Hub Alerts",
                    value = newAlertsCount.toString(),
                    color = if (newAlertsCount > 0) Color(0xFFDC2626) else CleankrSlate,
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Routed to Hub",
                    value = routedCount.toString(),
                    color = CleankrTeal,
                    icon = Icons.Default.AltRoute,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Pune Hubs",
                    value = puneHubs.size.toString(),
                    color = CleankrNavy,
                    icon = Icons.Default.LocationCity,
                    modifier = Modifier.weight(1f)
                )
            }

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CleankrCardSurface,
                contentColor = CleankrTeal
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Cross-Hub Alerts")
                            if (newAlertsCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = CleankrError,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = newAlertsCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Pune 5 Hubs") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Pincode Checker") }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Filters row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = alertFilter == "ALL",
                                onClick = { alertFilter = "ALL" },
                                label = { Text("All (${attempts.size})") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = alertFilter == "NEW_ALERT",
                                onClick = { alertFilter = "NEW_ALERT" },
                                label = { Text("New Alerts ($newAlertsCount)") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = alertFilter == "ROUTED",
                                onClick = { alertFilter = "ROUTED" },
                                label = { Text("Routed ($routedCount)") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = alertFilter == "OUT_OF_COVERAGE",
                                onClick = { alertFilter = "OUT_OF_COVERAGE" },
                                label = { Text("Unserved Leads") }
                            )
                        }
                    }

                    if (filteredAttempts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CleankrTeal,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Cross-Hub Alerts Found", fontWeight = FontWeight.Bold, color = CleankrNavy)
                                Text(
                                    "Tap the '+' icon above to simulate an out-of-hub booking attempt.",
                                    fontSize = 12.sp,
                                    color = CleankrSlate
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredAttempts, key = { it.id }) { attempt ->
                                CrossHubAttemptCard(
                                    attempt = attempt,
                                    puneHubs = puneHubs,
                                    onRouteToDetectedHub = {
                                        val detected = puneHubs.firstOrNull { it.hubId == attempt.actualDetectedHubId }
                                            ?: puneHubs.first()
                                        coroutineScope.launch {
                                            adminHubRepository.routeAttemptToHub(
                                                attemptId = attempt.id,
                                                targetHub = detected,
                                                note = "Admin routed customer to ${detected.hubName} fleet"
                                            )
                                            Toast.makeText(context, "Routed to ${detected.hubName}!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onRouteToSpecificHub = { hub ->
                                        coroutineScope.launch {
                                            adminHubRepository.routeAttemptToHub(
                                                attemptId = attempt.id,
                                                targetHub = hub,
                                                note = "Manually assigned to ${hub.hubName}"
                                            )
                                            Toast.makeText(context, "Assigned to ${hub.hubName}!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onCallCustomer = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${attempt.customerPhone}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    onWhatsAppCustomer = {
                                        val cleanPhone = attempt.customerPhone.replace("[^0-9]".toRegex(), "")
                                        val message = "Hello ${attempt.customerName}, this is Cleankr Pune Operations regarding your booking request for ${attempt.serviceTitle} (${attempt.attemptedPincode}). We are coordinating your service from our Pune Hubs fleet."
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onMarkContacted = {
                                        coroutineScope.launch {
                                            adminHubRepository.updateAttemptStatus(attempt.id, "CONTACTED", "Customer called by operations manager")
                                            Toast.makeText(context, "Marked as Contacted", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Pune 5 Hubs Division List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = CleankrTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Pune is divided into 5 active operating hubs. Cross-hub bookings from unassigned divisions are instantly flagged here.",
                                        fontSize = 12.sp,
                                        color = CleankrNavy
                                    )
                                }
                            }
                        }

                        items(puneHubs) { hub ->
                            PuneHubDetailCard(hub = hub)
                        }
                    }
                }

                2 -> {
                    // Pincode Hub Lookup
                    PincodeCoverageCheckerScreen(puneHubs = puneHubs)
                }
            }
        }
    }

    // Dialog for Simulating an Other-Hub Attempt for testing
    if (showSimulateDialog) {
        AlertDialog(
            onDismissRequest = { showSimulateDialog = false },
            title = { Text("Simulate Other-Hub Attempt", fontWeight = FontWeight.Bold, color = CleankrNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Test how a customer booking from another Pune hub triggers real-time alerts in this Admin Panel.",
                        fontSize = 13.sp,
                        color = CleankrSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Example Simulation:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CleankrNavy
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("👤 Customer: Vikram Joshi (+91 98224 55102)", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("📍 Address: Wakad, Hinjawadi Road (411057)", fontSize = 11.sp)
                            Text("❌ Customer Hub: Pune East Hub (Viman Nagar)", fontSize = 11.sp, color = Color(0xFFDC2626))
                            Text("✅ Actual Hub: Pune North Hub (Hinjawadi)", fontSize = 11.sp, color = CleankrTeal)
                            Text("🧹 Service: Full Home Deep Cleaning (₹3,499)", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val eastHub = puneHubs.first { it.hubId == "hub_pune_east" }
                            val northHub = puneHubs.first { it.hubId == "hub_pune_north" }
                            adminHubRepository.simulateSampleAttempt(
                                hubFrom = eastHub,
                                hubActual = northHub,
                                addressText = "Flat 204, Rohan Tarang, Wakad, Pune",
                                pincode = "411057",
                                serviceTitle = "Full Home Deep Cleaning",
                                amount = 3499
                            )
                            showSimulateDialog = false
                            Toast.makeText(context, "New Other-Hub Attempt Added to Admin Panel! 🚨", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal)
                ) {
                    Text("Trigger Test Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CrossHubAttemptCard(
    attempt: CrossHubAttempt,
    puneHubs: List<CleankrHub>,
    onRouteToDetectedHub: () -> Unit,
    onRouteToSpecificHub: (CleankrHub) -> Unit,
    onCallCustomer: () -> Unit,
    onWhatsAppCustomer: () -> Unit,
    onMarkContacted: () -> Unit
) {
    var showAssignMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(attempt.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(attempt.timestamp))
    }

    val isNew = attempt.status == "NEW_ALERT"
    val isRouted = attempt.status == "ROUTED"
    val isCrossHub = attempt.attemptType == "CROSS_HUB"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNew) Color(0xFFFFFBEB) else CleankrCardSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Status badge & timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isNew && isCrossHub -> Color(0xFFFEE2E2)
                        isNew -> Color(0xFFFEF3C7)
                        isRouted -> Color(0xFFD1FAE5)
                        else -> Color(0xFFE0E7FF)
                    }
                ) {
                    Text(
                        text = when {
                            isNew && isCrossHub -> "🚨 OTHER HUB ATTEMPT"
                            isNew -> "⚠️ OUT OF COVERAGE"
                            isRouted -> "✅ ROUTED TO HUB"
                            else -> "📞 CONTACTED"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isNew && isCrossHub -> Color(0xFFDC2626)
                            isNew -> Color(0xFFB45309)
                            isRouted -> Color(0xFF065F46)
                            else -> Color(0xFF3730A3)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = CleankrSlate
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = attempt.serviceTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CleankrNavy
                    )
                    if (attempt.variantName.isNotBlank()) {
                        Text(
                            text = attempt.variantName,
                            fontSize = 12.sp,
                            color = CleankrSlate
                        )
                    }
                }

                if (attempt.estimatedAmount > 0) {
                    Text(
                        text = "₹${attempt.estimatedAmount}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = CleankrTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Contact Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CleankrBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = attempt.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CleankrNavy
                        )
                        Text(
                            text = attempt.customerPhone,
                            fontSize = 12.sp,
                            color = CleankrSlate
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onCallCustomer,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CleankrTealLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Customer",
                                tint = CleankrTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onWhatsAppCustomer,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Discrepancy Indicator Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Customer Chose: ${attempt.customerSelectedHubName}",
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CleankrTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Actual Hub Location: ${attempt.actualDetectedHubName ?: "Outside Hub Area"}",
                            fontSize = 12.sp,
                            color = CleankrTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (attempt.reason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = attempt.reason,
                            fontSize = 11.sp,
                            color = CleankrSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address Text
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = CleankrSlate,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${attempt.attemptedAddressText} (PIN: ${attempt.attemptedPincode})",
                    fontSize = 12.sp,
                    color = CleankrNavy
                )
            }

            if (isRouted && attempt.assignedHubName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFECFDF5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Assigned & Dispatched to: ${attempt.assignedHubName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            // Action Buttons
            if (isNew) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Route Button
                    Button(
                        onClick = onRouteToDetectedHub,
                        colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AltRoute,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Route to Correct Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Assign Hub Menu
                    Box {
                        OutlinedButton(
                            onClick = { showAssignMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Reassign...", fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showAssignMenu,
                            onDismissRequest = { showAssignMenu = false }
                        ) {
                            puneHubs.forEach { hub ->
                                DropdownMenuItem(
                                    text = { Text(hub.divisionArea) },
                                    onClick = {
                                        showAssignMenu = false
                                        onRouteToSpecificHub(hub)
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Mark as Contacted") },
                                onClick = {
                                    showAssignMenu = false
                                    onMarkContacted()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PuneHubDetailCard(hub: CleankrHub) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                        text = hub.hubName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CleankrNavy
                    )
                    Text(
                        text = "Division: ${hub.divisionArea}",
                        fontSize = 12.sp,
                        color = CleankrTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (hub.isActive) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = if (hub.isActive) "ACTIVE FLEET" else "STANDBY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hub.isActive) Color(0xFF15803D) else CleankrSlate,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Key Localities: ${hub.keyLocalities}",
                fontSize = 12.sp,
                color = CleankrSlate
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Covered Pincodes:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CleankrNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(hub.coveredPincodes) { pin ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CleankrTealLight
                    ) {
                        Text(
                            text = pin,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleankrTeal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Staff: ${hub.activeStaff} Certified Pros",
                    fontSize = 12.sp,
                    color = CleankrSlate
                )
                Text(
                    text = "Dispatcher: ${hub.contactPhone}",
                    fontSize = 12.sp,
                    color = CleankrNavy,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PincodeCoverageCheckerScreen(puneHubs: List<CleankrHub>) {
    var searchPincode by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<Pair<Boolean, CleankrHub?>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pune Pincode Division Checker",
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavy
                )
                Text(
                    text = "Enter any 6-digit Pune pincode to see which hub division covers it.",
                    fontSize = 12.sp,
                    color = CleankrSlate
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchPincode,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            searchPincode = input
                            if (input.length == 6) {
                                val match = puneHubs.firstOrNull { it.coveredPincodes.contains(input) }
                                searchResult = Pair(match != null, match)
                            } else {
                                searchResult = null
                            }
                        }
                    },
                    label = { Text("Enter 6-digit Pune Pincode") },
                    placeholder = { Text("e.g. 411057 or 411038") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                searchResult?.let { (isCovered, hub) ->
                    if (isCovered && hub != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Covered under ${hub.hubName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF14532D)
                                    )
                                    Text(
                                        text = "Division: ${hub.divisionArea} • ${hub.keyLocalities}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Out of Hub Division ($searchPincode)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Customer bookings from this pincode will be logged as 'Out-of-Coverage' in the Admin Panel.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = CleankrSlate,
                maxLines = 1
            )
        }
    }
}
