package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.PropertyEntity
import com.example.ui.components.BeautifulKenyaMap
import com.example.ui.theme.KenyaGreen
import com.example.viewmodel.KenyaRentViewModel

@Composable
fun HomeScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val allProperties by viewModel.allProperties.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    var searchInput by remember { mutableStateOf("") }

    val counties = listOf("Nairobi", "Kiambu", "Mombasa", "Nakuru", "Eldoret", "Kisumu", "Kajiado", "Machakos")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Section & Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KenyaGreen)
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Habari Yako!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Find your perfect home in Kenya.",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = "App Logo Indicator",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Standard search field mimicking Instant Search (debounced filtering)
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = {
                        searchInput = it
                        viewModel.updateSearchQuery(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search estates, counties, beds...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = KenyaGreen) },
                    trailingIcon = {
                        if (searchInput.isNotEmpty()) {
                            IconButton(onClick = {
                                searchInput = ""
                                viewModel.updateSearchQuery("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear fields", tint = KenyaGreen)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFFE5A93B),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                if (searchInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Showing instant matching. Tap 'Browse' tab for advanced slider filters.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Interactive map layout call
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Explore Major Counties",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KenyaGreen
                    )
                    Text(
                        text = "Direct interactive mapping nodes filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Default.TrendingUp, contentDescription = "trend", tint = KenyaGreen)
            }

            // Map Widget Loading
            BeautifulKenyaMap(
                selectedCounty = viewModel.selectedCounty.collectAsState().value,
                onCountySelected = { county ->
                    viewModel.selectCounty(county)
                    viewModel.selectTab("Search")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // County scrolling fast filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "County Marketplaces",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                counties.take(4).forEach { county ->
                    CountyChip(
                        name = county,
                        onClick = {
                            viewModel.selectCounty(county)
                            viewModel.selectTab("Search")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                counties.drop(4).forEach { county ->
                    CountyChip(
                        name = county,
                        onClick = {
                            viewModel.selectCounty(county)
                            viewModel.selectTab("Search")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Featured Listings Carousel Card matching M3 visual benchmarks
            Text(
                text = "Featured Rental Listings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (allProperties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Refreshing vacant units from inventory...", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                allProperties.take(4).forEach { property ->
                    FeaturedPropertyRow(
                        property = property,
                        onClick = { viewModel.viewPropertyDetail(property.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CountyChip(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                color = KenyaGreen
            )
        }
    }
}

@Composable
fun FeaturedPropertyRow(
    property: PropertyEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoUrlsList = property.photosCsv.split(",").filter { it.isNotBlank() }
    val thumbUrl = photoUrlsList.firstOrNull() ?: "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Card(
                modifier = Modifier.size(88.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = thumbUrl),
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Metadata Detail
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${property.estate}, ${property.county}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KES ${property.rentAmount.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1,")}",
                        color = KenyaGreen,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Box(
                        modifier = Modifier
                            .background(KenyaGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${property.bedrooms} Bed • ${property.bathrooms} Bath",
                            color = KenyaGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
