package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PropertyEntity
import com.example.ui.theme.KenyaGreen
import com.example.viewmodel.KenyaRentViewModel

@Composable
fun LandlordDashboardScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val landlordProperties by viewModel.landlordProperties.collectAsState()
    val landlordNotifications by viewModel.landlordNotifications.collectAsState()
    val platformAnalyticsInquiries by viewModel.platformInquiries.collectAsState()

    val scrollState = rememberScrollState()

    var showCreateForm by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }

    // Multi-criteria form bindings
    var fTitle by remember { mutableStateOf("") }
    var fType by remember { mutableStateOf("Apartment") }
    var fCounty by remember { mutableStateOf("Nairobi") }
    var fEstate by remember { mutableStateOf("") }
    var fPrice by remember { mutableStateOf("") }
    var fIsNegotiable by remember { mutableStateOf(false) }
    var fBedrooms by remember { mutableStateOf("2") }
    var fBathrooms by remember { mutableStateOf("2") }
    var fSize by remember { mutableStateOf("") }
    var fIsPetFriendly by remember { mutableStateOf(true) }
    var fAvailableDate by remember { mutableStateOf("2026-06-01") }
    
    // Testing Coordinates
    var fLatitude by remember { mutableStateOf(-1.2921) }
    var fLongitude by remember { mutableStateOf(36.8219) }
    var fResolvedAddress by remember { mutableStateOf("Nairobi CBD, Kenya") }

    // Simulation / testing variables
    var useSpoofedExif by remember { mutableStateOf(false) }
    var useDuplicatePhoto by remember { mutableStateOf(false) }
    var useMismatchedBuilding by remember { mutableStateOf(false) }
    var triggerRuralOfflineReview by remember { mutableStateOf(false) }

    val selectedAmenSets = remember { mutableStateOf(setOf("WiFi", "Parking", "Security", "Water 24/7")) }
    val selectedNearbySets = remember { mutableStateOf(setOf("Shopping Malls", "Schools")) }

    var formMessageError by remember { mutableStateOf<String?>(null) }
    var showNotificationDrawer by remember { mutableStateOf(false) }

    val user = currentUser

    if (user == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ManageAccounts, contentDescription = "", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Landlord Verification Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please register or log in with Landlord role and perform 2-Factor registered SMS validation to upload vacant properties.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.selectTab("Profile") },
                    colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen)
                ) {
                    Text("Secure Login Access")
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Landlord Partner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.bodySmall,
                        color = KenyaGreen
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // System Logs / Notifications Badge
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { showNotificationDrawer = !showNotificationDrawer }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (landlordNotifications.any { !it.isRead }) KenyaGreen else Color.Gray
                        )
                        if (landlordNotifications.any { !it.isRead }) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, RoundedCornerShape(5.dp))
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            showCreateForm = !showCreateForm
                            currentStep = 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (showCreateForm) Color.Red else KenyaGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("post_property_button")
                    ) {
                        Icon(imageVector = if (showCreateForm) Icons.Default.Close else Icons.Default.AddHome, contentDescription = "Add listing")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showCreateForm) "Cancel" else "Post Vacant Property", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Notifications drawer
            AnimatedVisibility(visible = showNotificationDrawer) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("System AI Verification Alerts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Row {
                                TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                                    Text("Mark Read", fontSize = 11.sp, color = KenyaGreen)
                                }
                                TextButton(onClick = { viewModel.clearAllNotifications() }) {
                                    Text("Clear All", fontSize = 11.sp, color = Color.Red)
                                }
                            }
                        }
                        if (landlordNotifications.isEmpty()) {
                            Text("No recent automated property validation history logs.", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            landlordNotifications.take(5).forEach { log ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (log.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(log.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                        Text(log.message, fontSize = 11.sp)
                                        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                                        Text(dateStr, fontSize = 9.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Post Form Wizard
            AnimatedVisibility(
                visible = showCreateForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Multi-Step AI Property Verification Setup", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = KenyaGreen)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress Dots
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..4) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (currentStep == i) KenyaGreen else Color.LightGray, RoundedCornerShape(5.dp))
                                )
                                if (i < 4) {
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(2.dp)
                                            .background(if (currentStep > i) KenyaGreen else Color.LightGray)
                                    )
                                }
                            }
                        }

                        // STEP 1: DROP MAP PIN
                        if (currentStep == 1) {
                            Text("Step 1: Pinned Map Location Co-ordinates", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Point precisely where your listed building is situated inside Kenya boundaries. Our AI will matching this coordinate with local street images.", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Location presets for instant pipeline testing!
                            Text("Fast Location Test Presets (Pick a scenario):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            val presets = listOf(
                                Triple("Nairobi CBD (Valid)", -1.2921, 36.8219),
                                Triple("Nyali Beach (Valid)", -4.0255, 39.7314),
                                Triple("Nairobi National Park (Forbidden)", -1.3700, 36.8500),
                                Triple("Lake Victoria (Forbidden Water)", -0.5000, 34.2000),
                                Triple("Tsavo West Zone (Forbidden Area)", -3.1000, 38.0000),
                                Triple("Outside Edge (Invalid County)", -5.5000, 32.0000)
                            )
                            LazyRow(modifier = Modifier.padding(vertical = 6.dp)) {
                                items(presets) { preset ->
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .background(
                                                color = if (fLatitude == preset.second) KenyaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                fLatitude = preset.second
                                                fLongitude = preset.third
                                                fResolvedAddress = preset.first
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(preset.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (fLatitude == preset.second) KenyaGreen else Color.DarkGray)
                                    }
                                }
                            }

                            // Interactive Simulated Map with dropped pin
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE4F3E5)) // Greenish land
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            // Translate pixel offset to Kenya coordinates inside bounding box
                                            val latPercent = offset.y / 180f
                                            val lngPercent = offset.x / size.width.toFloat()
                                            fLatitude = -4.7 + (9.3 * (1f - latPercent))
                                            fLongitude = 33.9 + (8.0 * lngPercent)
                                            fResolvedAddress = "Dropped Pin (Lat: %.4f, Lng: %.4f)".format(fLatitude, fLongitude)
                                        }
                                    }
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Draw decorative map roads
                                    drawLine(Color.White, Offset(0f, 100f), Offset(size.width, 400f), strokeWidth = 12f)
                                    drawLine(Color.White, Offset(200f, 0f), Offset(500f, size.height), strokeWidth = 8f)
                                    drawLine(Color.White, Offset(800f, 0f), Offset(100f, size.height), strokeWidth = 10f)

                                    // Draw lake vector
                                    drawCircle(Color(0xFFA5C9EB), radius = 60f, center = Offset(50f, size.height - 50f))
                                }

                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .pointerInput(Unit) {}
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Map Pin",
                                        tint = Color.Red,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text("Dropped Pin", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                }

                                Text(
                                    "[Click anywhere on canvas map to drop pin custom location]",
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                                    Text("Coordinates Registry Parameters:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("Latitude: $fLatitude", fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    Text("Longitude: $fLongitude", fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    Text("Geocoded target: $fResolvedAddress", fontSize = 11.sp, color = KenyaGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // STEP 2: METADATA & PHOTO UPLOAD SIMULATOR
                        else if (currentStep == 2) {
                            Text("Step 2: Upload candidate house photos & Visual Metadata", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Configure photo settings below to test the automated pipeline's security defenses.", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Scenario switches
                            Card(
                                border = CardDefaults.outlinedCardBorder(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                    Text("Platform Security Pipeline Simulation Checks:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KenyaGreen)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Check 1: Spoofed EXIF GPS
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Switch(
                                            checked = useSpoofedExif,
                                            onCheckedChange = { useSpoofedExif = it },
                                            modifier = Modifier.testTag("spoofed_exif_switch")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Trigger EXIF Distance Failure (Scraped image simulation)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Injects camera metadata mismatch coordinates taken >200m away.", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                                    // Check 2: Visual duplicate (pHash match)
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Switch(
                                            checked = useDuplicatePhoto,
                                            onCheckedChange = { useDuplicatePhoto = it },
                                            modifier = Modifier.testTag("duplicate_photos_switch")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Trigger Copied/Duplicate Image Failure (pHash Check)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Perceptual average hashing checks if this asset is already listed.", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                                    // Check 3: Gemini Photo visual mismatch
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Switch(
                                            checked = useMismatchedBuilding,
                                            onCheckedChange = { useMismatchedBuilding = it },
                                            modifier = Modifier.testTag("mismatched_building_switch")
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Trigger Gemini Visual Mismatch Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Sends mismatched structure specs causing Gemini Vision API check fail.", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                                    // Check 4: Rural cover Satellite manual review
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Switch(checked = triggerRuralOfflineReview, onCheckedChange = { triggerRuralOfflineReview = it })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Simulate rural zone (Street View Coverage Limit)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Flags to transfer listing to manually moderated satellites check queue.", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 3: DATA SPECIFICATIONS FORM
                        else if (currentStep == 3) {
                            Text("Step 3: Property specification data specifications form", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = fTitle,
                                onValueChange = { fTitle = it },
                                label = { Text("Property Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("title_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = fCounty,
                                    onValueChange = { fCounty = it },
                                    label = { Text("County Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = fEstate,
                                    onValueChange = { fEstate = it },
                                    label = { Text("Estate Area") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("estate_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = fPrice,
                                    onValueChange = { fPrice = it },
                                    label = { Text("Price (KES/Mo)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("price_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = fSize,
                                    onValueChange = { fSize = it },
                                    label = { Text("Size (sqft)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = fBedrooms,
                                    onValueChange = { fBedrooms = it },
                                    label = { Text("Bedrooms") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = fBathrooms,
                                    onValueChange = { fBathrooms = it },
                                    label = { Text("Bathrooms") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                                )
                            }
                        }

                        // STEP 4: CONFIRM & SUBMIT
                        else if (currentStep == 4) {
                            Text("Step 4: Verification Contract Agreement", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                    Text("Contract Details Summary:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = KenyaGreen)
                                    Text("Title: $fTitle", fontSize = 11.sp)
                                    Text("Rent Rate: KES $fPrice / Month", fontSize = 11.sp)
                                    Text("Bounded Landmark: $fEstate, $fCounty", fontSize = 11.sp)
                                    Text("Map Registry GPS: ($fLatitude, $fLongitude)", fontSize = 11.sp)
                                    Text("EXIF Emulation: " + (if (useSpoofedExif) "Mismatched / Spoofed (Failure)" else "Original Meta (Safe)"), fontSize = 11.sp)
                                    Text("Visual Duplicate: " + (if (useDuplicatePhoto) "Reused Asset (Failure)" else "Fresh photography (Safe)"), fontSize = 11.sp)
                                    Text("Structural Matches: " + (if (useMismatchedBuilding) "Misrepresented properties (Failure)" else "Verified match (Safe)"), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("I hereby declare that this metadata represents a legally verified, physically standing architectural unit inside Kenya boundaries. I understand intentional spoof listings are flagged for automated account suspension.", fontSize = 10.sp, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (formMessageError != null) {
                            Text(formMessageError ?: "", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        // Control Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentStep > 1) {
                                Button(
                                    onClick = { currentStep-- },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Black)
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Back", fontSize = 11.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp)) // Placeholder
                            }

                            if (currentStep < 4) {
                                Button(
                                    onClick = {
                                        if (currentStep == 3 && (fTitle.isBlank() || fPrice.isBlank() || fEstate.isBlank())) {
                                            formMessageError = "Property details Title, Rent, and Estate are required."
                                        } else {
                                            formMessageError = null
                                            currentStep++
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                                    modifier = Modifier.testTag("step_next_button")
                                ) {
                                    Text("Next", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        // Package visual assets with indicators
                                        var photoBuffer = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914"
                                        if (useSpoofedExif) {
                                            photoBuffer += ",EXIF_FAIL"
                                        }
                                        if (useDuplicatePhoto) {
                                            photoBuffer += ",DUPLICATE"
                                        }
                                        if (useMismatchedBuilding) {
                                            photoBuffer += ",MISMATCH"
                                        }

                                        var finalAddr = fResolvedAddress
                                        if (triggerRuralOfflineReview) {
                                            finalAddr += ", rural county"
                                        }

                                        viewModel.createPropertyListing(
                                            title = fTitle,
                                            type = fType,
                                            county = fCounty,
                                            estate = fEstate,
                                            price = fPrice.toDoubleOrNull() ?: 35000.0,
                                            isNegotiable = fIsNegotiable,
                                            beds = fBedrooms.toIntOrNull() ?: 2,
                                            baths = fBathrooms.toIntOrNull() ?: 2,
                                            size = fSize.toIntOrNull() ?: 850,
                                            amenities = selectedAmenSets.value,
                                            nearby = selectedNearbySets.value,
                                            isPetFriendly = fIsPetFriendly,
                                            availableDate = fAvailableDate,
                                            imageUrls = listOf(photoBuffer),
                                            latitude = fLatitude,
                                            longitude = fLongitude,
                                            resolvedAddress = finalAddr
                                        )

                                        // Clear states
                                        fTitle = ""
                                        fEstate = ""
                                        fPrice = ""
                                        showCreateForm = false
                                        currentStep = 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                                    modifier = Modifier.testTag("secure_post_listing_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Submit")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Authorize & Run verification", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Pipeline Verification Tracker inside landlord properties List
            Text(
                text = "My Rental Listings Status Tracker",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (landlordProperties.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No listings added yet. Start by clicking \"Post Vacant Property\" above!", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            } else {
                landlordProperties.forEach { lp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("landlord_property_card_${lp.id}"),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(lp.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${lp.estate}, ${lp.county} • KES ${lp.rentAmount.toInt()}", fontSize = 11.sp, color = Color.Gray)
                                }

                                // Interactive verification badge with colors
                                val statusColor = when (lp.verificationStatus) {
                                    "VERIFIED" -> KenyaGreen
                                    "PENDING" -> Color(0xFFEAA300)
                                    "NEEDS_REVIEW" -> Color.Blue
                                    "REJECTED" -> Color.Red
                                    else -> Color.Gray
                                }
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = lp.verificationStatus,
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("status_badge_${lp.id}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // PENDING PIPELINE GRAPHICS AND LOGGING DETAILS
                            if (lp.verificationStatus == "PENDING") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF9E6), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFEAA300))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Verification Underway: Checking pin boundaries, street-view overlays, EXIF distances, and duplicate digital hashes...", fontSize = 10.sp, color = Color(0xFFB57000))
                                    }
                                }
                            } else if (lp.verificationStatus == "REJECTED") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFEEEE), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(imageVector = Icons.Default.Error, contentDescription = "error", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Reason for Automatic Rejection:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                            Text(lp.verificationReason, fontSize = 10.sp, color = Color.DarkGray)
                                            Text("Confidence Score matching: ${lp.aiConfidence}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        }
                                    }
                                }
                            } else if (lp.verificationStatus == "NEEDS_REVIEW") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEBF3FF), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(imageVector = Icons.Default.Help, contentDescription = "Inconclusive", tint = Color.Blue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Awaiting Manual Satellite Clearance:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                                            Text(lp.verificationReason, fontSize = 10.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            } else {
                                // VERIFIED! Show AI explanation badge details
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEEF9EE), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified tag", tint = KenyaGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Automated AI Audit Report:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KenyaGreen)
                                            Text(lp.verificationReason, fontSize = 10.sp, color = Color.DarkGray)
                                            Text("Verify Confidence: ${lp.aiConfidence}% • Status: ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = KenyaGreen)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row {
                                Button(
                                    onClick = { viewModel.viewPropertyDetail(lp.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("View Details specs", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.deletePropertyFromHub(lp.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Delist Property", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seeker Inquiries section
            Text(
                text = "Seeker viewing inquiries analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (platformAnalyticsInquiries.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No inquiries submitted by house seekers yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                platformAnalyticsInquiries.filter { it.landlordId == user.id }.forEach { inq ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Inquiry ID: #${inq.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = KenyaGreen)
                                Box(
                                    modifier = Modifier
                                        .background(KenyaGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(inq.timestamp))
                                    Text(formattedTime, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = KenyaGreen)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(inq.message, style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.createOrGetChatRoom(inq.seekerId)
                                    viewModel.selectTab("Chats")
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Respond on Instant Chat", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
