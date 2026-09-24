package com.example.ui.booking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AddOnItem
import com.example.core.model.Address
import com.example.core.model.Booking
import com.example.core.model.PaymentMethod
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import com.example.core.model.TimeSlot
import com.example.core.repository.AddressRepository
import com.example.core.repository.BookingRepository
import com.example.core.repository.CalendarDay
import com.example.core.repository.ServiceRepository
import com.example.core.repository.SlotRepository
import com.example.ui.components.CleankrButton
import com.example.ui.components.CleankrStandardTopBar
import com.example.ui.components.ErrorBanner
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BookingFlowScreen(
    serviceId: String,
    serviceRepository: ServiceRepository,
    slotRepository: SlotRepository,
    addressRepository: AddressRepository,
    bookingRepository: BookingRepository,
    onBookingCompleted: (bookingId: String) -> Unit,
    onBackClick: () -> Unit
) {
    val service = serviceRepository.getServiceById(serviceId)
    if (service == null) {
        onBackClick()
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val addresses by addressRepository.addresses.collectAsState(initial = emptyList())
    val defaultAddress by addressRepository.defaultAddress.collectAsState(initial = null)

    // Booking Wizard Steps:
    // 0: Variant & Addons
    // 1: Date & Time Slot
    // 2: Service Address & Instructions
    // 3: Review & Payment
    var currentStep by remember { mutableIntStateOf(0) }

    // Selections
    var selectedVariant by remember { mutableStateOf(service.variants.first()) }
    val selectedAddOns = remember { mutableStateListOf<AddOnItem>() }
    var quantity by remember { mutableIntStateOf(1) }

    val upcomingDays = remember { slotRepository.getUpcomingDays(14) }
    var selectedDay by remember { mutableStateOf(upcomingDays.firstOrNull { it.isAvailable } ?: upcomingDays.first()) }

    var timeSlots by remember { mutableStateOf(slotRepository.getTimeSlotsForDate(selectedDay.dateString)) }
    var selectedSlot by remember { mutableStateOf(timeSlots.firstOrNull { it.isAvailable } ?: timeSlots.first()) }

    var selectedAddress by remember { mutableStateOf<Address?>(defaultAddress ?: addresses.firstOrNull()) }
    var customerInstructions by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.ONLINE) }

    var isCreatingBooking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Quick address form dialog state
    var showAddAddressDialog by remember { mutableStateOf(false) }

    // Keep selectedAddress updated when defaultAddress loads
    LaunchedEffect(defaultAddress, addresses) {
        if (selectedAddress == null) {
            selectedAddress = defaultAddress ?: addresses.firstOrNull()
        }
    }

    // Refresh time slots when date changes
    LaunchedEffect(selectedDay) {
        timeSlots = slotRepository.getTimeSlotsForDate(selectedDay.dateString)
        val valid = timeSlots.firstOrNull { it.isAvailable }
        if (valid != null) {
            selectedSlot = valid
        }
    }

    // Server-side calculated totals
    val addOnsTotal = remember(selectedAddOns.toList()) { selectedAddOns.sumOf { it.price } }
    val servicePrice = remember(selectedVariant, quantity) { selectedVariant.price * quantity }
    val totalAmount = remember(servicePrice, addOnsTotal) { servicePrice + addOnsTotal }

    val stepTitles = listOf("Package & Add-ons", "Date & Time Slot", "Address & Notes", "Review & Confirm")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(
            title = stepTitles[currentStep],
            onBackClick = {
                if (currentStep > 0) currentStep -= 1
                else onBackClick()
            }
        )

        // Step Progress Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepTitles.forEachIndexed { index, title ->
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> CleankrGreen
                                    isCurrent -> CleankrOrange
                                    else -> CleankrBorder
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else CleankrMuted
                            )
                        }
                    }
                    if (index < stepTitles.size - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(2.dp)
                                .background(if (index < currentStep) CleankrGreen else CleankrBorder)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }

        if (errorMessage != null) {
            Box(modifier = Modifier.padding(16.dp)) {
                ErrorBanner(errorMessage = errorMessage!!, onRetry = { errorMessage = null })
            }
        }

        // Animated Step Contents
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                0 -> StepVariantAndAddons(
                    service = service,
                    selectedVariant = selectedVariant,
                    onVariantChange = { selectedVariant = it },
                    selectedAddOns = selectedAddOns,
                    onToggleAddOn = { addon ->
                        if (selectedAddOns.any { it.id == addon.id }) selectedAddOns.removeAll { it.id == addon.id }
                        else selectedAddOns.add(addon)
                    }
                )
                1 -> StepDateAndTimeSlot(
                    upcomingDays = upcomingDays,
                    selectedDay = selectedDay,
                    onSelectDay = { selectedDay = it },
                    timeSlots = timeSlots,
                    selectedSlot = selectedSlot,
                    onSelectSlot = { selectedSlot = it }
                )
                2 -> StepAddressAndNotes(
                    addresses = addresses,
                    selectedAddress = selectedAddress,
                    onSelectAddress = { selectedAddress = it },
                    instructions = customerInstructions,
                    onInstructionsChange = { customerInstructions = it },
                    onAddNewAddressClick = { showAddAddressDialog = true }
                )
                3 -> StepReviewAndPayment(
                    service = service,
                    selectedVariant = selectedVariant,
                    quantity = quantity,
                    selectedAddOns = selectedAddOns,
                    dateString = selectedDay.dateString,
                    slotTime = selectedSlot.timeDisplay,
                    address = selectedAddress,
                    instructions = customerInstructions,
                    paymentMethod = paymentMethod,
                    onPaymentMethodChange = { paymentMethod = it },
                    servicePrice = servicePrice,
                    addOnsTotal = addOnsTotal,
                    totalAmount = totalAmount
                )
            }
        }

        // Bottom Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Payable",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleankrMuted
                    )
                    Text(
                        text = "₹$totalAmount",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleankrNavyDark
                    )
                }

                if (currentStep < 3) {
                    CleankrButton(
                        text = "Next Step",
                        onClick = {
                            if (currentStep == 1 && !selectedSlot.isAvailable) {
                                errorMessage = "Please select an available time slot."
                            } else if (currentStep == 2 && selectedAddress == null) {
                                errorMessage = "Please select or add a service address."
                            } else {
                                errorMessage = null
                                currentStep += 1
                            }
                        },
                        modifier = Modifier.width(180.dp),
                        leadingIcon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                } else {
                    CleankrButton(
                        text = if (paymentMethod == PaymentMethod.ONLINE) "Pay ₹$totalAmount" else "Confirm Booking",
                        onClick = {
                            if (selectedAddress == null) {
                                errorMessage = "Please select a service address."
                                return@CleankrButton
                            }
                            coroutineScope.launch {
                                isCreatingBooking = true
                                errorMessage = null
                                delay(1200) // Simulate secure payment gateway & order booking handshake

                                val result = bookingRepository.createBooking(
                                    service = service,
                                    variant = selectedVariant,
                                    quantity = quantity,
                                    selectedAddOnNames = selectedAddOns.map { it.name },
                                    dateString = selectedDay.dateString,
                                    slotTime = selectedSlot.timeDisplay,
                                    address = selectedAddress!!,
                                    instructions = customerInstructions,
                                    paymentMethod = paymentMethod
                                )

                                isCreatingBooking = false
                                result.fold(
                                    onSuccess = { booking ->
                                        onBookingCompleted(booking.id)
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.message ?: "Failed to book service. Please retry."
                                    }
                                )
                            }
                        },
                        isLoading = isCreatingBooking,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
        }
    }

    // Add Address Dialog
    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddAddressDialog = false },
            onSave = { newAddr ->
                coroutineScope.launch {
                    val saved = addressRepository.addAddress(newAddr)
                    selectedAddress = saved
                    showAddAddressDialog = false
                }
            }
        )
    }
}

