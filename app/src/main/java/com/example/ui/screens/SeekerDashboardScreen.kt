package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.KenyaGreen
import com.example.viewmodel.KenyaRentViewModel

@Composable
fun SeekerDashboardScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val bookmarkedProperties by viewModel.bookmarkedProperties.collectAsState()
    val savedAlertsList by viewModel.savedSearches.collectAsState()

    val scrollState = rememberScrollState()
    var showErasureWarningDialog by remember { mutableStateOf(false) }

    val user = currentUser

    if (user == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountCircle, contentDescription = "", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Login Required to View Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please register or sign in as an occupant to save bookmarks, setup real-time alerts & manage profile.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = Color.Gray)
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
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Profile Summary Header Module
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        color = KenyaGreen
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.fullName.take(1).uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Phone Line: ${user.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bookmarked Properties Section
            Text(
                text = "Your Bookmarked Listings (${bookmarkedProperties.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (bookmarkedProperties.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No bookmarks yet.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                bookmarkedProperties.forEach { bp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.viewPropertyDetail(bp.id) },
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(bp.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("${bp.estate}, ${bp.county}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.apiBookmarkProperty(bp.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "delete bookmark", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Saved Searches Alerts Checklist (Instant Debounced criteria saved)
            Text(
                text = "Real-time Instant Search Alerts (${savedAlertsList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KenyaGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (savedAlertsList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No saved search alert criteria set yet. Go to Browse tab to trigger 'Save Search'.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                savedAlertsList.forEach { sa ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Preset: ${sa.query.ifBlank { "All Vacant Properties" }}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Location: ${sa.county ?: "Anywhere"} • Price Max: KES ${sa.maxPrice.toInt()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.deleteSavedSearch(sa.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "delete alert", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // GDPR Compliance Erasure Action (Requirement: "GDPR compliance data erasure requests")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "GDPR security compliance", tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GDPR Account Privacy Rights",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "According to European GDPR laws, you have the absolute 'Right to be Forgotten'. Clicking the link below removes all registered info, chat logs, profile metadata directly from KenyaRent databases.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showErasureWarningDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Trigger Data Erasure", color = Color.White)
                    }
                }
            }
        }
    }

    if (showErasureWarningDialog) {
        AlertDialog(
            onDismissRequest = { showErasureWarningDialog = false },
            title = { Text("Absolute Account Data Erasure") },
            text = { Text("Are you completely sure? This will delete your current tenant credentials, bookmark listings history, and chat messages immediately. This action is final.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErasureWarningDialog = false
                        viewModel.eraseMyUserDataGDPR()
                    }
                ) {
                    Text("YES, CONFIRM ERASURE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showErasureWarningDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
