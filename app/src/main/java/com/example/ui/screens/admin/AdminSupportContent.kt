package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportContent(
    supportRequests: List<SupportRequest>,
    supportConfig: SupportConfig,
    onSendAdminReply: (requestId: String, message: String) -> Unit,
    onUpdateStatus: (requestId: String, status: SupportStatus, notes: String, assignedStaff: String) -> Unit,
    onUpdateConfig: (SupportConfig) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Support Tickets, 1 = Support Contact Settings
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<SupportCategory?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<SupportStatus?>(null) }
    var activeDetailRequest by remember { mutableStateOf<SupportRequest?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-tabs
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Student Support Tickets") },
                icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = null) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Support Contact Config") },
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) }
            )
        }

        if (selectedSubTab == 0) {
            // Overview KPI Cards
            val newCount = supportRequests.count { it.status == SupportStatus.NEW }
            val openCount = supportRequests.count { it.status == SupportStatus.OPEN }
            val inProgressCount = supportRequests.count { it.status == SupportStatus.IN_PROGRESS }
            val waitingCount = supportRequests.count { it.status == SupportStatus.WAITING_FOR_STUDENT }
            val resolvedCount = supportRequests.count { it.status == SupportStatus.RESOLVED || it.status == SupportStatus.CLOSED }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(title = "New", count = newCount, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                KpiCard(title = "Open", count = openCount, color = Color(0xFF1565C0), modifier = Modifier.weight(1f))
                KpiCard(title = "In Progress", count = inProgressCount, color = Color(0xFFE65100), modifier = Modifier.weight(1f))
                KpiCard(title = "Waiting", count = waitingCount, color = Color(0xFF7B1FA2), modifier = Modifier.weight(1f))
                KpiCard(title = "Resolved", count = resolvedCount, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
            }

            // Search Bar & Filter Chips
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name, email, subject, or request ID...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null && selectedStatusFilter == null,
                        onClick = {
                            selectedCategoryFilter = null
                            selectedStatusFilter = null
                        },
                        label = { Text("All Requests") }
                    )
                }

                items(SupportCategory.entries.toTypedArray()) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text(cat.displayName) }
                    )
                }
            }

            // Filtered Requests List
            val filteredList = supportRequests.filter { req ->
                val matchesSearch = searchQuery.isBlank() ||
                        req.studentName.contains(searchQuery, ignoreCase = true) ||
                        req.studentEmail.contains(searchQuery, ignoreCase = true) ||
                        req.subject.contains(searchQuery, ignoreCase = true) ||
                        req.requestId.contains(searchQuery, ignoreCase = true)

                val matchesCat = selectedCategoryFilter == null || req.category == selectedCategoryFilter
                val matchesStat = selectedStatusFilter == null || req.status == selectedStatusFilter

                matchesSearch && matchesCat && matchesStat
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No support requests match current filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { req ->
                        AdminSupportTicketCard(
                            request = req,
                            onClick = { activeDetailRequest = req }
                        )
                    }
                }
            }
        } else {
            // Support Contact Config Editor
            AdminSupportConfigForm(
                initialConfig = supportConfig,
                onSave = onUpdateConfig
            )
        }
    }

    activeDetailRequest?.let { req ->
        AdminSupportDetailDialog(
            request = req,
            onDismiss = { activeDetailRequest = null },
            onSendReply = { msg ->
                onSendAdminReply(req.requestId, msg)
            },
            onUpdateStatusAndNotes = { newStatus, notes, staff ->
                onUpdateStatus(req.requestId, newStatus, notes, staff)
            }
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        }
    }
}