@Composable
fun StepVariantAndAddons(
    service: ServiceItem,
    selectedVariant: ServiceVariant,
    onVariantChange: (ServiceVariant) -> Unit,
    selectedAddOns: List<AddOnItem>,
    onToggleAddOn: (AddOnItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Select Service Package",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CleankrNavyDark
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                service.variants.forEach { variant ->
                    val isSelected = selectedVariant.id == variant.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CleankrTealContainer.copy(alpha = 0.4f) else Color.Transparent)
                            .clickable { onVariantChange(variant) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onVariantChange(variant) },
                                colors = RadioButtonDefaults.colors(selectedColor = CleankrTeal)
                            )
                            Column {
                                Text(
                                    text = variant.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavyDark
                                )
                                Text(
                                    text = "~${variant.durationText}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrMuted
                                )
                            }
                        }
                        Text(
                            text = "₹${variant.price}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CleankrNavyDark
                        )
                    }
                }
            }
        }

        if (service.addOns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Add Specialized Enhancements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    service.addOns.forEach { addon ->
                        val isChecked = selectedAddOns.any { it.id == addon.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { onToggleAddOn(addon) },
                                colors = CheckboxDefaults.colors(checkedColor = CleankrOrange)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = addon.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CleankrNavyDark
                                )
                                Text(
                                    text = addon.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleankrMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "+₹${addon.price}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleankrOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepDateAndTimeSlot(
    upcomingDays: List<CalendarDay>,
    selectedDay: CalendarDay,
    onSelectDay: (CalendarDay) -> Unit,
    timeSlots: List<TimeSlot>,
    selectedSlot: TimeSlot,
    onSelectSlot: (TimeSlot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = CleankrTeal,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Select Service Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Calendar Picker
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(upcomingDays) { day ->
                val isSelected = (day.dateString == selectedDay.dateString)
                val isBooked = day.isFullyBooked

                Card(
                    modifier = Modifier
                        .width(70.dp)
                        .clickable(enabled = day.isAvailable) { onSelectDay(day) }
                        .testTag("date_picker_${day.dateString}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> CleankrTeal
                            isBooked -> CleankrBorder.copy(alpha = 0.5f)
                            else -> Color.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayOfWeekText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> Color.White
                                isBooked -> CleankrMuted
                                else -> CleankrSlate
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayOfMonthText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isSelected -> Color.White
                                isBooked -> CleankrMuted
                                else -> CleankrNavyDark
                            }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBooked) "Full" else day.monthText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isSelected -> CleankrTealContainer
                                isBooked -> CleankrRed
                                else -> CleankrMuted
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = CleankrOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Select Time Slot",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Cleaners will arrive within the selected 2-hour window.",
            style = MaterialTheme.typography.bodySmall,
            color = CleankrMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            timeSlots.forEach { slot ->
                val isSelected = (slot.id == selectedSlot.id)
                val isAvailable = slot.isAvailable

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isAvailable) { onSelectSlot(slot) }
                        .testTag("time_slot_${slot.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> CleankrOrangeContainer
                            !isAvailable -> CleankrBorder.copy(alpha = 0.3f)
                            else -> Color.White
                        }
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CleankrOrange)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { if (isAvailable) onSelectSlot(slot) },
                                enabled = isAvailable,
                                colors = RadioButtonDefaults.colors(selectedColor = CleankrOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = slot.timeDisplay,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAvailable) CleankrNavyDark else CleankrMuted
                                )
                                Text(
                                    text = slot.period,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrMuted
                                )
                            }
                        }

                        if (!isAvailable) {
                            Surface(
                                color = CleankrRedLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Slot Passed / Unavailable",
                                    fontSize = 10.sp,
                                    color = CleankrRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CleankrOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepAddressAndNotes(
    addresses: List<Address>,
    selectedAddress: Address?,
    onSelectAddress: (Address) -> Unit,
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onAddNewAddressClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Service Address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            TextButton(
                onClick = onAddNewAddressClick,
                modifier = Modifier.testTag("add_new_address_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add New", fontWeight = FontWeight.Bold, color = CleankrTeal)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (addresses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No saved addresses yet.", color = CleankrMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    CleankrButton(
                        text = "Add Service Address",
                        onClick = onAddNewAddressClick,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                addresses.forEach { addr ->
                    val isSelected = selectedAddress?.id == addr.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAddress(addr) }
                            .testTag("address_item_${addr.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CleankrTeal)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectAddress(addr) },
                                colors = RadioButtonDefaults.colors(selectedColor = CleankrTeal)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = CleankrTealContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = addr.label.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CleankrTealDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (addr.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Default",
                                            fontSize = 10.sp,
                                            color = CleankrMuted
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = addr.flatNo,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavyDark
                                )
                                Text(
                                    text = "${addr.street}, ${addr.landmark}, ${addr.city} - ${addr.pincode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleankrMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Special Instructions for Partner",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CleankrNavyDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "e.g., 'Pet in the house', 'Call on reaching the gate', 'Key with neighbor'",
            style = MaterialTheme.typography.bodySmall,
            color = CleankrMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = instructions,
            onValueChange = onInstructionsChange,
            placeholder = { Text("Add instructions here (optional)...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("booking_instructions_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = CleankrTeal,
                unfocusedBorderColor = CleankrBorder
            )
        )
    }
}

@Composable
fun StepReviewAndPayment(
    service: ServiceItem,
    selectedVariant: ServiceVariant,
    quantity: Int,
    selectedAddOns: List<AddOnItem>,
    dateString: String,
    slotTime: String,
    address: Address?,
    instructions: String,
    paymentMethod: PaymentMethod,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    servicePrice: Int,
    addOnsTotal: Int,
    totalAmount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Booking Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CleankrTealDark
                )
                Text(
                    text = "Package: ${selectedVariant.name} (Qty: $quantity)",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleankrSlate
                )

                if (selectedAddOns.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add-ons: " + selectedAddOns.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = CleankrOrange
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CleankrBorder)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Date: $dateString", style = MaterialTheme.typography.bodySmall, color = CleankrNavyDark, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = CleankrOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Slot: $slotTime", style = MaterialTheme.typography.bodySmall, color = CleankrNavyDark, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = CleankrGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = address?.fullAddressText ?: "No address selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleankrSlate
                    )
                }

                if (instructions.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instructions: \"$instructions\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleankrMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price Breakdown Card (Official Company Fixed Breakdown)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Price Breakdown (Fixed Company Pricing)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${selectedVariant.name} x $quantity", style = MaterialTheme.typography.bodySmall, color = CleankrSlate)
                    Text(text = "₹$servicePrice", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = CleankrNavyDark)
                }

                if (addOnsTotal > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Add-ons Total", style = MaterialTheme.typography.bodySmall, color = CleankrSlate)
                        Text(text = "+₹$addOnsTotal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = CleankrOrange)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Safety & Equipment Kit", style = MaterialTheme.typography.bodySmall, color = CleankrGreen)
                    Text(text = "FREE", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CleankrGreen)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CleankrBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Final Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                    Text(text = "₹$totalAmount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CleankrOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payment Method Selection
        Text(
            text = "Select Payment Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CleankrNavyDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Online Payment
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPaymentMethodChange(PaymentMethod.ONLINE) }
                    .testTag("payment_method_online"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = if (paymentMethod == PaymentMethod.ONLINE) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CleankrTeal)) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = paymentMethod == PaymentMethod.ONLINE,
                        onClick = { onPaymentMethodChange(PaymentMethod.ONLINE) },
                        colors = RadioButtonDefaults.colors(selectedColor = CleankrTeal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = CleankrTeal)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Online Payment (UPI, Cards, NetBanking)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Text(text = "Instant refund eligible on cancellation", style = MaterialTheme.typography.labelSmall, color = CleankrGreen)
                    }
                }
            }

            // Cash on Service
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPaymentMethodChange(PaymentMethod.CASH) }
                    .testTag("payment_method_cash"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = if (paymentMethod == PaymentMethod.CASH) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CleankrTeal)) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = paymentMethod == PaymentMethod.CASH,
                        onClick = { onPaymentMethodChange(PaymentMethod.CASH) },
                        colors = RadioButtonDefaults.colors(selectedColor = CleankrTeal)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Money, contentDescription = null, tint = CleankrOrange)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Cash on Service", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CleankrNavyDark)
                        Text(text = "Pay directly to partner after service completion", style = MaterialTheme.typography.labelSmall, color = CleankrMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    var label by remember { mutableStateOf("Home") }
    var flatNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bengaluru") }
    var pincode by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Add New Service Address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavyDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Address Type Pill
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Home", "Work", "Other").forEach { type ->
                            Surface(
                                color = if (label == type) CleankrTealContainer else CleankrBackground,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable { label = type }
                                    .border(1.dp, if (label == type) CleankrTeal else CleankrBorder, RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (label == type) CleankrTealDark else CleankrSlate,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = flatNo,
                        onValueChange = { flatNo = it },
                        label = { Text("Flat / House / Building No.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_address_flat"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street / Society / Area") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_address_street"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark (e.g. Near Metro Station)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pincode = it },
                            label = { Text("Pincode") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it },
                            colors = CheckboxDefaults.colors(checkedColor = CleankrTeal)
                        )
                        Text(
                            text = "Set as default service address",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = CleankrMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        CleankrButton(
                            text = "Save Address",
                            onClick = {
                                if (flatNo.isNotBlank() && street.isNotBlank() && pincode.length == 6) {
                                    onSave(
                                        Address(
                                            id = "",
                                            label = label,
                                            flatNo = flatNo,
                                            street = street,
                                            landmark = landmark,
                                            city = city,
                                            pincode = pincode,
                                            isDefault = isDefault
                                        )
                                    )
                                }
                            },
                            enabled = flatNo.isNotBlank() && street.isNotBlank() && pincode.length == 6,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }
        }
    }
}
