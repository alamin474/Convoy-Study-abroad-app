package com.example.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.SupportUiState
import com.example.ui.viewmodel.SupportViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportHelpScreen(
    viewModel: SupportViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Help & Contact, 1 = My Support Tickets

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionFeedback) {
        uiState.actionFeedback?.let { feedback ->
            snackbarHostState.showSnackbar(feedback)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Help & Support",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Convoy Student Assistance Center",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("support_back_button")
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openNewRequestDialog() },
                        modifier = Modifier.testTag("open_support_dialog")
                    ) {
                        Icon(Icons.Filled.AddComment, contentDescription = "New Support Ticket")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Contact Us & FAQ") },
                    icon = { Icon(Icons.Outlined.HeadsetMic, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("My Support Tickets")
                            if (uiState.userRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = uiState.userRequests.size.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                ContactUsMainContent(
                    uiState = uiState,
                    onCategorySelect = { cat ->
                        viewModel.openNewRequestDialog(category = cat)
                    },
                    onOpenNewDialog = { viewModel.openNewRequestDialog() }
                )
            } else {
                MySupportTicketsContent(
                    uiState = uiState,
                    onSelectRequest = { req -> viewModel.selectRequest(req) },
                    onCategoryFilter = { cat -> viewModel.setCategoryFilter(cat) },
                    onStatusFilter = { stat -> viewModel.setStatusFilter(stat) },
                    onOpenNewDialog = { viewModel.openNewRequestDialog() }
                )
            }
        }
    }

    if (uiState.showNewRequestDialog) {
        NewSupportRequestDialog(
            prefilledCategory = uiState.prefilledCategory,
            prefilledAppId = uiState.prefilledApplicationId,
            prefilledUni = uiState.prefilledUniversity,
            prefilledDocContext = uiState.prefilledDocumentContext,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { viewModel.dismissNewRequestDialog() },
            onSubmit = { cat, subj, msg, appId, uni, doc, att ->
                viewModel.createSupportRequest(
                    category = cat,
                    subject = subj,
                    message = msg,
                    applicationId = appId,
                    university = uni,
                    documentContext = doc,
                    attachmentName = att
                )
            }
        )
    }

    uiState.selectedRequest?.let { req ->
        SupportRequestDetailModal(
            request = req,
            onDismiss = { viewModel.selectRequest(null) },
            onSendReply = { msg ->
                viewModel.addStudentReply(req.requestId, msg)
            },
            onCloseRequest = {
                viewModel.closeStudentRequest(req.requestId)
            }
        )
    }
}

@Composable
fun ContactUsMainContent(
    uiState: SupportUiState,
    onCategorySelect: (SupportCategory) -> Unit,
    onOpenNewDialog: () -> Unit
) {
    val config = uiState.supportConfig

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.SupportAgent,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Column {
                            Text(
                                text = "How can we help you?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Get official guidance from Convoy admissions & technical support team",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onOpenNewDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_message_button")
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send us a Message")
                    }
                }
            }
        }

        // Help Categories
        item {
            Text(
                text = "Select Support Category",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Choose a topic below for specialized assistance",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(SupportCategory.entries.toTypedArray()) { cat ->
            CategoryCard(
                category = cat,
                onClick = { onCategorySelect(cat) }
            )
        }

        // Official Contact Details Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Official Convoy Support Contacts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ContactInfoRow(
                        icon = Icons.Filled.Email,
                        label = "Email Support",
                        value = config.supportEmail
                    )
                    ContactInfoRow(
                        icon = Icons.Filled.Phone,
                        label = "Phone Support",
                        value = config.phoneNumber
                    )
                    ContactInfoRow(
                        icon = Icons.Filled.Chat,
                        label = "WhatsApp Official Line",
                        value = config.whatsappNumber
                    )
                    ContactInfoRow(
                        icon = Icons.Filled.Schedule,
                        label = "Office Hours",
                        value = config.officeHours
                    )
                }
            }
        }

        // Privacy & Document Security Warning Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Student Document Security Guarantee",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your private documents (passports, financial statements, academic transcripts) are securely stored in encrypted vaults and accessed strictly by authorized Convoy counselors. Convoy support staff will never ask for unencrypted passwords or sensitive identity files through informal channels.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: SupportCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val (icon, iconColor) = when (category) {
                SupportCategory.GENERAL -> Icons.Filled.HelpOutline to MaterialTheme.colorScheme.primary
                SupportCategory.UNIVERSITY -> Icons.Filled.School to MaterialTheme.colorScheme.secondary
                SupportCategory.SCHOLARSHIP -> Icons.Filled.WorkspacePremium to MaterialTheme.colorScheme.tertiary
                SupportCategory.APPLICATION -> Icons.Filled.Assignment to MaterialTheme.colorScheme.primary
                SupportCategory.DOCUMENT -> Icons.Filled.Description to MaterialTheme.colorScheme.secondary
                SupportCategory.TECHNICAL -> Icons.Filled.Build to MaterialTheme.colorScheme.error
                SupportCategory.OTHER -> Icons.Filled.MoreHoriz to MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = category.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ContactInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MySupportTicketsContent(
    uiState: SupportUiState,
    onSelectRequest: (SupportRequest) -> Unit,
    onCategoryFilter: (SupportCategory?) -> Unit,
    onStatusFilter: (SupportStatus?) -> Unit,
    onOpenNewDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.categoryFilter == null && uiState.statusFilter == null,
                    onClick = {
                        onCategoryFilter(null)
                        onStatusFilter(null)
                    },
                    label = { Text("All Tickets") }
                )
            }

            items(SupportStatus.entries.toTypedArray()) { stat ->
                FilterChip(
                    selected = uiState.statusFilter == stat,
                    onClick = { onStatusFilter(stat) },
                    label = { Text(stat.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.filteredRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "No Support Tickets Found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Need help with applications or documents? Submit a ticket anytime.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onOpenNewDialog) {
                        Text("Submit Support Request")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.filteredRequests) { req ->
                    SupportTicketCard(
                        request = req,
                        onClick = { onSelectRequest(req) }
                    )
                }
            }
        }
    }
}

