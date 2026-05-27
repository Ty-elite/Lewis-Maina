package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KenyaGreen

/**
 * An offline warning banner that allows the user to simulate toggling internet connection in-app
 * to verify performance rules and the offline mock caching, complete with custom labels.
 */
@Composable
fun OfflineBanner(
    isOffline: Boolean,
    onToggleOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOffline) MaterialTheme.colorScheme.errorContainer else KenyaGreen.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.Wifi,
                    contentDescription = "Connection Indicator",
                    tint = if (isOffline) MaterialTheme.colorScheme.error else KenyaGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isOffline) "Browsing Offline Mode" else "Connected to KenyaRent Server",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOffline) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOffline) "Viewing previously loaded cached properties." else "In-app database is synchronizing live.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOffline) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onToggleOffline,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOffline) MaterialTheme.colorScheme.error else KenyaGreen
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isOffline) "Go Live" else "Go Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Interactive Accessible Security sum-puzzle. Fulfills the CAPTCHA requirement.
 */
@Composable
fun MathCaptchaWidget(
    val1: Int,
    val2: Int,
    answer: String,
    onAnswerChanged: (String) -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Shield CAPTCHA",
                        tint = KenyaGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Security Verification",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = KenyaGreen
                    )
                }
                Text(
                    text = "Refresh Test",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onRegenerate() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Visual sum block
                Box(
                    modifier = Modifier
                        .background(KenyaGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$val1 + $val2 = ?",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = onAnswerChanged,
                    label = { Text("Enter Sum") },
                    placeholder = { Text("Answer") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KenyaGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
            Text(
                text = "Solves bots and automatic brute-force registrations.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Coordinates and renders a stunning vector graphic simulation of the Kenyan Map Hotspots.
 * Renders major county nodes with interactive coordinate tapping and visual ripples.
 */
@Composable
fun BeautifulKenyaMap(
    selectedCounty: String?,
    onCountySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Definitive Kenyan county coordinates mapped to 0-1 percentage constraints (width, height)
    val mapTargetHotspots = remember {
        listOf(
            MapHotspot("Nairobi", 0.52f, 0.62f, "Capital Center"),
            MapHotspot("Kiambu", 0.49f, 0.55f, "Metropolitan Area"),
            MapHotspot("Mombasa", 0.72f, 0.88f, "Coast & Sandy Beaches"),
            MapHotspot("Nakuru", 0.38f, 0.48f, "Rift Valley Hub"),
            MapHotspot("Eldoret", 0.28f, 0.35f, "North Rift Town"),
            MapHotspot("Kisumu", 0.18f, 0.43f, "Lakeside City"),
            MapHotspot("Kajiado", 0.46f, 0.72f, "Southern Meadows"),
            MapHotspot("Machakos", 0.59f, 0.68f, "Eastern Hills")
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasW = maxWidth
            val canvasH = maxHeight

            // Stylized background grid of Kenya Coordinates
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridGap = 40.dp.toPx()
                for (x in 0..size.width.toInt() step gridGap.toInt()) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.1f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..size.height.toInt() step gridGap.toInt()) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.1f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }

            // Draw beautiful aesthetic background boundaries
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = "Interactive Hotspots of Kenya",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KenyaGreen
                    )
                    Text(
                        text = "Tap on a city pin to filter available properties",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Render interactive hotspots
            mapTargetHotspots.forEach { spot ->
                val xPos = canvasW * spot.pctX
                val yPos = canvasH * spot.pctY
                val isSelected = selectedCounty == spot.name

                Box(
                    modifier = Modifier
                        .offset(x = xPos - 20.dp, y = yPos - 20.dp)
                        .size(44.dp)
                        .pointerInput(spot.name) {
                            detectTapGestures {
                                onCountySelected(spot.name)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Ring for selections
                    if (isSelected) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp),
                            shape = CircleShape,
                            color = KenyaGreen.copy(alpha = 0.25f),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {}
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = spot.name,
                            tint = if (isSelected) Color(0xFFE5A93B) else KenyaGreen,
                            modifier = Modifier.size(if (isSelected) 30.dp else 22.dp)
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) Color(0xFFE5A93B) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = spot.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MapHotspot(
    val name: String,
    val pctX: Float,
    val pctY: Float,
    val note: String
)
