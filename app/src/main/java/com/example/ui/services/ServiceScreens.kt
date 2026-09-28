package com.example.ui.services

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.core.model.ServiceCategory
import com.example.core.model.ServiceItem
import com.example.ui.theme.*

@Composable
fun ServiceCatalogScreen(
    categories: List<ServiceCategory>,
    services: List<ServiceItem>,
    selectedCategoryId: String,
    onSelectCategory: (String) -> Unit,
    onServiceClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredServices = remember(services, selectedCategoryId, searchQuery) {
        services.filter { item ->
            val matchesCategory = selectedCategoryId == "cat_all" || selectedCategoryId.isBlank() || item.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search bathroom, kitchen, full home...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CleankrSlate)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("catalog_search_bar"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CleankrCardSurface,
                unfocusedContainerColor = CleankrCardSurface
            )
        )

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat.id == selectedCategoryId
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(cat.id) },
                    label = { Text(cat.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CleankrTeal,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_${cat.id}")
                )
            }
        }

        // Service List
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredServices) { service ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onServiceClick(service.id) }
                        .testTag("catalog_service_item_${service.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CleankrNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = service.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CleankrSlate,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CleankrTealLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (service.categoryId) {
                                        "cat_bathroom" -> Icons.Default.Bathtub
                                        "cat_kitchen" -> Icons.Default.Kitchen
                                        "cat_fullhome" -> Icons.Default.Home
                                        else -> Icons.Default.CleaningServices
                                    },
                                    contentDescription = service.title,
                                    tint = CleankrTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = CleankrBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Admin Verified Price",
                                    fontSize = 10.sp,
                                    color = CleankrSlate
                                )
                                Text(
                                    text = "₹${service.basePrice}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CleankrTeal
                                )
                            }
                            Button(
                                onClick = { onServiceClick(service.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("View Options", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceDetailScreen(
    service: ServiceItem,
    onBookNow: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CleankrBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            // Header Card
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CleankrTealLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = service.categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleankrTeal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = CleankrAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${service.rating} (${service.reviewCount} reviews)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = CleankrNavy
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleankrSlate
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${service.durationMinutes} mins approx", style = MaterialTheme.typography.bodyMedium, color = CleankrNavy)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CleankrTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Doorstep PIN", style = MaterialTheme.typography.bodyMedium, color = CleankrNavy)
                        }
                    }
                }
            }
        }

        // Available Variants & Configuration
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configurations & Pricing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleankrNavy
                )
                Spacer(modifier = Modifier.height(10.dp))
                service.variants.forEach { variant ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(variant.name, fontWeight = FontWeight.Bold, color = CleankrNavy)
                                Text(variant.description, style = MaterialTheme.typography.labelSmall, color = CleankrSlate)
                            }
                            Text(
                                text = "₹${variant.price}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = CleankrTeal
                            )
                        }
                    }
                }
            }
        }

        // What's Included & Not Included
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CleankrCardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "What is Included",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CleankrNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    service.includedItems.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CleankrSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item, style = MaterialTheme.typography.bodyMedium, color = CleankrNavy)
                        }
                    }

                    if (service.notIncludedItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "What is NOT Included",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleankrNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        service.notIncludedItems.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = CleankrError, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item, style = MaterialTheme.typography.bodyMedium, color = CleankrSlate)
                            }
                        }
                    }
                }
            }
        }
    }

    // Sticky Bottom Bar
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
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
                    Text("Starts from", fontSize = 11.sp, color = CleankrSlate)
                    Text(
                        text = "₹${service.basePrice}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CleankrTeal
                    )
                }
                Button(
                    onClick = onBookNow,
                    colors = ButtonDefaults.buttonColors(containerColor = CleankrTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("book_now_bottom_button")
                ) {
                    Text("Book Service", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
