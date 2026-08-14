package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMessageType
import com.example.data.model.ConversationStatus
import com.example.data.model.InternalNote
import com.example.data.model.UserRole
import com.example.data.repository.ChatRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminChatContent(
    chatRepository: ChatRepository = remember { ChatRepository() },
    modifier: Modifier = Modifier
) {
    var conversations by remember { mutableStateOf<List<ChatConversation>>(emptyList()) }
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, UNREAD, RESOLVED

    var activeMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var activeInternalNotes by remember { mutableStateOf<List<InternalNote>>(emptyList()) }

    var replyText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var showAssignDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Observe Admin Conversations
    LaunchedEffect(Unit) {
        chatRepository.getAllConversationsForAdmin().collect { list ->
            conversations = list
            if (selectedConversationId == null && list.isNotEmpty()) {
                selectedConversationId = list.first().conversationId
            }
        }
    }

    // Observe active conversation details
    LaunchedEffect(selectedConversationId) {
        val id = selectedConversationId ?: return@LaunchedEffect
        chatRepository.markAsRead(id, "admin_staff")
        chatRepository.getMessages(id).collect { msgs ->
            activeMessages = msgs
        }
    }

    LaunchedEffect(selectedConversationId) {
        val id = selectedConversationId ?: return@LaunchedEffect
        chatRepository.getInternalNotes(id).collect { notes ->
            activeInternalNotes = notes
        }
    }

    val selectedConv = conversations.find { it.conversationId == selectedConversationId }

    val filteredList = conversations.filter { conv ->
        val matchesSearch = searchQuery.isBlank() ||
                conv.studentName.contains(searchQuery, ignoreCase = true) ||
                conv.studentEmail.contains(searchQuery, ignoreCase = true) ||
                conv.title.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "UNREAD" -> conv.unreadCountStaff > 0
            "RESOLVED" -> conv.status == ConversationStatus.RESOLVED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_chat_hub")
    ) {
        // Admin Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Counsellor & Chat Management Hub",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage student inquiries, assign counsellors, and record internal notes",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left List Pane
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search student, email...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_chat_search"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL" to "All", "UNREAD" to "Unread", "RESOLVED" to "Resolved").forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        OutlinedButton(
                            onClick = { selectedFilter = key },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of conversations
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.conversationId }) { conv ->
                        val isSelected = conv.conversationId == selectedConversationId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedConversationId = conv.conversationId }
                                .testTag("admin_conv_card_${conv.conversationId}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = conv.studentName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = conv.studentName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                        if (conv.unreadCountStaff > 0) {
                                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                Text("${conv.unreadCountStaff}", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                    Text(
                                        text = conv.title,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = conv.lastMessageText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right Conversation Details & Reply Pane
            if (selectedConv != null) {
                Card(
                    modifier = Modifier
                        .weight(1.8f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Conversation Header Bar
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${selectedConv.studentName} (${selectedConv.studentEmail})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Assigned Counsellor: ${selectedConv.counsellorName ?: "Unassigned"}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Assign Counsellor Button
                                    Box {
                                        OutlinedButton(
                                            onClick = { showAssignDropdown = true },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Assign", fontSize = 12.sp)
                                        }
                                        DropdownMenu(
                                            expanded = showAssignDropdown,
                                            onDismissRequest = { showAssignDropdown = false }
                                        ) {
                                            chatRepository.getCounsellors().forEach { counsellor ->
                                                DropdownMenuItem(
                                                    text = { Text("${counsellor.name} (${counsellor.roleTitle})") },
                                                    onClick = {
                                                        showAssignDropdown = false
                                                        chatRepository.assignCounsellor(selectedConv.conversationId, counsellor.id, counsellor.name)
                                                        Toast.makeText(context, "Reassigned to ${counsellor.name}", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Resolve/Reopen Toggle
                                    Button(
                                        onClick = {
                                            val newStatus = if (selectedConv.status == ConversationStatus.ACTIVE) ConversationStatus.RESOLVED else ConversationStatus.ACTIVE
                                            chatRepository.updateStatus(selectedConv.conversationId, newStatus)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedConv.status == ConversationStatus.RESOLVED) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (selectedConv.status == ConversationStatus.RESOLVED) "Reopen" else "Mark Resolved", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Internal Notes Banner Section (Staff Private Notes)
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Private Notes",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "INTERNAL COUNSELLOR NOTES (Strictly Hidden from Student)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                if (activeInternalNotes.isNotEmpty()) {
                                    activeInternalNotes.forEach { note ->
                                        Text(
                                            text = "• ${note.authorName}: ${note.content}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No internal staff notes recorded yet.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        placeholder = { Text("Add private staff note...", fontSize = 12.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("admin_internal_note_input"),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (noteText.isNotBlank()) {
                                                chatRepository.addInternalNote(
                                                    InternalNote(
                                                        conversationId = selectedConv.conversationId,
                                                        authorId = "admin_staff",
                                                        authorName = "Admissions Staff",
                                                        content = noteText.trim()
                                                    )
                                                )
                                                noteText = ""
                                                Toast.makeText(context, "Internal note saved", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.NoteAdd, contentDescription = "Add Note", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Message Stream
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeMessages, key = { it.messageId }) { msg ->
                                val isStaff = msg.senderRole == UserRole.COUNSELOR || msg.senderRole == UserRole.ADMIN
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isStaff) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isStaff) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.widthIn(max = 320.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "${msg.senderName} (${msg.senderRole.name})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isStaff) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (msg.attachment != null) {
                                                Text(
                                                    text = "📎 Attachment: ${msg.attachment.fileName} (${msg.attachment.fileSizeFormatted})",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Text(text = msg.text, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Admin Reply Bar
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    placeholder = { Text("Reply to student...", fontSize = 13.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_reply_input"),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (replyText.isNotBlank()) {
                                            val replyMsg = ChatMessage(
                                                conversationId = selectedConv.conversationId,
                                                senderId = "admin_staff",
                                                senderName = selectedConv.counsellorName ?: "Convoy Counsellor",
                                                senderRole = UserRole.COUNSELOR,
                                                text = replyText.trim(),
                                                isDelivered = true,
                                                isRead = false,
                                                createdAtTimestamp = System.currentTimeMillis()
                                            )
                                            chatRepository.sendMessage(replyMsg)
                                            replyText = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("admin_reply_send_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Reply")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