@Composable
fun AdminSupportTicketCard(
    request: SupportRequest,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "#${request.requestId}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = request.studentName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text(text = "(${request.studentEmail})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AdminStatusBadge(status = request.status)
            }

            Text(text = request.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = request.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category: ${request.category.displayName} · Staff: ${request.assignedStaff}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(Date(request.createdAt)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AdminStatusBadge(status: SupportStatus) {
    val (bgColor, textColor) = when (status) {
        SupportStatus.NEW -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        SupportStatus.OPEN -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        SupportStatus.IN_PROGRESS -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        SupportStatus.WAITING_FOR_STUDENT -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        SupportStatus.RESOLVED -> Color(0xE8E8F5E9) to Color(0xFF2E7D32)
        SupportStatus.CLOSED -> Color(0xFFEEEEEE) to Color(0xFF616161)
    }

    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportDetailDialog(
    request: SupportRequest,
    onDismiss: () -> Unit,
    onSendReply: (String) -> Unit,
    onUpdateStatusAndNotes: (SupportStatus, String, String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var currentStatus by remember { mutableStateOf(request.status) }
    var internalNotes by remember { mutableStateOf(request.internalNotes) }
    var assignedStaff by remember { mutableStateOf(request.assignedStaff) }

    var expandedStatusMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Manage Request #${request.requestId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Student: ${request.studentName} (${request.studentEmail})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Linked Context Info
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Request Context", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Category: ${request.category.displayName}", fontSize = 11.sp)
                        if (!request.relatedUniversity.isNullOrBlank()) {
                            Text(text = "University: ${request.relatedUniversity}", fontSize = 11.sp)
                        }
                        if (!request.relatedApplicationId.isNullOrBlank()) {
                            Text(text = "Application ID: ${request.relatedApplicationId}", fontSize = 11.sp)
                        }
                        if (!request.relatedDocumentContext.isNullOrBlank()) {
                            Text(text = "Document Context: ${request.relatedDocumentContext}", fontSize = 11.sp)
                        }
                    }
                }

                // Initial Student Message
                Text(text = "Subject: ${request.subject}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = request.message, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }

                // History
                if (request.replies.isNotEmpty()) {
                    Text(text = "Replies & Conversation History", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    request.replies.forEach { reply ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (reply.isAdmin) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = reply.senderName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(text = dateFormat.format(Date(reply.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(text = reply.message, fontSize = 12.sp)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Admin Reply Input
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Reply to Student") },
                    placeholder = { Text("Type official reply to student...") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            enabled = replyText.isNotBlank(),
                            onClick = {
                                onSendReply(replyText.trim())
                                replyText = ""
                            }
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send Reply")
                        }
                    }
                )

                HorizontalDivider()

                // Status & Staff Controls
                Text(text = "Update Status & Management", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                ExposedDropdownMenuBox(
                    expanded = expandedStatusMenu,
                    onExpandedChange = { expandedStatusMenu = !expandedStatusMenu }
                ) {
                    OutlinedTextField(
                        value = currentStatus.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatusMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatusMenu,
                        onDismissRequest = { expandedStatusMenu = false }
                    ) {
                        SupportStatus.entries.forEach { stat ->
                            DropdownMenuItem(
                                text = { Text(stat.displayName) },
                                onClick = {
                                    currentStatus = stat
                                    expandedStatusMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = assignedStaff,
                    onValueChange = { assignedStaff = it },
                    label = { Text("Assigned Counselor / Staff") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = internalNotes,
                    onValueChange = { internalNotes = it },
                    label = { Text("Internal Admin Notes (Private)") },
                    placeholder = { Text("Notes visible only to admin team...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateStatusAndNotes(currentStatus, internalNotes, assignedStaff)
                    onDismiss()
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminSupportConfigForm(
    initialConfig: SupportConfig,
    onSave: (SupportConfig) -> Unit
) {
    var email by remember { mutableStateOf(initialConfig.supportEmail) }
    var phone by remember { mutableStateOf(initialConfig.phoneNumber) }
    var whatsapp by remember { mutableStateOf(initialConfig.whatsappNumber) }
    var officeHours by remember { mutableStateOf(initialConfig.officeHours) }
    var linkedin by remember { mutableStateOf(initialConfig.linkedinUrl) }
    var twitter by remember { mutableStateOf(initialConfig.twitterUrl) }
    var instagram by remember { mutableStateOf(initialConfig.instagramUrl) }

    var saveSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Backend Support Contact Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "Update official support contact channels dynamically across the Convoy app without rebuilding the application.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Support Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Support Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("WhatsApp Official Support Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = officeHours,
            onValueChange = { officeHours = it },
            label = { Text("Office Hours") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = linkedin,
            onValueChange = { linkedin = it },
            label = { Text("LinkedIn Page URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = twitter,
            onValueChange = { twitter = it },
            label = { Text("X / Twitter Handle URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = instagram,
            onValueChange = { instagram = it },
            label = { Text("Instagram Page URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val updated = SupportConfig(
                    supportEmail = email.trim(),
                    phoneNumber = phone.trim(),
                    whatsappNumber = whatsapp.trim(),
                    officeHours = officeHours.trim(),
                    linkedinUrl = linkedin.trim(),
                    twitterUrl = twitter.trim(),
                    instagramUrl = instagram.trim(),
                    lastUpdatedBy = "Admin",
                    updatedAt = System.currentTimeMillis()
                )
                onSave(updated)
                saveSuccessMessage = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Contact Configuration")
        }

        if (saveSuccessMessage) {
            Text(
                text = "✓ Support contact details updated successfully!",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
