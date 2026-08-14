package com.example.ui.screens.scholarships

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ScholarshipsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarshipDetailScreen(
    scholarshipId: String,
    viewModel: ScholarshipsViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    viewModel.selectScholarship(scholarshipId)
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val scholarship = uiState.selectedScholarship

    var showRequestInfoDialog by remember { mutableStateOf(false) }
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var requestPhoneInput by remember { mutableStateOf("") }
    var requestMessageInput by remember { mutableStateOf("") }
    var feedbackDialogMessage by remember { mutableStateOf<String?>(null) }

    if (scholarship == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Scholarship details not found.", fontSize = 16.sp)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Scholarship Details", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleSave(scholarship.id, authUiState.currentUser)
                        if (!authUiState.isLoggedIn) {
                            showCreateProfileDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = if (scholarship.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save Scholarship",
                            tint = if (scholarship.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showRequestInfoDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("scholarship_request_info_btn")
                    ) {
                        Text("Request Info", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (!authUiState.isLoggedIn) {
                                showCreateProfileDialog = true
                            } else {
                                viewModel.requestInformation(
                                    scholarship = scholarship,
                                    currentUser = authUiState.currentUser,
                                    message = "Interested in applying for ${scholarship.name}"
                                )
                                feedbackDialogMessage = "Application lead registered! An advisor will assist you with ${scholarship.name}."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_scholarship_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (authUiState.isLoggedIn) "Apply for Award" else "Start Application", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("scholarship_detail_screen")
        ) {
            // Guest Profile CTA Banner
            if (!authUiState.isLoggedIn) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Create Your Free Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Save scholarships & get eligibility alerts.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Button(
                            onClick = { showCreateProfileDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("create_free_profile_scholarship_banner_btn")
                        ) {
                            Text("Create Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // Badges Row: Funding Type & Last Verified Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${scholarship.scholarshipType} ${if (scholarship.isFullyFunded) "• Fully Funded" else "• Partial"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // Visible Last Verified Badge (Required!)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Last Verified: ${scholarship.lastVerified}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Provider
            Text(
                text = scholarship.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Provider: ${scholarship.provider}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (scholarship.university.isNotBlank() && scholarship.university != "Global Partner Institutions") {
                Text(
                    text = "Host Institution: ${scholarship.university}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial Coverage Banner & Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scholarship Financial Benefits",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scholarship.coverageAmount,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (scholarship.tuitionCoverage.isNotBlank()) {
                            Text("• Tuition Fee: ${scholarship.tuitionCoverage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        if (scholarship.stipend.isNotBlank()) {
                            Text("• Living Stipend: ${scholarship.stipend}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        if (scholarship.accommodation.isNotBlank()) {
                            Text("• Accommodation: ${scholarship.accommodation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        if (scholarship.travelAllowance.isNotBlank()) {
                            Text("• Travel Allowance: ${scholarship.travelAllowance}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        if (scholarship.insurance.isNotBlank()) {
                            Text("• Health Insurance: ${scholarship.insurance}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Country, Degree Level & Field of Study
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Host Country: ${scholarship.hostCountry}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Degree Level: ${scholarship.degreeLevel}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Field of Study: ${scholarship.fieldOfStudy}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Application Timeline
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Application Timeline", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Opening Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = scholarship.applicationOpeningDate.ifBlank { "Open Now" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Application Deadline", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = scholarship.deadline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description / Overview
            if (scholarship.description.isNotBlank()) {
                Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = scholarship.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Academic & Language Requirements
            Text("Admission & Academic Requirements", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Academic Criteria", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(scholarship.academicRequirements, fontSize = 13.sp)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("English Language Requirement / MOI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(scholarship.englishRequirements, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Eligibility Criteria
            if (scholarship.eligibilityCriteria.isNotEmpty()) {
                Text("Eligibility Criteria", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                scholarship.eligibilityCriteria.forEach { criteria ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = criteria,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Required Documents
            if (scholarship.requiredDocuments.isNotEmpty()) {
                Text("Required Application Documents", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                scholarship.requiredDocuments.forEach { doc ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = doc,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Dialog: Request Information
        if (showRequestInfoDialog) {
            AlertDialog(
                onDismissRequest = { showRequestInfoDialog = false },
                title = {
                    Text(
                        "Request Scholarship Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            "Submit an inquiry to receive details on eligibility and application guidelines for ${scholarship.name}.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = requestPhoneInput,
                            onValueChange = { requestPhoneInput = it },
                            label = { Text("Phone Number (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("scholarship_info_phone_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = requestMessageInput,
                            onValueChange = { requestMessageInput = it },
                            label = { Text("Message / Question (Optional)") },
                            modifier = Modifier.fillMaxWidth().testTag("scholarship_info_message_input"),
                            minLines = 2
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRequestInfoDialog = false
                            val (success, msg) = viewModel.requestInformation(
                                scholarship = scholarship,
                                currentUser = authUiState.currentUser,
                                phone = requestPhoneInput,
                                message = requestMessageInput
                            )
                            feedbackDialogMessage = msg
                        },
                        modifier = Modifier.testTag("submit_scholarship_info_btn")
                    ) {
                        Text("Submit Inquiry")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRequestInfoDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Create Free Profile
        if (showCreateProfileDialog) {
            AlertDialog(
                onDismissRequest = { showCreateProfileDialog = false },
                title = {
                    Text(
                        "Create Your Free Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            "Join Convoy to save funding opportunities, request counselor assistance, and track deadlines.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = authUiState.registerFullName,
                            onValueChange = { authViewModel.onRegisterFullNameChange(it) },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("scholarship_dialog_register_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = authUiState.registerEmail,
                            onValueChange = { authViewModel.onRegisterEmailChange(it) },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("scholarship_dialog_register_email_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = authUiState.registerPassword,
                            onValueChange = { authViewModel.onRegisterPasswordChange(it) },
                            label = { Text("Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("scholarship_dialog_register_password_input")
                        )

                        authUiState.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.registerStudent {
                                showCreateProfileDialog = false
                                feedbackDialogMessage = "Free Profile created! You are now registered."
                            }
                        },
                        enabled = !authUiState.isLoading,
                        modifier = Modifier.testTag("scholarship_submit_create_profile_btn")
                    ) {
                        Text(if (authUiState.isLoading) "Creating..." else "Create Free Profile")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateProfileDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Feedback
        feedbackDialogMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { feedbackDialogMessage = null },
                title = { Text("Convoy Lead Assistant", fontWeight = FontWeight.Bold) },
                text = { Text(msg, fontSize = 14.sp) },
                confirmButton = {
                    Button(onClick = { feedbackDialogMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