@Composable
fun SupportTicketCard(
    request: SupportRequest,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = request.status)
                Text(
                    text = dateFormat.format(Date(request.createdAt)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = request.subject,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = request.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!request.relatedUniversity.isNullOrBlank() || !request.relatedApplicationId.isNullOrBlank() || !request.relatedDocumentContext.isNullOrBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!request.relatedUniversity.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = request.relatedUniversity, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (!request.relatedDocumentContext.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = request.relatedDocumentContext, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            if (request.replies.isNotEmpty()) {
                val lastReply = request.replies.last()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (lastReply.isAdmin) Icons.Filled.SupportAgent else Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Last reply from ${lastReply.senderName}: ${lastReply.message}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: SupportStatus) {
    val (bgColor, textColor) = when (status) {
        SupportStatus.NEW -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        SupportStatus.OPEN -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        SupportStatus.IN_PROGRESS -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        SupportStatus.WAITING_FOR_STUDENT -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        SupportStatus.RESOLVED -> Color(0xE8E8F5E9) to Color(0xFF2E7D32)
        SupportStatus.CLOSED -> Color(0xFFEEEEEE) to Color(0xFF616161)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSupportRequestDialog(
    prefilledCategory: SupportCategory,
    prefilledAppId: String?,
    prefilledUni: String?,
    prefilledDocContext: String?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (SupportCategory, String, String, String?, String?, String?, String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(prefilledCategory) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var university by remember { mutableStateOf(prefilledUni ?: "") }
    var docContext by remember { mutableStateOf(prefilledDocContext ?: "") }
    var attachmentName by remember { mutableStateOf("") }

    var expandedCatMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Support Ticket",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCatMenu,
                    onExpandedChange = { expandedCatMenu = !expandedCatMenu }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        SupportCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject / Request Summary *") },
                    placeholder = { Text("e.g. Question regarding SOP format") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Detailed Message *") },
                    placeholder = { Text("Provide details about your query so Convoy advisors can assist you...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 5
                )

                if (prefilledUni != null || selectedCategory == SupportCategory.UNIVERSITY || selectedCategory == SupportCategory.APPLICATION) {
                    OutlinedTextField(
                        value = university,
                        onValueChange = { university = it },
                        label = { Text("Related University (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (prefilledDocContext != null || selectedCategory == SupportCategory.DOCUMENT) {
                    OutlinedTextField(
                        value = docContext,
                        onValueChange = { docContext = it },
                        label = { Text("Related Document Context (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = attachmentName,
                    onValueChange = { attachmentName = it },
                    label = { Text("Attachment / File Reference (Optional)") },
                    placeholder = { Text("e.g. SOP_Draft_v1.pdf") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = subject.isNotBlank() && message.isNotBlank() && !isSubmitting,
                onClick = {
                    onSubmit(
                        selectedCategory,
                        subject.trim(),
                        message.trim(),
                        prefilledAppId,
                        university.ifBlank { null },
                        docContext.ifBlank { null },
                        attachmentName.ifBlank { null }
                    )
                }
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text("Submit Ticket")
                }
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
fun SupportRequestDetailModal(
    request: SupportRequest,
    onDismiss: () -> Unit,
    onSendReply: (String) -> Unit,
    onCloseRequest: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ticket #${request.requestId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                StatusBadge(status = request.status)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = request.subject,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category: ${request.category.displayName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "·",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(Date(request.createdAt)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                // Initial Message
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = request.studentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = request.message, fontSize = 13.sp)
                    }
                }

                // Replies List
                if (request.replies.isNotEmpty()) {
                    Text(text = "Conversation History", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(request.replies) { reply ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (reply.isAdmin) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = reply.senderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (reply.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = dateFormat.format(Date(reply.timestamp)),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = reply.message, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (request.status != SupportStatus.CLOSED) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Type your reply...") },
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
                }
            }
        },
        confirmButton = {
            if (request.status != SupportStatus.CLOSED) {
                OutlinedButton(onClick = onCloseRequest) {
                    Text("Close Ticket")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
