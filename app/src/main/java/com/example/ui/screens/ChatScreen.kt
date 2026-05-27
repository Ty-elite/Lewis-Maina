package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatRoom
import com.example.ui.theme.KenyaGreen
import com.example.viewmodel.KenyaRentViewModel

@Composable
fun ChatScreen(
    viewModel: KenyaRentViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val chatRooms by viewModel.chatRooms.collectAsState()
    val activeChatRoomId by viewModel.activeChatRoomId.collectAsState()
    val activeMessages by viewModel.activeRoomMessages.collectAsState()
    val activeRecipientName by viewModel.activeRoomRecipientName.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    val user = currentUser

    if (user == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Login Required to Chat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Manage your rental inquiries and discuss with landlords.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.selectTab("Profile") }, colors = ButtonDefaults.buttonColors(containerColor = KenyaGreen)) {
                    Text("Secure Login Access")
                }
            }
        }
        return
    }

    // Scroll to bottom when new chat messages occur
    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (activeChatRoomId == null) {
            // Renders Active Channel Rooms Conversations
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KenyaGreen)
                        .padding(16.dp)
                ) {
                    Text("KenyaRent Secured Messenger", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (chatRooms.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No discussions active yet. Go in detailed specs to trigger inquiry chat.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(chatRooms) { room ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable { viewModel.selectChatRoom(room.id) },
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = room.recipientName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = room.lastMessageSnip,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(room.lastTimestamp, fontSize = 9.sp, color = KenyaGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Direct chat panel between Tenant and Landlord
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = KenyaGreen,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.clearActiveChatRoom() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(activeRecipientName ?: "Private Discussion", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Secured Messenger active", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                // Chat bubbles lists
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeMessages) { msg ->
                        ChatBubble(message = msg, currentUserId = user.id)
                    }
                }

                // Input message footer send bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Tupigie / discuss rental condition...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KenyaGreen)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendDirectRoomMessage(messageText)
                                    messageText = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = KenyaGreen)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "send message", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity, currentUserId: String) {
    val isMine = message.senderId == currentUserId

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        val bubbleColor = if (isMine) KenyaGreen else MaterialTheme.colorScheme.surfaceVariant
        val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface

        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMine) 12.dp else 0.dp,
                bottomEnd = if (isMine) 0.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.message,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                val formattedTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                Text(
                    text = formattedTime,
                    color = if (isMine) Color.LightGray else Color.Gray,
                    fontSize = 8.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
