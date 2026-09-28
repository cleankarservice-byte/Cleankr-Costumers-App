package com.example.ui.address

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.core.data.firebase.FirebaseBackendService
import com.example.core.model.Address
import com.example.core.repository.AddressRepository
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun AddressManagementScreen(
    addresses: List<Address>,
    addressRepository: AddressRepository,
    backendService: FirebaseBackendService
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddEditAddressDialog(
            backendService = backendService,
            onDismiss = { showAddDialog = false },
            onSave = { newAddr ->
                coroutineScope.launch {
                    addressRepository.saveAddress(newAddr)
                    showAddDialog = false
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CleankrTeal,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_address_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Address")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CleankrBackground)
        ) {
            if (addresses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.HomeWork, contentDescription = null, tint = CleankrSlate, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No Saved Addresses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CleankrNavy)
                        Text("Add your service addresses to check Cleankr active hub coverage", style = MaterialTheme.typography.bodyMedium, color = CleankrSlate)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal)
                        ) {
                            Text("Add Address with GPS")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(addresses) { addr ->
                        val hub = backendService.findActiveHubForAddress(addr)
                        val isCovered = hub != null

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("address_card_${addr.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (addr.label) {
                                                "Home" -> Icons.Default.Home
                                                "Office" -> Icons.Default.Work
                                                else -> Icons.Default.LocationOn
                                            },
                                            contentDescription = null,
                                            tint = CleankrTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(addr.label, fontWeight = FontWeight.Bold, color = CleankrNavy)
                                        if (addr.isDefault) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = CleankrTealLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "DEFAULT",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CleankrTeal,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        if (!addr.isDefault) {
                                            TextButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        addressRepository.setDefaultAddress(addr.id)
                                                    }
                                                }
                                            ) {
                                                Text("Make Default", fontSize = 11.sp, color = CleankrTeal)
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    addressRepository.deleteAddress(addr.id)
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CleankrError)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${addr.flatNo}, ${addr.street}, ${addr.landmark.ifBlank { "" }} ${addr.city} - ${addr.pincode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CleankrNavy
                                )
                                Text(
                                    text = "Phone: ${addr.contactPhone}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleankrSlate
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = CleankrBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCovered) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CleankrSuccess, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Active Hub: ${hub?.hubName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CleankrSuccess
                                        )
                                    } else {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CleankrError, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Outside Service Area (${addr.pincode})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CleankrError
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
}

@Composable
fun AddEditAddressDialog(
    backendService: FirebaseBackendService,
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var label by remember { mutableStateOf("Home") }
    var flatNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bengaluru") }
    var pincode by remember { mutableStateOf("560095") }
    var contactPhone by remember { mutableStateOf("+91 98765 43210") }
    var isDefault by remember { mutableStateOf(true) }
    var isDetectingLocation by remember { mutableStateOf(false) }

    // Feature 9: GPS Auto-Detect Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isDetectingLocation = true
            coroutineScope.launch {
                try {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            try {
                                val geocoder = Geocoder(context, Locale.ENGLISH)
                                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    addr.postalCode?.let { pincode = it }
                                    addr.locality?.let { city = it }
                                    addr.thoroughfare?.let { street = it }
                                    addr.subLocality?.let { landmark = it }
                                }
                            } catch (_: Exception) {}
                        }
                        isDetectingLocation = false
                    }.addOnFailureListener {
                        isDetectingLocation = false
                    }
                } catch (_: Exception) {
                    isDetectingLocation = false
                }
            }
        }
    }

    val activeHub = backendService.findActiveHubForPincode(pincode)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Service Address", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // GPS Button (Feature 9)
                item {
                    Button(
                        onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CleankrTealLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("use_gps_location_button")
                    ) {
                        if (isDetectingLocation) {
                            CircularProgressIndicator(color = CleankrTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detecting GPS...", color = CleankrTeal, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = CleankrTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Current Location (GPS)", color = CleankrTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Label selector
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Home", "Office", "Other").forEach { l ->
                            FilterChip(
                                selected = (label == l),
                                onClick = { label = l },
                                label = { Text(l) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = flatNo,
                        onValueChange = { flatNo = it },
                        placeholder = { Text("House / Flat No / Floor *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        placeholder = { Text("Street / Apartment / Society *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        placeholder = { Text("Landmark (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            placeholder = { Text("City") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { if (it.length <= 6) pincode = it },
                            placeholder = { Text("Pincode *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("address_pincode_field"),
                            singleLine = true
                        )
                    }
                }

                // Active Hub Feedback
                item {
                    if (activeHub != null) {
                        Surface(
                            color = CleankrTealLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ Serviced by ${activeHub.hubName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CleankrTeal,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠ Pincode outside active hubs (Try 560034, 560095, 560001, 560038)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CleankrError,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        placeholder = { Text("Contact Phone *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Address(
                            label = label,
                            flatNo = flatNo.ifBlank { "Flat 204" },
                            street = street.ifBlank { "80 Feet Road" },
                            landmark = landmark,
                            city = city,
                            pincode = pincode,
                            contactPhone = contactPhone,
                            isDefault = isDefault
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                modifier = Modifier.testTag("save_address_button")
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
