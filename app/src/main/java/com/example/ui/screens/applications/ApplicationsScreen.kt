package com.example.ui.screens.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import com.example.data.model.Application
import com.example.data.model.ApplicationStatus
import com.example.data.model.AssistanceRequest
import com.example.data.model.AssistanceStatus
import com.example.data.model.AssistanceType
import com.example.data.model.DocumentCategory
import com.example.data.model.GuidanceMessage
import com.example.ui.components.ApplicationCard
import com.example.ui.viewmodel.ApplicationsUiState
import com.example.ui.viewmodel.ApplicationsViewModel

@Composable
fun ApplicationsScreen(
    viewModel: ApplicationsViewModel,
    onNavigateToUniversities: () -> Unit,
    onNavigateToSupport: () -> Unit = {},
    onNavigateToChat: ((Application) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            when (uiState.activeTab) {
                0 -> {
                    FloatingActionButton(
                        onClick = { viewModel.toggleNewAppDialog(true) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("create_application_fab")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "New Application")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Draft App", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                1 -> {
                    FloatingActionButton(
                        onClick = { viewModel.toggleRequestAssistanceDialog(true) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.testTag("request_assistance_fab")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = "Request Assistance")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Assistance", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    FloatingActionButton(
                        onClick = { viewModel.toggleUploadDocDialog(true) },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.testTag("upload_doc_fab")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Upload Document")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Doc", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("applications_screen_root")
        ) {
            // Header & Tab Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Student Application Center",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Track application milestones, offers, documents & optional guided assistance",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TabRow(
                        selectedTabIndex = uiState.activeTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = uiState.activeTab == 0,
                            onClick = { viewModel.setTab(0) },
                            text = { Text("Applications (${uiState.applications.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.testTag("tab_my_applications")
                        )
                        Tab(
                            selected = uiState.activeTab == 1,
                            onClick = { viewModel.setTab(1) },
                            text = { Text("Assistance (${uiState.assistanceRequests.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.testTag("tab_assistance_services")
                        )
                        Tab(
                            selected = uiState.activeTab == 2,
                            onClick = { viewModel.setTab(2) },
                            text = { Text("Documents (${uiState.documents.count { it.isUploaded }})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            modifier = Modifier.testTag("tab_document_locker")
                        )
                    }
                }
            }

            // Tab 0: Applications Dashboard
            if (uiState.activeTab == 0) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Status Filter Chips
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedStatusFilter == null,
                                onClick = { viewModel.filterByStatus(null) },
                                label = { Text("All (${uiState.applications.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }

                        items(ApplicationStatus.entries) { status ->
                            val isSelected = uiState.selectedStatusFilter == status
                            val count = uiState.applications.count { it.status == status }
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.filterByStatus(status) },
                                label = { Text("${status.label} ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    if (uiState.filteredApplications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Applications Found",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Start exploring partner universities and create your first draft.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onNavigateToUniversities) {
                                    Text("Explore Universities")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.testTag("applications_list")
                        ) {
                            items(uiState.filteredApplications, key = { it.id }) { app ->
                                ApplicationCard(
                                    application = app,
                                    onClick = { viewModel.selectApplication(app) },
                                    onChatClick = { onNavigateToChat?.invoke(app) }
                                )
                            }
                        }
                    }
                }
            } else if (uiState.activeTab == 1) {
                // Tab 1: Convoy Application Assistance
                AssistanceServicesTabContent(
                    uiState = uiState,
                    onRequestAssistance = { viewModel.toggleRequestAssistanceDialog(true) },
                    onAddMessage = { reqId, msg -> viewModel.addGuidanceMessage(reqId, msg) }
                )
            } else {
                // Tab 2: Document Locker Section
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("documents_list")
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Convoy Document Vault",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Upload once to automatically attach your official documents to future university applications.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    items(uiState.documents, key = { it.id }) { doc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (doc.isUploaded) Icons.Default.CheckCircle else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (doc.isUploaded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = doc.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${doc.category.label} • ${if (doc.isUploaded) "${doc.fileName} (${doc.fileSize})" else "Pending Upload"}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (doc.isUploaded) {
                                    IconButton(onClick = { viewModel.deleteDocument(doc.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.uploadDocument(doc.title, doc.category) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Upload", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create New Draft Application
    if (uiState.showNewAppDialog) {
        NewApplicationDialog(
            onDismiss = { viewModel.toggleNewAppDialog(false) },
            onCreate = { uni, prog, degree, country, intake ->
                viewModel.createDraftApplication(uni, prog, degree, country, intake)
            }
        )
    }

    // Dialog: Upload Document
    if (uiState.showUploadDocDialog) {
        UploadDocumentDialog(
            onDismiss = { viewModel.toggleUploadDocDialog(false) },
            onUpload = { title, category ->
                viewModel.uploadDocument(title, category)
            }
        )
    }

    // Dialog: Request Application Assistance
    if (uiState.showRequestAssistanceDialog) {
        RequestAssistanceDialog(
            onDismiss = { viewModel.toggleRequestAssistanceDialog(false) },
            onSubmit = { type, uni, prog, notes ->
                viewModel.createAssistanceRequest(type, uni, prog, notes)
            }
        )
    }

    // Dialog: Application Detail View
    if (uiState.selectedApplication != null) {
        StudentApplicationDetailDialog(
            application = uiState.selectedApplication!!,
            onDismiss = { viewModel.selectApplication(null) },
            onSubmit = { viewModel.submitApplication(it) },
            onSaveNotes = { id, notes -> viewModel.saveDraftNotes(id, notes) },
            onWithdraw = { viewModel.withdrawApplication(it) },
            onNavigateToSupport = {
                viewModel.selectApplication(null)
                onNavigateToSupport()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewApplicationDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit
) {
    var universityName by remember { mutableStateOf("University of Oxford") }
    var programName by remember { mutableStateOf("MSc Artificial Intelligence") }
    var degreeLevel by remember { mutableStateOf("Master's Degree") }
    var country by remember { mutableStateOf("United Kingdom") }
    var intakeSeason by remember { mutableStateOf("Fall 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Draft Application", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = universityName,
                    onValueChange = { universityName = it },
                    label = { Text("University Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = programName,
                    onValueChange = { programName = it },
                    label = { Text("Program / Major") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = degreeLevel,
                    onValueChange = { degreeLevel = it },
                    label = { Text("Degree Level") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = intakeSeason,
                    onValueChange = { intakeSeason = it },
                    label = { Text("Target Intake Season") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (universityName.isNotBlank() && programName.isNotBlank()) {
                        onCreate(universityName, programName, degreeLevel, country, intakeSeason)
                    }
                }
            ) {
                Text("Create Application")
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
fun UploadDocumentDialog(
    onDismiss: () -> Unit,
    onUpload: (String, DocumentCategory) -> Unit
) {
    var docTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.TRANSCRIPT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Document to Vault", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = docTitle,
                    onValueChange = { docTitle = it },
                    label = { Text("Document Title (e.g., IELTS Result)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                DocumentCategory.entries.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val title = if (docTitle.isBlank()) selectedCategory.label else docTitle
                    onUpload(title, selectedCategory)
                }
            ) {
                Text("Simulate Upload")
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
fun StudentApplicationDetailDialog(
    application: Application,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    onSaveNotes: (String, String) -> Unit,
    onWithdraw: (String) -> Unit,
    onNavigateToSupport: () -> Unit = {}
) {
    var studentNotes by remember { mutableStateOf(application.studentNotes) }
    var showConfirmation by remember { mutableStateOf(false) }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
                onDismiss()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Application Submitted Successfully",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your application to ${application.universityName} for ${application.programName} (${application.intakeSeason}) has been submitted for Convoy Application Assistance.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Our Study Abroad Guidance team will review your submitted documents, contact you for any additional requirements, and guide you through the official university intake process.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        onDismiss()
                    },
                    modifier = Modifier.testTag("confirm_submission_dialog_btn")
                ) {
                    Text("Got It")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = application.universityName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${application.programName} (${application.degreeLevel})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Info Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Country", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(application.country, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Target Intake", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(application.intakeSeason, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Submitted Date", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(application.submittedDate ?: "Not Submitted", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Application Checklist Overview
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Convoy Application Checklist",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1. University & Program Selected", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("2. Student Profile & Documents Verified", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (application.status != ApplicationStatus.DRAFT) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (application.status != ApplicationStatus.DRAFT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3. Submission for Convoy Study Abroad Guidance", fontSize = 11.sp)
                        }
                    }
                }

                // Current Status
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = application.status.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Next Milestone: ${application.nextMilestone}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Requested Missing Documents (from Admin)
                if (application.requestedDocuments.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Action Required: Requested Documents",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            application.requestedDocuments.forEach { docName ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(docName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Required Documents List
                Text("Required Documents Checklist", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    application.requiredDocuments.forEach { docTitle ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(docTitle, fontSize = 12.sp)
                                }
                                Text("Attached", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Status Timeline History
                if (application.statusHistory.isNotEmpty()) {
                    Text("Application Timeline & Guidance Updates", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        application.statusHistory.reversed().forEach { update ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = update.status.label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = update.timestamp,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (update.note.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = update.note,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Personal Draft Notes
                Text("Personal Notes & Student Reminders", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = studentNotes,
                    onValueChange = { studentNotes = it },
                    placeholder = { Text("Add notes for SOP, references, or reminders...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Button(
                    onClick = { onSaveNotes(application.applicationId, studentNotes) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("save_draft_notes_btn")
                ) {
                    Text("Save Draft Notes", fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToSupport,
                    modifier = Modifier.testTag("contact_application_support_btn")
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contact Support", fontSize = 12.sp)
                }
                if (application.status == ApplicationStatus.DRAFT) {
                    Button(
                        onClick = {
                            onSubmit(application.applicationId)
                            showConfirmation = true
                        },
                        modifier = Modifier.testTag("submit_application_btn")
                    ) {
                        Text("Submit Application")
                    }
                }
                if (application.status == ApplicationStatus.DRAFT ||
                    application.status == ApplicationStatus.SUBMITTED ||
                    application.status == ApplicationStatus.UNDER_REVIEW
                ) {
                    TextButton(
                        onClick = {
                            onWithdraw(application.applicationId)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("withdraw_application_btn")
                    ) {
                        Text("Withdraw App", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_application_dialog_btn")
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AssistanceServicesTabContent(
    uiState: ApplicationsUiState,
    onRequestAssistance: () -> Unit,
    onAddMessage: (String, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("assistance_services_tab_content")
    ) {
        // Clear Service Distinction Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "100% FREE DISCOVERY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "OPTIONAL ASSISTANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Convoy Application Assistance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Finding universities, viewing admission criteria, checking eligibility, and discovering scholarships is ALWAYS completely free on Convoy. For students desiring expert hands-on counseling, our Convoy Application Assistance offers optional 1-on-1 end-to-end guidance.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Disclaimer: Admission & visa decisions are determined solely by universities and immigration authorities. Convoy does not sell or guarantee admissions or visas.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Section: Available Optional Services Catalog
        item {
            Text(
                text = "Available Assistance Services",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select a specialized service to request personalized counselor support",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(items = AssistanceType.entries.toList()) { serviceType ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assistance_service_card_${serviceType.name}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = serviceType.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Optional",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = serviceType.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onRequestAssistance,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("request_service_btn_${serviceType.name}")
                    ) {
                        Text("Request", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section: Active Service Requests
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Assistance Requests (${uiState.assistanceRequests.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onRequestAssistance,
                    modifier = Modifier.testTag("new_request_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Request", fontSize = 11.sp)
                }
            }
        }

        if (uiState.assistanceRequests.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You haven't requested any application assistance services yet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(items = uiState.assistanceRequests, key = { it.requestId }) { req ->
                AssistanceRequestCard(
                    request = req,
                    onAddMessage = { msg -> onAddMessage(req.requestId, msg) }
                )
            }
        }
    }
}

@Composable
private fun AssistanceRequestCard(
    request: AssistanceRequest,
    onAddMessage: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    val statusColor = when (request.status) {
        AssistanceStatus.REQUESTED -> MaterialTheme.colorScheme.secondary
        AssistanceStatus.UNDER_REVIEW -> MaterialTheme.colorScheme.primary
        AssistanceStatus.ASSIGNED -> MaterialTheme.colorScheme.tertiary
        AssistanceStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        AssistanceStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        AssistanceStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assistance_request_card_${request.requestId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.serviceType.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "ID: ${request.requestId}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = request.status.displayName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (request.targetUniversityName.isNotBlank()) {
                Text(
                    text = "Target: ${request.targetUniversityName}${if (request.targetProgramName.isNotBlank()) " • ${request.targetProgramName}" else ""}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Assigned Counselor: ${request.assignedCounselor}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (request.studentNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Request Notes: ${request.studentNotes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Guidance & Message history thread
            if (request.guidanceMessages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Counselor Guidance Thread", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    request.guidanceMessages.forEach { msg ->
                        Surface(
                            color = if (msg.isFromAdmin) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isFromAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = msg.message,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Reply row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Send follow-up note to counselor...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onAddMessage(replyText.trim())
                            replyText = ""
                        }
                    },
                    modifier = Modifier.testTag("send_assistance_msg_${request.requestId}")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestAssistanceDialog(
    onDismiss: () -> Unit,
    onSubmit: (AssistanceType, String, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(AssistanceType.APPLICATION_GUIDANCE) }
    var targetUniInput by remember { mutableStateOf("") }
    var targetProgramInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Request Convoy Application Assistance",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select optional service & share your target university details for counselor review.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Service Dropdown
                Text("Assistance Service", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.title,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        AssistanceType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.title) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Description box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedType.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                OutlinedTextField(
                    value = targetUniInput,
                    onValueChange = { targetUniInput = it },
                    label = { Text("Target University (Optional)") },
                    placeholder = { Text("e.g. University of Oxford") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetProgramInput,
                    onValueChange = { targetProgramInput = it },
                    label = { Text("Target Program (Optional)") },
                    placeholder = { Text("e.g. MSc Computer Science") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes & Specific Questions") },
                    placeholder = { Text("Explain what help you need (e.g. SOP review, visa guidance, shortlisting)...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_assistance_notes_input"),
                    maxLines = 3
                )

                // No direct payment notice
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "No payment required now. A Convoy counselor will review your profile and contact you directly.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(selectedType, targetUniInput.trim(), targetProgramInput.trim(), notesInput.trim())
                },
                modifier = Modifier.testTag("submit_request_assistance_btn")
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_request_assistance_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
