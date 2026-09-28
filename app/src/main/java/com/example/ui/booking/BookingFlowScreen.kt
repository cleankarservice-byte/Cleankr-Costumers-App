package com.example.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.config.CleankrLegalConfig
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.AddOnItem
import com.example.core.model.Address
import com.example.core.model.CleankrHub
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import com.example.core.repository.AdminHubRepository
import com.example.core.repository.BookingRepository
import com.example.core.repository.SlotRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFlowScreen(
    service: ServiceItem,
    savedAddresses: List<Address>,
    bookingRepository: BookingRepository,
    slotRepository: SlotRepository,
    firebaseBackend: FirebaseBackendService,
    customerId: String,
    adminHubRepository: AdminHubRepository? = null,
    customerName: String = "Valued Customer",
    customerPhone: String = "",
    onBookingSuccess: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onAddNewAddressClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(1) } // 1: Variant, 2: Slot, 3: Address, 4: Review

    var selectedVariant by remember { mutableStateOf(service.variants.firstOrNull() ?: ServiceVariant("v1", service.title, "", service.basePrice, service.durationMinutes)) }
    var quantity by remember { mutableStateOf(1) }
    val selectedAddOns = remember { mutableStateListOf<AddOnItem>() }

    val upcomingDates = remember { slotRepository.getUpcomingDates() }
    val availableSlots = remember { slotRepository.getAvailableTimeSlots() }
    var selectedDate by remember { mutableStateOf(upcomingDates.firstOrNull()?.second ?: "Today") }
    var selectedSlot by remember { mutableStateOf(availableSlots.firstOrNull() ?: "10:00 AM - 12:00 PM") }

    var selectedAddress by remember { mutableStateOf(savedAddresses.firstOrNull { it.isDefault } ?: savedAddresses.firstOrNull()) }
    var instructions by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("COD") }
    var isBookingCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isTermsAccepted by remember { mutableStateOf(false) }
    var activeLegalDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Active Hub check for the currently selected address
    val activeHub: CleankrHub? = remember(selectedAddress) {
        selectedAddress?.let { firebaseBackend.findActiveHubForAddress(it) }
    }
    val isHubServiceable = activeHub != null

    // Base, Add-ons & Total Calculations
    val basePriceTotal = selectedVariant.price * quantity
    val addOnsTotal = selectedAddOns.sumOf { it.price }
    val grandTotal = basePriceTotal + addOnsTotal

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Book ${service.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavy
                        )
                        Text(
                            text = "Step $currentStep of 4",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleankrSlate
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onNavigateBack()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CleankrCardSurface)
            )
        },
        bottomBar = {
            Surface(
                color = CleankrCardSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Amount", fontSize = 11.sp, color = CleankrSlate)
                        Text(
                            text = "₹$grandTotal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = CleankrTeal
                        )
                    }

                    Button(
                        onClick = {
                            when (currentStep) {
                                1 -> currentStep = 2
                                2 -> currentStep = 3
                                3 -> {
                                    if (selectedAddress == null) {
                                        errorMessage = "Please add or select a service address"
                                    } else if (!isHubServiceable) {
                                        errorMessage = "Selected address is outside Cleankr covered hubs (${selectedAddress?.pincode})"
                                    } else {
                                        errorMessage = null
                                        currentStep = 4
                                    }
                                }
                                4 -> {
                                    if (selectedAddress == null) {
                                        errorMessage = "Please select an address"
                                        return@Button
                                    }
                                    if (!isTermsAccepted) {
                                        errorMessage = "Please read and verify Cleankr Terms & Conditions and Privacy Policy before booking."
                                        return@Button
                                    }
                                    isBookingCreating = true
                                    coroutineScope.launch {
                                        try {
                                            val booking = bookingRepository.createBooking(
                                                customerId = customerId,
                                                serviceId = service.id,
                                                serviceTitle = service.title,
                                                categoryName = service.categoryName,
                                                variantName = selectedVariant.name,
                                                quantity = quantity,
                                                selectedAddOns = selectedAddOns.toList(),
                                                bookingDate = selectedDate,
                                                slotTime = selectedSlot,
                                                address = selectedAddress!!,
                                                instructions = instructions,
                                                paymentMethod = paymentMethod
                                            )
                                            isBookingCreating = false
                                            onBookingSuccess(booking.id)
                                        } catch (e: Exception) {
                                            isBookingCreating = false
                                            errorMessage = e.message ?: "Booking failed. Please try again."
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isBookingCreating,
                        colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("booking_flow_action_button")
                    ) {
                        if (isBookingCreating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = if (currentStep == 4) {
                                    if (isTermsAccepted) "Confirm & Book" else "Verify & Book"
                                } else "Continue",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CleankrBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                item {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CleankrError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage ?: "", color = CleankrError, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Step 1: Variant & Add-ons
            if (currentStep == 1) {
                item {
                    Text(
                        text = "1. Select Service Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                }

                items(service.variants) { variant ->
                    val isSelected = variant.id == selectedVariant.id
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CleankrTeal else CleankrBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedVariant = variant }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isSelected, onClick = { selectedVariant = variant })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(variant.name, fontWeight = FontWeight.Bold, color = CleankrNavy)
                                    if (variant.description.isNotBlank()) {
                                        Text(variant.description, style = MaterialTheme.typography.labelSmall, color = CleankrSlate)
                                    }
                                }
                            }
                            Text("₹${variant.price}", fontWeight = FontWeight.ExtraBold, color = CleankrTeal, fontSize = 16.sp)
                        }
                    }
                }

                // Recommended Add-ons
                if (service.addOns.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add-ons (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavy
                        )
                    }

                    items(service.addOns) { addOn ->
                        val isChecked = selectedAddOns.any { it.id == addOn.id }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isChecked) 1.5.dp else 1.dp,
                                    color = if (isChecked) CleankrTeal else CleankrBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (isChecked) selectedAddOns.removeAll { it.id == addOn.id }
                                    else selectedAddOns.add(addOn)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            if (isChecked) selectedAddOns.removeAll { it.id == addOn.id }
                                            else selectedAddOns.add(addOn)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(addOn.name, fontWeight = FontWeight.Bold, color = CleankrNavy)
                                        Text(addOn.description, style = MaterialTheme.typography.labelSmall, color = CleankrSlate)
                                    }
                                }
                                Text("+₹${addOn.price}", fontWeight = FontWeight.Bold, color = CleankrTeal)
                            }
                        }
                    }
                }
            }

            // Step 2: Slot Selection
            if (currentStep == 2) {
                item {
                    Text(
                        text = "2. Select Date & Slot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Date", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(upcomingDates) { (label, value) ->
                            val isSelected = selectedDate == value
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CleankrTeal else CleankrCardSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CleankrTeal else CleankrBorder),
                                modifier = Modifier.clickable { selectedDate = value }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else CleankrNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Time Slot", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }

                items(availableSlots) { slot ->
                    val isSelected = selectedSlot == slot
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CleankrTeal else CleankrBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedSlot = slot }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { selectedSlot = slot })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slot, fontWeight = FontWeight.Bold, color = CleankrNavy)
                        }
                    }
                }
            }

            // Step 3: Address & Hub Serviceability Check
            if (currentStep == 3) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Service Address",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavy
                        )
                        TextButton(onClick = onAddNewAddressClick) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CleankrTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Address", color = CleankrTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (savedAddresses.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.HomeWork, contentDescription = null, tint = CleankrSlate, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No saved addresses found", fontWeight = FontWeight.Bold, color = CleankrNavy)
                                Text("Add your address with pincode to check hub serviceability", style = MaterialTheme.typography.bodyMedium, color = CleankrSlate)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onAddNewAddressClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal)
                                ) {
                                    Text("Add New Address")
                                }
                            }
                        }
                    }
                } else {
                    items(savedAddresses) { addr ->
                        val isSelected = selectedAddress?.id == addr.id
                        val hubForAddr = firebaseBackend.findActiveHubForAddress(addr)
                        val isCovered = hubForAddr != null

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CleankrTeal else CleankrBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedAddress = addr }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = isSelected, onClick = { selectedAddress = addr })
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(addr.label, fontWeight = FontWeight.Bold, color = CleankrNavy)
                                    }
                                    if (isCovered) {
                                        Surface(
                                            color = CleankrTealLight,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Covered: ${hubForAddr?.hubName}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CleankrTeal,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Unavailable Area (${addr.pincode})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CleankrError,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${addr.flatNo}, ${addr.street}, ${addr.city} - ${addr.pincode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CleankrSlate
                                )
                                Text(
                                    text = "Phone: ${addr.contactPhone}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrSlate
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        placeholder = { Text("Special cleaning instructions (e.g. Ring doorbell, hard stains in master bathroom)...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Step 4: Review & Payment
            if (currentStep == 4) {
                item {
                    Text(
                        text = "4. Review & Doorstep Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                }

                // Booking Overview Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(service.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CleankrNavy)
                            Text("Variant: ${selectedVariant.name}", style = MaterialTheme.typography.bodyMedium, color = CleankrSlate)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$selectedDate • $selectedSlot", fontWeight = FontWeight.Bold, color = CleankrNavy)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${selectedAddress?.flatNo}, ${selectedAddress?.street} (${activeHub?.hubName})", color = CleankrSlate, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Price Breakdown Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Price Breakdown", fontWeight = FontWeight.Bold, color = CleankrNavy)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Base Price (${selectedVariant.name})", color = CleankrSlate)
                                Text("₹$basePriceTotal", fontWeight = FontWeight.Bold, color = CleankrNavy)
                            }
                            if (selectedAddOns.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                selectedAddOns.forEach { addOn ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("+ ${addOn.name}", color = CleankrSlate, fontSize = 13.sp)
                                        Text("₹${addOn.price}", color = CleankrNavy, fontSize = 13.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxes & Service Fees", color = CleankrSlate, fontSize = 13.sp)
                                Text("₹0 (Zero Tax)", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = CleankrBorder)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount Payable", fontWeight = FontWeight.Bold, color = CleankrNavy)
                                Text("₹$grandTotal", fontWeight = FontWeight.ExtraBold, color = CleankrTeal, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // Payment Options
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Payment Mode", fontWeight = FontWeight.Bold, color = CleankrNavy)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethod = "COD" }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (paymentMethod == "COD"), onClick = { paymentMethod = "COD" })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Pay After Service (Cash / UPI at Doorstep)", fontWeight = FontWeight.Bold, color = CleankrNavy)
                                    Text("Inspect service first, pay after satisfaction", style = MaterialTheme.typography.labelSmall, color = CleankrSlate)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethod = "ONLINE" }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = (paymentMethod == "ONLINE"), onClick = { paymentMethod = "ONLINE" })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Pay Online (UPI / Credit & Debit Card)", fontWeight = FontWeight.Bold, color = CleankrNavy)
                                    Text("Instant contactless confirmation", style = MaterialTheme.typography.labelSmall, color = CleankrSlate)
                                }
                            }
                        }
                    }
                }

                // Step 4 Mandatory Terms & Privacy Policy Verification
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTermsAccepted) CleankrCardSurface else Color(0xFFF0FDF4)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isTermsAccepted) CleankrTeal.copy(alpha = 0.5f) else Color(0xFF16A34A).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("booking_terms_verification_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (isTermsAccepted) Color(0xFF16A34A) else CleankrTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Terms & Privacy Policy Verification",
                                        fontWeight = FontWeight.Bold,
                                        color = CleankrNavy,
                                        fontSize = 14.sp
                                    )
                                }
                                Surface(
                                    color = if (isTermsAccepted) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isTermsAccepted) "VERIFIED ✓" else "REQUIRED",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isTermsAccepted) Color(0xFF16A34A) else Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Cleankr operates with 0% extra tax, verified Pune cleaning partners, and 4-digit doorstep safety PIN. Customers must verify and accept the policies before placing a booking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Checkbox Box
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { isTermsAccepted = !isTermsAccepted }
                                    .background(if (isTermsAccepted) CleankrTealLight.copy(alpha = 0.4f) else Color.White)
                                    .border(
                                        1.dp,
                                        if (isTermsAccepted) CleankrTeal else CleankrBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isTermsAccepted,
                                    onCheckedChange = { isTermsAccepted = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CleankrTeal,
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("terms_acceptance_checkbox")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "I have read, verified, and agree to Cleankr's Terms & Conditions and Privacy Policy.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isTermsAccepted) FontWeight.Bold else FontWeight.Normal,
                                    color = CleankrNavy,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Links to read full policies
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        activeLegalDialog = Pair("Terms & Conditions", CleankrLegalConfig.TERMS_OF_SERVICE)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("button_read_terms")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = CleankrTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Terms & Conditions", fontSize = 11.sp, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        activeLegalDialog = Pair("Privacy Policy", CleankrLegalConfig.PRIVACY_POLICY)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("button_read_privacy")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PrivacyTip,
                                        contentDescription = null,
                                        tint = CleankrTeal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Privacy Policy", fontSize = 11.sp, maxLines = 1)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    activeLegalDialog = Pair("Cancellation & Refund Policy", CleankrLegalConfig.CANCELLATION_POLICY)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_read_cancellation")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Policy,
                                    contentDescription = null,
                                    tint = CleankrTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancellation Policy (Free up to 2 hrs)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal AlertDialog for viewing and accepting policies directly
    activeLegalDialog?.let { (title, content) ->
        AlertDialog(
            onDismissRequest = { activeLegalDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (title.contains("Privacy")) Icons.Default.PrivacyTip else Icons.Default.Description,
                        contentDescription = null,
                        tint = CleankrTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, color = CleankrNavy, fontSize = 18.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    item {
                        Text(
                            text = content.trimIndent(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CleankrNavy,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isTermsAccepted = true
                        activeLegalDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dialog_accept_policy_button")
                ) {
                    Text("I Understand & Accept", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeLegalDialog = null }) {
                    Text("Close", color = CleankrSlate)
                }
            }
        )
    }
}
