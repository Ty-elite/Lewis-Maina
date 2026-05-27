package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLogEntity
import com.example.data.model.PropertyEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.KenyaGreen
import com.example.viewmodel.KenyaRentViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allProperties by viewModel.allProperties.collectAsState()
    val allRegisteredUsers by viewModel.allRegisteredUsers.collectAsState()
    val fullAuditLogs by viewModel.auditLogs.collectAsState()

    val scrollState = rememberScrollState()

    var activeAdminTab by remember { mutableStateOf("Manual Verification") } // "Users", "Manual Verification", "Audit Logs"
    var filterVerificationStatus by remember { mutableStateOf("ALL") } // "ALL", "NEEDS_REVIEW", "REJECTED"

    // Manual Rejection State
    var activeRejectPropertyId by remember { mutableStateOf<String?>(null) }
    var manualRejectReason by remember { mutableStateOf("") }

    val user = currentUser

    if (user == null || user.role != UserRole.ADMIN) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = "", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Admin Clearance Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("This dashboard allows listing moderators to delete reports, ban misbehaving scammers, and review compliance audit logs.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.selectTab("Profile") }, colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen)) {
                    Text("Secure Login Access")
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color(0xFFE5A93B), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Platform Moderation Unit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Admin Cleared: ${user.fullName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KenyaGreen
                        )
                    }
                }
            }

            // Global Metrics Stats Overview Panel
            Text(
                text = "Live Network Metrics",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val needsReviewCount = allProperties.count { it.verificationStatus == "NEEDS_REVIEW" }
                val rejectedCount = allProperties.count { it.verificationStatus == "REJECTED" }
                MetricSummaryCard(label = "Total Vacant Units", value = allProperties.size.toString(), icon = Icons.Default.HomeWork, modifier = Modifier.weight(1f))
                MetricSummaryCard(label = "Pending Manual Review", value = needsReviewCount.toString(), icon = Icons.Default.PendingActions, modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val rejectedCount = allProperties.count { it.verificationStatus == "REJECTED" }
                val bannedCount = allRegisteredUsers.filter { it.isBanned }.size
                MetricSummaryCard(label = "AI Blocked Fraud", value = rejectedCount.toString(), icon = Icons.Default.Shield, modifier = Modifier.weight(1f))
                MetricSummaryCard(label = "Banned Scammer users", value = bannedCount.toString(), icon = Icons.Default.Block, modifier = Modifier.weight(1f))
            }

            // Admin tab controls
            ScrollableTabRow(
                selectedTabIndex = when (activeAdminTab) {
                    "Manual Verification" -> 0
                    "Users" -> 1
                    "Audit Logs" -> 2
                    else -> 0
                },
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                indicator = {}
            ) {
                listOf("Manual Verification", "Users", "Audit Logs").forEach { tab ->
                    val isSelected = activeAdminTab == tab
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) KenyaGreen else Color.Transparent)
                            .clickable { activeAdminTab = tab }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.testTag("admin_tab_button_$tab")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TAB 1: MANUAL VERIFICATION DASHBOARD (NEW CODES)
            if (activeAdminTab == "Manual Verification") {
                Text(
                    text = "Automated AI Verification Queue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Filters Inside Tab
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    listOf("ALL", "NEEDS_REVIEW", "REJECTED").forEach { statusLabel ->
                        val isSel = filterVerificationStatus == statusLabel
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    color = if (isSel) KenyaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { filterVerificationStatus = statusLabel }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                statusLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) KenyaGreen else Color.Gray
                            )
                        }
                    }
                }

                val filteredQueue = allProperties.filter {
                    when (filterVerificationStatus) {
                        "NEEDS_REVIEW" -> it.verificationStatus == "NEEDS_REVIEW"
                        "REJECTED" -> it.verificationStatus == "REJECTED"
                        else -> it.verificationStatus == "NEEDS_REVIEW" || it.verificationStatus == "REJECTED"
                    }
                }

                if (filteredQueue.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.FactCheck, contentDescription = "checks", tint = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("All clear! No listings currently awaiting manual clearance.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                } else {
                    filteredQueue.forEach { item ->
                        // Calculate Fraud Score dynamically: count of listings rejected belongs to this landlord
                        val landlordFraudScore = allProperties.count { it.landlordId == item.landlordId && it.verificationStatus == "REJECTED" }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("admin_verification_card_${item.id}"),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("By Landlord: ${item.landlordName} • Phone: ${item.landlordPhone}", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    // Display fraud score warning badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (landlordFraudScore >= 3) Color.Red.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "Fraud Score: $landlordFraudScore",
                                            color = if (landlordFraudScore >= 3) Color.Red else Color.Gray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // SIDE BY SIDE COMPARISON (SIMULATED VISUAL ASSET LAYOUTS)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Landlord uploaded photo
                                    Card(
                                        modifier = Modifier.weight(1f).height(100.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.4f))
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Icon(imageVector = Icons.Default.Image, contentDescription = "", modifier = Modifier.size(36.dp).align(Alignment.Center), tint = Color.Gray)
                                            Text("Uploaded Rental Photo", modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.6f)).fillMaxWidth().padding(2.dp), fontSize = 8.sp, color = Color.White, textAlign = TextAlign.Center)
                                        }
                                    }

                                    // Google Street View image side comparison
                                    Card(
                                        modifier = Modifier.weight(1f).height(100.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.05f))
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (item.streetViewImageUrl.isNotBlank()) {
                                                Icon(imageVector = Icons.Default.Streetview, contentDescription = "", modifier = Modifier.size(36.dp).align(Alignment.Center), tint = KenyaGreen)
                                            } else {
                                                Icon(imageVector = Icons.Default.Satellite, contentDescription = "", modifier = Modifier.size(36.dp).align(Alignment.Center), tint = Color.Red)
                                            }
                                            Text(
                                                text = if (item.streetViewImageUrl.isNotBlank()) "Google Street View Match" else "No coverage (rural satellite)",
                                                modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.6f)).fillMaxWidth().padding(2.dp),
                                                fontSize = 8.sp,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // AI Diagnosis Explanation Details
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = KenyaGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("AI Diagnostics (Confidence: ${item.aiConfidence}%):", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = KenyaGreen)
                                        }
                                        Text(item.verificationReason, fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                                        Text("Coord bounds: Latitude ${item.latitude}, Longitude ${item.longitude}", fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp), color = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Input form on active Rejection
                                val isRejectingThis = activeRejectPropertyId == item.id
                                if (isRejectingThis) {
                                    OutlinedTextField(
                                        value = manualRejectReason,
                                        onValueChange = { manualRejectReason = it },
                                        label = { Text("Describe manual rejection reason (Mandatory)") },
                                        singleLine = false,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("admin_rejection_reason_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red)
                                    )
                                }

                                // Interactive Actions Left/Right buttons
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (isRejectingThis) {
                                        Button(
                                            onClick = {
                                                if (manualRejectReason.isNotBlank()) {
                                                    viewModel.adminRejectListing(item.id, manualRejectReason)
                                                    activeRejectPropertyId = null
                                                    manualRejectReason = ""
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1f).testTag("confirm_reject_button")
                                        ) {
                                            Text("Confirm Red Reject", fontSize = 11.sp, color = Color.White)
                                        }
                                        Button(
                                            onClick = {
                                                activeRejectPropertyId = null
                                                manualRejectReason = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(0.5f)
                                        ) {
                                            Text("Back", fontSize = 11.sp, color = Color.Black)
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.adminApproveListing(item.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1f).testTag("admin_approve_button")
                                        ) {
                                            Icon(imageVector = Icons.Default.Verified, contentDescription = "", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Verify & Publish live", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { activeRejectPropertyId = item.id },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1f).testTag("admin_reject_button")
                                        ) {
                                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Manual Reject Specs", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ANOMALOUS FRAUD HEATMAP (VISUAL ANALYTICS OF REJECTED FRAUDULENT PLACES)
                Text(
                    text = "Metropolitan Fraud Heatmap (Blocked Submissions)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text("Real-time visual concentrations of black-listed/scammer listing coords blocking coordinates matches.", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B1B1D)) // Dark radar theme
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Custom draw coordinates hotspots
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Nairobi core hotspot
                            drawCircle(Color.Red.copy(alpha = 0.45f), radius = 50f, center = Offset(180f, 100f))
                            drawCircle(Color.Red.copy(alpha = 0.25f), radius = 80f, center = Offset(180f, 100f))
                            drawCircle(Color.White, radius = 4f, center = Offset(180f, 100f))

                            // Mombasa coastal hotspot
                            drawCircle(Color.Red.copy(alpha = 0.35f), radius = 30f, center = Offset(450f, 150f))
                            drawCircle(Color.Red.copy(alpha = 0.15f), radius = 50f, center = Offset(450f, 150f))
                            drawCircle(Color.White, radius = 4f, center = Offset(450f, 150f))

                            // Eldoret / Kisumu hotspot
                            drawCircle(Color.Red.copy(alpha = 0.3f), radius = 25f, center = Offset(80f, 60f))
                            drawCircle(Color.White, radius = 4f, center = Offset(80f, 60f))

                            // Draw radar grids
                            drawCircle(Color.Green.copy(alpha = 0.15f), radius = 120f, center = Offset(size.width / 2, size.height / 2))
                            drawLine(Color.Green.copy(alpha = 0.1f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2))
                        }

                        // Hotspots labels
                        Text("NAIROBI HOTSPOT (5 BLOCK REJECTS)", modifier = Modifier.align(Alignment.TopStart).padding(start = 120.dp, top = 20.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(4.dp), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("MOMBASA HARBOR HOTSPOT (3 REJECTS)", modifier = Modifier.align(Alignment.BottomEnd).padding(end = 60.dp, bottom = 20.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(4.dp), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("KISUMU RADIAL HOTSPOT (2 REJECTS)", modifier = Modifier.align(Alignment.TopStart).padding(start = 10.dp, top = 90.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(4.dp), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TAB 2: ORIGINAL USERS MANAGEMENT BAN LISTS
            else if (activeAdminTab == "Users") {
                Text(
                    text = "User Accounts & Scammer Ban list",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                allRegisteredUsers.forEach { ac ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ac.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${ac.email} (${ac.role}) • Phone: ${ac.phone}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                if (ac.isBanned) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Red, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("BANNED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row {
                                Button(
                                    onClick = { viewModel.moderatorToggleBan(ac.id, !ac.isBanned) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (ac.isBanned) KenyaGreen else Color.Red),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (ac.isBanned) Icons.Default.Check else Icons.Default.DeleteForever,
                                        contentDescription = "Action toggle",
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (ac.isBanned) "Revoke Scammer Ban" else "Ban Scammer user", fontSize = 10.sp)
                                }

                                if (ac.role == UserRole.LANDLORD) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.moderatorToggleVerifiedLandlord(ac.id, !ac.isVerifiedLandlord) },
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(38.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (ac.isVerifiedLandlord) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE5A93B),
                                            contentColor = if (ac.isVerifiedLandlord) MaterialTheme.colorScheme.onSurface else Color.Black
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (ac.isVerifiedLandlord) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = "verified tag flag",
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (ac.isVerifiedLandlord) "Revoke Verification" else "Approve Landlord 2FA Identity", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            // TAB 3: ORIGINAL AUDIT TRAIL LISTINGS
            else if (activeAdminTab == "Audit Logs") {
                Text(
                    text = "System Action Audit Trails logs",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = KenyaGreen,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fullAuditLogs) { audit ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ACTION: ${audit.actionKey}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = KenyaGreen
                                    )

                                    Box(
                                        modifier = Modifier
                                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    ) {
                                        Text(
                                            text = audit.timestamp,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Actor: ${audit.actorName} (User #${audit.actorId})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = audit.details,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun MetricSummaryCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = KenyaGreen, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = KenyaGreen)
            }
        }
    }
}
