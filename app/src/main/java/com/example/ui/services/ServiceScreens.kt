package com.example.ui.services

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AddOnItem
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.core.model.ServiceVariant
import com.example.core.repository.ServiceRepository
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
import com.example.ui.theme.CleankrRed
import com.example.ui.theme.CleankrRedLight
import com.example.ui.theme.CleankrSlate
import com.example.ui.theme.CleankrTeal
import com.example.ui.theme.CleankrTealContainer
import com.example.ui.theme.CleankrTealDark

@Composable
fun ServiceListScreen(
    category: ServiceCategory,
    serviceRepository: ServiceRepository,
    onServiceClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val services by serviceRepository.getServicesByCategory(category).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(
            title = "${category.displayName} Services",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = CleankrTealContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CleankrTealDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Transparent fixed company pricing. Includes professional equipment and eco-safe descalers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrTealDark
                        )
                    }
                }
            }

            items(services) { service ->
                ServiceCatalogCard(
                    service = service,
                    onClick = { onServiceClick(service.id) }
                )
            }
        }
    }
}

@Composable
fun ServiceCatalogCard(
    service: ServiceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("catalog_card_${service.id}"),
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
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = CleankrOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${service.rating} (${service.reviewsCount})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavyDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = service.shortDesc,
                style = MaterialTheme.typography.bodySmall,
                color = CleankrMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Duration & Price breakdown pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = CleankrTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = service.durationText,
                    style = MaterialTheme.typography.labelSmall,
                    color = CleankrSlate
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "• ${service.variants.size} Options Available",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleankrTealDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = CleankrBorder
            )

            // Price list sample
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Company Fixed Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleankrMuted
                    )
                    Text(
                        text = "Starts ₹${service.basePrice}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleankrOrange
                    )
                }

                Surface(
                    color = CleankrOrange,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View Details",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceDetailScreen(
    serviceId: String,
    serviceRepository: ServiceRepository,
    onBookNow: (serviceId: String) -> Unit,
    onBackClick: () -> Unit
) {
    val service = serviceRepository.getServiceById(serviceId)

    if (service == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleankrBackground)
        ) {
            CleankrStandardTopBar(title = "Service Details", onBackClick = onBackClick)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Service not found.", color = CleankrMuted)
            }
        }
        return
    }

    var selectedVariant by remember { mutableStateOf(service.variants.first()) }
    val selectedAddOns = remember { mutableStateListOf<AddOnItem>() }
    var quantity by remember { mutableIntStateOf(1) }

    val calculatedTotal = remember(selectedVariant, quantity, selectedAddOns.toList()) {
        serviceRepository.calculateTotal(service, selectedVariant, quantity, selectedAddOns)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        CleankrStandardTopBar(
            title = service.title,
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        color = CleankrTealContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = service.category.displayName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleankrTealDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavyDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CleankrOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${service.rating} (${service.reviewsCount} customer reviews)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavyDark
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "⏱ ${service.durationText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleankrSlate
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = service.fullDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleankrSlate,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Select Variant (Fixed company pricing)
            Text(
                text = "1. Select Package / Size",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CleankrNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    service.variants.forEach { variant ->
                        val isSelected = (selectedVariant.id == variant.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CleankrTealContainer.copy(alpha = 0.4f) else Color.Transparent)
                                .clickable { selectedVariant = variant }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedVariant = variant },
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
                                        text = "Duration: ~${variant.durationText}",
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

            // Add-ons Section
            if (service.addOns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "2. Optional Add-ons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavyDark
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                                    onCheckedChange = { checked ->
                                        if (checked) selectedAddOns.add(addon)
                                        else selectedAddOns.removeAll { it.id == addon.id }
                                    },
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

            Spacer(modifier = Modifier.height(16.dp))

            // What's Included & Not Included
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "What's Included",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    service.included.forEach { inc ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CleankrGreen,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = inc,
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrNavyDark
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CleankrBorder)

                    Text(
                        text = "What's NOT Included",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    service.notIncluded.forEach { notInc ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = CleankrRed,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = notInc,
                                style = MaterialTheme.typography.bodySmall,
                                color = CleankrSlate
                            )
                        }
                    }
                }
            }
        }

        // Bottom Fixed Booking Bar
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
                        text = "Total Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleankrMuted
                    )
                    Text(
                        text = "₹$calculatedTotal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleankrNavyDark
                    )
                    Text(
                        text = "Includes all equipment & taxes",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleankrTealDark,
                        fontSize = 10.sp
                    )
                }

                CleankrButton(
                    text = "Continue to Booking",
                    onClick = { onBookNow(service.id) },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}
