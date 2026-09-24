package com.example.ui.address

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Address
import com.example.core.repository.AddressRepository
import com.example.ui.booking.AddAddressDialog
import com.example.ui.components.CleankrStandardTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.CleankrBackground
import com.example.ui.theme.CleankrBorder
import com.example.ui.theme.CleankrGreen
import com.example.ui.theme.CleankrMuted
import com.example.ui.theme.CleankrNavyDark
import com.example.ui.theme.CleankrOrange
import com.example.ui.theme.CleankrRed
import com.example.ui.theme.CleankrSlate
import com.example.ui.theme.CleankrTeal
import com.example.ui.theme.CleankrTealContainer
import com.example.ui.theme.CleankrTealDark
import kotlinx.coroutines.launch

@Composable
fun AddressManagementScreen(
    addressRepository: AddressRepository,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val addresses by addressRepository.addresses.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(
            title = "Saved Addresses",
            onBackClick = onBackClick,
            actions = {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_address_topbar_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Address", tint = CleankrOrange)
                }
            }
        )

        if (addresses.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.LocationOn,
                title = "No Saved Addresses",
                subtitle = "Add your home or office address for faster booking.",
                actionButtonText = "Add Address",
                onActionClick = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(addresses) { addr ->
                    AddressItemCard(
                        address = addr,
                        onSetDefault = {
                            coroutineScope.launch { addressRepository.setDefault(addr.id) }
                        },
                        onDelete = {
                            coroutineScope.launch { addressRepository.deleteAddress(addr.id) }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddAddressDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newAddr ->
                coroutineScope.launch {
                    addressRepository.addAddress(newAddr)
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun AddressItemCard(
    address: Address,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("address_card_${address.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CleankrTealContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = address.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleankrTealDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (address.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CleankrGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Default", style = MaterialTheme.typography.labelSmall, color = CleankrGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CleankrRed, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = address.flatNo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Text(
                text = "${address.street}, ${address.landmark}, ${address.city} - ${address.pincode}",
                style = MaterialTheme.typography.bodySmall,
                color = CleankrSlate
            )

            if (!address.isDefault) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CleankrBorder)
                TextButton(
                    onClick = onSetDefault,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Set as Default Address", color = CleankrTeal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
