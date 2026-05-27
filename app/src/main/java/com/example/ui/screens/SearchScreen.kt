package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import java.util.Locale

@Composable
fun SearchScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProps by viewModel.filteredProperties.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCounty by viewModel.selectedCounty.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    val bedroomsCount by viewModel.bedroomsCount.collectAsState()
    val selectedAmenities by viewModel.selectedAmenities.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val isMapView by viewModel.isMapView.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showFiltersDrawer by remember { mutableStateOf(false) }

    val counties = listOf("Nairobi", "Kiambu", "Mombasa", "Nakuru", "Eldoret", "Kisumu", "Kajiado", "Machakos")
    val types = listOf("Apartment", "Flat", "Bungalow", "Mansion", "Studio", "Bedsitter", "Townhouse", "Villa")
    val allAmenitiesList = listOf("WiFi", "Parking", "Security", "Water 24/7", "Backup Generator", "Gym", "Swimming Pool", "Balcony", "Garden", "Furnished")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Header Inputs block
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search estate, road, title...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon", tint = KenyaGreen) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showFiltersDrawer = !showFiltersDrawer },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showFiltersDrawer || selectedCounty != null || selectedType != null || maxPrice < 200000.0 || bedroomsCount != null || selectedAmenities.isNotEmpty()) {
                                KenyaGreen.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Toggle",
                            tint = KenyaGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // View Mode Toggles (Grid vs Interactive Map) & Clear Alerts Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.setMapView(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isMapView) KenyaGreen else Color.Transparent,
                                contentColor = if (!isMapView) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = "Grid", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Grid Split", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.setMapView(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMapView) KenyaGreen else Color.Transparent,
                                contentColor = if (isMapView) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Map View", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Map Pins", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (currentUser != null) {
                        TextButton(
                            onClick = { viewModel.saveCurrentSearch() },
                            colors = ButtonDefaults.textButtonColors(contentColor = KenyaGreen)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Saved Alert", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Search", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Expanded collapsible filter drawer list
        AnimatedVisibility(
            visible = showFiltersDrawer,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .padding(16.dp)
                ) {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Advanced Filtering Options", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Clear All",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.clickable { viewModel.resetFilters() }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // County Dropdown Grid
                    item {
                        Text("County Area", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = KenyaGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            counties.take(4).forEach { county ->
                                FilterChip(
                                    selected = selectedCounty == county,
                                    onClick = { viewModel.selectCounty(if (selectedCounty == county) null else county) },
                                    label = { Text(county, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            counties.drop(4).forEach { county ->
                                FilterChip(
                                    selected = selectedCounty == county,
                                    onClick = { viewModel.selectCounty(if (selectedCounty == county) null else county) },
                                    label = { Text(county, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Property Type filter
                    item {
                        Text("Property Type", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = KenyaGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                types.take(4).forEach { tp ->
                                    FilterChip(
                                        selected = selectedType == tp,
                                        onClick = { viewModel.selectPropertyType(if (selectedType == tp) null else tp) },
                                        label = { Text(tp, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                                    )
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                types.drop(4).forEach { tp ->
                                    FilterChip(
                                        selected = selectedType == tp,
                                        onClick = { viewModel.selectPropertyType(if (selectedType == tp) null else tp) },
                                        label = { Text(tp, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Price Slider Limit
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rent Cap (KES/month)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = KenyaGreen)
                            Text(
                                "KES ${maxPrice.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1,")}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KenyaGreen
                            )
                        }
                        Slider(
                            value = maxPrice.toFloat(),
                            onValueChange = { viewModel.updateMaxPrice(it.toDouble()) },
                            valueRange = 0f..200000f,
                            colors = SliderDefaults.colors(
                                thumbColor = KenyaGreen,
                                activeTrackColor = KenyaGreen
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Bedrooms Count Filter
                    item {
                        Text("Bedrooms required", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = KenyaGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Any", "1", "2", "3", "4", "5+").forEachIndexed { index, tag ->
                                val filterVal = if (index == 0) null else if (index == 5) 5 else index
                                val isSelected = (bedroomsCount == null && index == 0) || (bedroomsCount == filterVal && index != 0)

                                OutlinedButton(
                                    onClick = { viewModel.selectBedrooms(filterVal) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) KenyaGreen else Color.Transparent,
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(tag, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Sorting order dropdown multi-choose
                    item {
                        Text("Sort Results By", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = KenyaGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Newest", "Price (Low to High)", "Price (High to Low)", "Most Popular").forEach { order ->
                                FilterChip(
                                    selected = sortBy == order,
                                    onClick = { viewModel.setSortBy(order) },
                                    label = { Text(order, fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Amenities check list
                    item {
                        Text("Amenities included", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = KenyaGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column {
                            allAmenitiesList.chunked(2).forEach { pair ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    pair.forEach { am ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.toggleAmenity(am) }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Checkbox(
                                                checked = selectedAmenities.contains(am),
                                                onCheckedChange = { viewModel.toggleAmenity(am) },
                                                colors = CheckboxDefaults.colors(checkedColor = KenyaGreen)
                                            )
                                            Text(am, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results listing layer
        Box(modifier = Modifier.weight(1f)) {
            if (isMapView) {
                // Interactive map coordinates view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Map Coordinate Filtering",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KenyaGreen
                    )
                    Text(
                        text = "Focusing searches on active location of Pins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    BeautifulKenyaMap(
                        selectedCounty = selectedCounty,
                        onCountySelected = { county ->
                            viewModel.selectCounty(if (selectedCounty == county) null else county)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                if (filteredProps.isEmpty()) {
                    // Empty search feedbacks
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = "No listings", modifier = Modifier.size(54.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No matching listings found.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Try clearing filters, searching different estates, or widening your KES price Cap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.resetFilters() },
                            colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen)
                        ) {
                            Text("Clear All Filters")
                        }
                    }
                } else {
                    // Beautiful card list grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProps) { prop ->
                            GridPropertyCard(
                                property = prop,
                                onClick = { viewModel.viewPropertyDetail(prop.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridPropertyCard(
    property: PropertyEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageList = property.photosCsv.split(",").filter { it.isNotBlank() }
    val imageUrl = imageList.firstOrNull() ?: "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Price
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${property.rentAmount.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1,")} KES",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status Tag
                if (property.isTaken) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("TAKEN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (property.isNegotiable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(Color(0xFFE5A93B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("NEGOTIABLE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "loc",
                        tint = Color.Gray,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${property.estate}, ${property.county}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${property.bedrooms} Bed • ${property.bathrooms} Bath",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Detail",
                        tint = KenyaGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
