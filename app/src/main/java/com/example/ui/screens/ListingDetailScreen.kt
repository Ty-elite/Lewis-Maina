package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.ReviewEntity
import com.example.ui.theme.KenyaGreen
import com.example.viewmodel.KenyaRentViewModel

@Composable
fun ListingDetailScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
    val propertyId by viewModel.selectedPropertyId.collectAsState()
    val propertyDetails by viewModel.selectedPropertyDetails.collectAsState()
    val landlordDetails by viewModel.selectedLandlordDetails.collectAsState()
    val reviews by viewModel.selectedPropertyReviews.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isBookmarked by viewModel.isSelectedPropertyBookmarked.collectAsState()

    val scrollState = rememberScrollState()

    var inquiryMessage by remember { mutableStateOf("I am highly interested in viewing this property unit, please schedule a visiting slot.") }
    var inquirySent by remember { mutableStateOf(false) }

    var userRatingInput by remember { mutableStateOf(5) }
    var userCommentInput by remember { mutableStateOf("") }

    var isVideoPlaying by remember { mutableStateOf(false) }
    var secondsPlayed by remember { mutableStateOf(0) }
    var shareSheetOpen by remember { mutableStateOf(false) }

    // Periodically update simulated virtual viewing progression
    LaunchedEffect(isVideoPlaying) {
        if (isVideoPlaying) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                secondsPlayed = (secondsPlayed + 1) % 15
            }
        }
    }

    val property = propertyDetails

    if (propertyId == null || property == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = KenyaGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Retrieving Vacant property specs...", style = MaterialTheme.typography.bodySmall)
            }
        }
        return
    }

    val photos = property.photosCsv.split(",").filter { it.isNotBlank() }
    val amenities = property.amenitiesCsv.split(",").filter { it.isNotBlank() }
    val nearbys = property.nearbyPlacesCsv.split(",").filter { it.isNotBlank() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Hero Gallery Header Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                if (photos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HomeRepairService, contentDescription = "no-photo", tint = Color.LightGray)
                    }
                } else {
                    LazyRow(modifier = Modifier.fillMaxSize()) {
                        items(photos) { ph ->
                            Image(
                                painter = rememberAsyncImagePainter(model = ph),
                                contentDescription = "Apartment Gallery Image",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(360.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Header buttons overlay (Back arrow, Bookmark icon, Share icons) Let's make sure it is super readable
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.clearSelectedProperty() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White)
                    }

                    Row {
                        IconButton(
                            onClick = { shareSheetOpen = true },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share property details", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.toggleBookmarkSelected() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark / Favorite Toggle",
                                tint = if (isBookmarked) Color(0xFFE5A93B) else Color.White
                            )
                        }
                    }
                }

                // Negotiable badges
                if (property.isNegotiable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(Color(0xFFE5A93B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("NEGOTIABLE RENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            // Core Property specification contents
            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = property.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "LocPin", tint = KenyaGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${property.estate}, ${property.county}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Price Cap
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "KES ${property.rentAmount.toInt().toString().replace(Regex("(\\d)(?=(\\d{3})+(?!\\d))"), "$1,")}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = KenyaGreen
                        )
                        Text(
                            text = "per Month",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Core Spec Cards (Bedrooms count, Size sqft, Available Date)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpecCell(icon = Icons.Default.Bed, label = "Bedrooms", value = "${property.bedrooms} Bed", modifier = Modifier.weight(1f))
                    SpecCell(icon = Icons.Default.Bathtub, label = "Bathrooms", value = "${property.bathrooms} Bath", modifier = Modifier.weight(1f))
                    SpecCell(icon = Icons.Default.AspectRatio, label = "Unit area", value = "${property.sizeSqft} sqft", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Virtual Walkthrough Video Simulation (Requirement: "virtual physical walkthrough loops")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Virtual Walkthrough Video",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = KenyaGreen
                        )
                        Text(
                            text = "Looping physical viewing simulation to verify room condition.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.Black, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVideoPlaying) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = KenyaGreen,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Playing Virtual Walkthrough loop... 00:${secondsPlayed.toString().padStart(2, '0')} / 00:15",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { isVideoPlaying = false }) {
                                        Text("Stop Stream", color = Color.Red, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { isVideoPlaying = true },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = KenyaGreen)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play virtual walkthrough stream", tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Simulated Real-time Room Stream (15s Loop)",
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Detailed Property Description content
                Text(
                    text = "Property Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "This stunning ${property.bedrooms} bedroom ${property.type} is situated in the prestigious area of ${property.estate}, ${property.county}. It features spacious rooms, modern finishes, and is available immediately for KES ${property.rentAmount} per month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Amenities list grid
                Text(
                    text = "Amenities & Services Included",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    amenities.chunked(2).forEach { chunk ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            chunk.forEach { am ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = "Check", tint = KenyaGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(am, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nearby Landmark location mapping listing
                Text(
                    text = "Nearby Landmark Hotspots",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    nearbys.forEach { nb ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Navigation, contentDescription = "Navigation node pointer", tint = Color(0xFFE5A93B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(nb, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Landlord disclosure module (Landlord Contact Reveal Requirement: "landlord contact details reveal button")
                Text(
                    text = "Landlord Partner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(KenyaGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (landlordDetails?.fullName?.firstOrNull() ?: 'L').toString().uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = landlordDetails?.fullName ?: "Private Landlord Partner",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Mobile Verification Active • Verified Landlord ID",
                                    fontSize = 11.sp,
                                    color = KenyaGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Secured contact layout state
                        val hasRevealed = viewModel.landlordContactRevealed.collectAsState().value
                        if (hasRevealed) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(KenyaGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Official Contact Number", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = landlordDetails?.phone ?: "Not available",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = KenyaGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Official Email Contact", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = landlordDetails?.email ?: "Not listed",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.revealLandlordContact() },
                                colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "reveal")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reveal Verified Landlord Contacts")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // In-App direct messenger module
                Text(
                    text = "Request Viewing Slot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (inquirySent) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KenyaGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Sent", tint = KenyaGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your secure viewing slot has been scheduled!", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = KenyaGreen)
                            Text("Landlord has received details on Chat. Refresh 'Chats' tab to view responding slots.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = inquiryMessage,
                        onValueChange = { inquiryMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Enter inquiry / preferred viewing afternoon") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (currentUser == null) {
                                viewModel.selectTab("Profile")
                            } else {
                                viewModel.sendSimulatedInquiry(inquiryMessage)
                                inquirySent = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MailOutline, contentDescription = "send inquiry")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentUser == null) "Log In to Schedule Slot" else "Send Secure Chat Inquiry")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Occupants reviews list (Requirement: "submitting a review, rating, and descriptive comments")
                Text(
                    text = "Occupant Reviews & Ratings Unit feedback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (currentUser != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Submit Professional Review", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Star interactive bar
                            Row {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= userRatingInput) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Rating choice star",
                                        tint = Color(0xFFE5A93B),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { userRatingInput = star }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = userCommentInput,
                                onValueChange = { userCommentInput = it },
                                label = { Text("Review comment...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (userCommentInput.isNotBlank()) {
                                        viewModel.submitPropertyReview(userRatingInput, userCommentInput)
                                        userCommentInput = ""
                                        userRatingInput = 5
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Post Review Feedback", fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (reviews.isEmpty()) {
                    Text(
                        "No historic tenant reviews submitted for this vacant unit yet. Be the first!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    reviews.forEach { r ->
                        ReviewCardItem(review = r)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Shared dialog overlay
        if (shareSheetOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Share KenyaRent Listing via WhatsApp / Telegram", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "https://kenya-rent.com/property/${property.id}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = KenyaGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { shareSheetOpen = false },
                            colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Copy link link details & Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecCell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = KenyaGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReviewCardItem(review: ReviewEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Occupant tenant verification node",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen
                )

                Row {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star ratings score",
                            tint = Color(0xFFE5A93B),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Submitted on: ${review.timestamp}",
                fontSize = 9.sp,
                color = Color.LightGray
            )
        }
    }
}
