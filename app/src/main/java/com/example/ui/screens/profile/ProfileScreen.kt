package com.example.ui.screens.profile

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import com.example.data.model.Referral
import com.example.data.model.ReferralStatus
import com.example.data.remote.ConvoyRemoteDataSource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.data.model.StudentProfile
import com.example.ui.screens.info.AboutConvoyModal
import com.example.ui.screens.info.HelpSupportModal
import com.example.ui.screens.info.PrivacyPolicyModal
import com.example.ui.screens.info.TermsConditionsModal
import com.example.ui.viewmodel.AuthMode
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToStudyOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val profile = uiState.studentProfile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen_root"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile?.fullName ?: authUiState.currentUser?.name ?: "Student User",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Student",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = profile?.email ?: authUiState.currentUser?.email ?: "student@convoy.org",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = if (authUiState.isLoggedIn) "Student Account Active" else "Guest Mode",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (authUiState.isLoggedIn) {
                            IconButton(
                                onClick = { viewModel.toggleEditProfileModal(true) },
                                modifier = Modifier.testTag("edit_profile_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Personal Information Card
        item {
            ProfileSectionCard(title = "Personal Information", icon = Icons.Default.Person) {
                ProfileDetailRow(label = "Full Name", value = profile?.fullName ?: "-")
                ProfileDetailRow(label = "Date of Birth", value = profile?.dateOfBirth ?: "-")
                ProfileDetailRow(label = "Nationality", value = profile?.nationality ?: "-")
                ProfileDetailRow(label = "Country of Residence", value = profile?.countryOfResidence ?: "-")
                ProfileDetailRow(label = "Email Address", value = profile?.email ?: "-")
                ProfileDetailRow(label = "Phone Contact", value = profile?.phone ?: "-")
            }
        }

        // 3. Academic Background Card
        item {
            ProfileSectionCard(title = "Academic Information", icon = Icons.Default.School) {
                ProfileDetailRow(label = "Highest Qualification", value = profile?.highestQualification ?: "-")
                ProfileDetailRow(label = "Institution", value = profile?.institution ?: "-")
                ProfileDetailRow(label = "GPA / Grade", value = profile?.gpaScore ?: "-")
                ProfileDetailRow(label = "Graduation Year", value = profile?.graduationYear ?: "-")
                ProfileDetailRow(label = "Intended Degree", value = profile?.intendedDegree ?: "-")
                ProfileDetailRow(label = "Intended Field", value = profile?.intendedField ?: "-")
                ProfileDetailRow(label = "Preferred Countries", value = profile?.preferredCountries?.joinToString(", ") ?: "-")
            }
        }

        // 4. English Qualifications Card
        item {
            ProfileSectionCard(title = "English Qualifications", icon = Icons.Default.Translate) {
                ProfileDetailRow(label = "IELTS", value = profile?.ieltsScore?.ifBlank { "Not Taken" } ?: "-")
                ProfileDetailRow(label = "TOEFL", value = profile?.toeflScore?.ifBlank { "Not Taken" } ?: "-")
                ProfileDetailRow(label = "PTE", value = profile?.pteScore?.ifBlank { "Not Taken" } ?: "-")
                ProfileDetailRow(label = "Other Qualification", value = profile?.otherEnglishQualification?.ifBlank { "None" } ?: "-")
            }
        }

        // 5. Preferences & Budget Card
        item {
            ProfileSectionCard(title = "Study Preferences & Budget 📚", icon = Icons.Default.Tune) {
                ProfileDetailRow(label = "Study Level", value = profile?.selectedStudyLevel?.ifBlank { profile.preferredDegree } ?: "Postgraduate")
                ProfileDetailRow(label = "Selected Subjects", value = profile?.selectedSubjects?.joinToString(", ")?.ifBlank { profile.preferredField } ?: "Computer Science, AI")
                ProfileDetailRow(label = "Annual Budget", value = profile?.budgetRangePerYear ?: "-")
                ProfileDetailRow(label = "Preferred Countries", value = profile?.preferredCountries?.joinToString(", ") ?: "-")

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onNavigateToStudyOnboarding() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_study_preferences_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Study Preferences 📚",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 6. Referral Program ($100 Reward) Card
        item {
            StudentReferralCenterCard(
                currentUserId = authUiState.currentUser?.userId ?: "guest",
                currentUserName = authUiState.currentUser?.name ?: "Student",
                currentUserEmail = authUiState.currentUser?.email ?: ""
            )
        }

        // 6. Account & Authentication Management
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Account Management", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (authUiState.isLoggedIn) {
                        // Password Reset Button
                        SettingsNavRow(
                            title = "Reset Account Password",
                            icon = Icons.Default.LockReset,
                            onClick = { authViewModel.toggleAuthDialog(true, AuthMode.RESET_PASSWORD) },
                            tag = "btn_reset_password"
                        )

                        // Edit Profile Button
                        SettingsNavRow(
                            title = "Edit Profile Details",
                            icon = Icons.Default.Edit,
                            onClick = { viewModel.toggleEditProfileModal(true) },
                            tag = "btn_edit_profile_row"
                        )

                        // Logout Button
                        SettingsNavRow(
                            title = "Log Out of Convoy",
                            icon = Icons.AutoMirrored.Filled.Logout,
                            onClick = {
                                authViewModel.logout()
                                viewModel.refreshProfile()
                            },
                            tag = "btn_logout"
                        )
                    } else {
                        // Login Button
                        SettingsNavRow(
                            title = "Log In to Student Account",
                            icon = Icons.Default.Login,
                            onClick = { authViewModel.toggleAuthDialog(true, AuthMode.LOGIN) },
                            tag = "btn_open_login"
                        )

                        // Register Button
                        SettingsNavRow(
                            title = "Register New Student Account",
                            icon = Icons.Default.AppRegistration,
                            onClick = { authViewModel.toggleAuthDialog(true, AuthMode.REGISTER) },
                            tag = "btn_open_register"
                        )
                    }
                }
            }
        }

        // 7. Platform Settings & Legal
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Settings & Legal Information", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Push Notifications & Application Alerts", fontSize = 13.sp)
                        }

                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsNavRow(
                        title = "About Convoy Platform",
                        icon = Icons.Default.Info,
                        onClick = { viewModel.toggleAboutModal(true) },
                        tag = "nav_about_convoy"
                    )

                    SettingsNavRow(
                        title = "Privacy Policy & Data Security",
                        icon = Icons.Default.Lock,
                        onClick = { viewModel.togglePrivacyModal(true) },
                        tag = "nav_privacy_policy"
                    )

                    SettingsNavRow(
                        title = "Terms & Conditions",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        onClick = { viewModel.toggleTermsModal(true) },
                        tag = "nav_terms_conditions"
                    )

                    SettingsNavRow(
                        title = "Help & Student Support",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        onClick = onNavigateToSupport,
                        tag = "nav_help_support"
                    )

                    SettingsNavRow(
                        title = "Admin Control Center",
                        icon = Icons.Default.Lock,
                        onClick = onNavigateToAdmin,
                        tag = "nav_admin_control_center"
                    )
                }
            }
        }
    }

    // Comprehensive Edit Profile Modal
    if (uiState.showEditProfileModal && profile != null) {
        EditProfileModal(
            profile = profile,
            onDismiss = { viewModel.toggleEditProfileModal(false) },
            onSave = { updated ->
                viewModel.updateProfile(updated)
            }
        )
    }

    // Authentication Dialog (Register, Login, Reset Password)
    if (authUiState.showAuthDialog) {
        AuthDialog(
            uiState = authUiState,
            authViewModel = authViewModel,
            onDismiss = { authViewModel.toggleAuthDialog(false) },
            onSuccess = {
                viewModel.refreshProfile()
            },
            onNavigateToOnboarding = {
                viewModel.refreshProfile()
                onNavigateToStudyOnboarding()
            }
        )
    }

    // Info modals
    if (uiState.showAboutModal) {
        AboutConvoyModal(onDismiss = { viewModel.toggleAboutModal(false) })
    }

    if (uiState.showPrivacyModal) {
        PrivacyPolicyModal(onDismiss = { viewModel.togglePrivacyModal(false) })
    }

    if (uiState.showTermsModal) {
        TermsConditionsModal(onDismiss = { viewModel.toggleTermsModal(false) })
    }

    if (uiState.showSupportModal) {
        HelpSupportModal(onDismiss = { viewModel.toggleSupportModal(false) })
    }
}

@Composable
fun ProfileSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
fun SettingsNavRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
            .testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun EditProfileModal(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onSave: (StudentProfile) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Personal", "Academic", "English", "Preferences")

    // Personal Fields
    var fullName by remember { mutableStateOf(profile.fullName) }
    var dateOfBirth by remember { mutableStateOf(profile.dateOfBirth) }
    var nationality by remember { mutableStateOf(profile.nationality) }
    var countryOfResidence by remember { mutableStateOf(profile.countryOfResidence) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }

    // Academic Fields
    var highestQualification by remember { mutableStateOf(profile.highestQualification) }
    var institution by remember { mutableStateOf(profile.institution) }
    var gpaScore by remember { mutableStateOf(profile.gpaScore) }
    var graduationYear by remember { mutableStateOf(profile.graduationYear) }
    var intendedDegree by remember { mutableStateOf(profile.intendedDegree) }
    var intendedField by remember { mutableStateOf(profile.intendedField) }

    // English Fields
    var ieltsScore by remember { mutableStateOf(profile.ieltsScore) }
    var toeflScore by remember { mutableStateOf(profile.toeflScore) }
    var pteScore by remember { mutableStateOf(profile.pteScore) }
    var otherEnglishQualification by remember { mutableStateOf(profile.otherEnglishQualification) }

    // Preferences Fields
    var budgetRangePerYear by remember { mutableStateOf(profile.budgetRangePerYear) }
    var preferredCountriesStr by remember { mutableStateOf(profile.preferredCountries.joinToString(", ")) }
    var preferredDegree by remember { mutableStateOf(profile.preferredDegree) }
    var preferredField by remember { mutableStateOf(profile.preferredField) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        0 -> { // Personal
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_full_name")
                            )
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                label = { Text("Date of Birth (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_dob")
                            )
                            OutlinedTextField(
                                value = nationality,
                                onValueChange = { nationality = it },
                                label = { Text("Nationality") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_nationality")
                            )
                            OutlinedTextField(
                                value = countryOfResidence,
                                onValueChange = { countryOfResidence = it },
                                label = { Text("Country of Residence") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_residence")
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_email")
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Contact") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_phone")
                            )
                        }
                        1 -> { // Academic
                            OutlinedTextField(
                                value = highestQualification,
                                onValueChange = { highestQualification = it },
                                label = { Text("Highest Qualification") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_highest_qualification")
                            )
                            OutlinedTextField(
                                value = institution,
                                onValueChange = { institution = it },
                                label = { Text("Institution / University") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_institution")
                            )
                            OutlinedTextField(
                                value = gpaScore,
                                onValueChange = { gpaScore = it },
                                label = { Text("GPA / Grade Percentage") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_gpa")
                            )
                            OutlinedTextField(
                                value = graduationYear,
                                onValueChange = { graduationYear = it },
                                label = { Text("Graduation Year") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_grad_year")
                            )
                            OutlinedTextField(
                                value = intendedDegree,
                                onValueChange = { intendedDegree = it },
                                label = { Text("Intended Degree") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_intended_degree")
                            )
                            OutlinedTextField(
                                value = intendedField,
                                onValueChange = { intendedField = it },
                                label = { Text("Intended Field of Study") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_intended_field")
                            )
                        }
                        2 -> { // English
                            OutlinedTextField(
                                value = ieltsScore,
                                onValueChange = { ieltsScore = it },
                                label = { Text("IELTS Band Score") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_ielts")
                            )
                            OutlinedTextField(
                                value = toeflScore,
                                onValueChange = { toeflScore = it },
                                label = { Text("TOEFL Score") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_toefl")
                            )
                            OutlinedTextField(
                                value = pteScore,
                                onValueChange = { pteScore = it },
                                label = { Text("PTE Score") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_pte")
                            )
                            OutlinedTextField(
                                value = otherEnglishQualification,
                                onValueChange = { otherEnglishQualification = it },
                                label = { Text("Other Qualification / MOI") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_other_english")
                            )
                        }
                        3 -> { // Preferences
                            OutlinedTextField(
                                value = budgetRangePerYear,
                                onValueChange = { budgetRangePerYear = it },
                                label = { Text("Annual Budget Range") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_budget")
                            )
                            OutlinedTextField(
                                value = preferredCountriesStr,
                                onValueChange = { preferredCountriesStr = it },
                                label = { Text("Preferred Countries (Comma Separated)") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_preferred_countries")
                            )
                            OutlinedTextField(
                                value = preferredDegree,
                                onValueChange = { preferredDegree = it },
                                label = { Text("Preferred Degree") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_preferred_degree")
                            )
                            OutlinedTextField(
                                value = preferredField,
                                onValueChange = { preferredField = it },
                                label = { Text("Preferred Field") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_preferred_field")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedCountries = preferredCountriesStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(
                        profile.copy(
                            fullName = fullName,
                            dateOfBirth = dateOfBirth,
                            nationality = nationality,
                            countryOfResidence = countryOfResidence,
                            email = email,
                            phone = phone,
                            highestQualification = highestQualification,
                            institution = institution,
                            gpaScore = gpaScore,
                            graduationYear = graduationYear,
                            intendedDegree = intendedDegree,
                            intendedField = intendedField,
                            ieltsScore = ieltsScore,
                            toeflScore = toeflScore,
                            pteScore = pteScore,
                            otherEnglishQualification = otherEnglishQualification,
                            budgetRangePerYear = budgetRangePerYear,
                            preferredCountries = if (parsedCountries.isNotEmpty()) parsedCountries else profile.preferredCountries,
                            preferredDegree = preferredDegree,
                            preferredField = preferredField
                        )
                    )
                },
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AuthDialog(
    uiState: com.example.ui.viewmodel.AuthUiState,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (uiState.authMode) {
                    AuthMode.LOGIN -> "Student Login"
                    AuthMode.REGISTER -> "Register Student Account"
                    AuthMode.RESET_PASSWORD -> "Reset Password"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (uiState.errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (uiState.infoMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uiState.infoMessage,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (uiState.authMode != AuthMode.RESET_PASSWORD) {
                    OutlinedButton(
                        onClick = {
                            authViewModel.loginWithGoogle { needsOnboarding ->
                                onDismiss()
                                if (needsOnboarding) {
                                    onNavigateToOnboarding()
                                } else {
                                    onSuccess()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_signin_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_google_logo),
                            contentDescription = "Google",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = " OR ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                }

                when (uiState.authMode) {
                    AuthMode.LOGIN -> {
                        OutlinedTextField(
                            value = uiState.emailInput,
                            onValueChange = { authViewModel.onEmailChange(it) },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("login_email")
                        )
                        OutlinedTextField(
                            value = uiState.passkeyInput,
                            onValueChange = { authViewModel.onPasskeyChange(it) },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("login_password")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { authViewModel.setAuthMode(AuthMode.RESET_PASSWORD) }) {
                                Text("Forgot Password?", fontSize = 12.sp)
                            }
                            TextButton(onClick = { authViewModel.setAuthMode(AuthMode.REGISTER) }) {
                                Text("New student? Register", fontSize = 12.sp)
                            }
                        }
                    }

                    AuthMode.REGISTER -> {
                        OutlinedTextField(
                            value = uiState.registerFullName,
                            onValueChange = { authViewModel.onRegisterFullNameChange(it) },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("register_full_name")
                        )
                        OutlinedTextField(
                            value = uiState.registerEmail,
                            onValueChange = { authViewModel.onRegisterEmailChange(it) },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("register_email")
                        )
                        OutlinedTextField(
                            value = uiState.registerPassword,
                            onValueChange = { authViewModel.onRegisterPasswordChange(it) },
                            label = { Text("Password (Min 6 chars)") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("register_password")
                        )
                        OutlinedTextField(
                            value = uiState.registerReferralCode,
                            onValueChange = { authViewModel.onRegisterReferralCodeChange(it) },
                            label = { Text("Referral Code (Optional - $100 Reward)") },
                            placeholder = { Text("e.g. ALEX-REF101") },
                            modifier = Modifier.fillMaxWidth().testTag("register_referral_code")
                        )

                        TextButton(
                            onClick = { authViewModel.setAuthMode(AuthMode.LOGIN) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Already registered? Login", fontSize = 12.sp)
                        }
                    }

                    AuthMode.RESET_PASSWORD -> {
                        Text(
                            text = "Enter your registered email address to receive password reset instructions.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = uiState.resetEmailInput,
                            onValueChange = { authViewModel.onResetEmailChange(it) },
                            label = { Text("Registered Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("reset_email")
                        )

                        TextButton(
                            onClick = { authViewModel.setAuthMode(AuthMode.LOGIN) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Back to Login", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (uiState.authMode) {
                        AuthMode.LOGIN -> authViewModel.login(onSuccess)
                        AuthMode.REGISTER -> authViewModel.registerStudent(onSuccess)
                        AuthMode.RESET_PASSWORD -> authViewModel.resetPassword()
                    }
                },
                modifier = Modifier.testTag("auth_confirm_btn")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        when (uiState.authMode) {
                            AuthMode.LOGIN -> "Log In"
                            AuthMode.REGISTER -> "Register"
                            AuthMode.RESET_PASSWORD -> "Send Reset Instructions"
                        }
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StudentReferralCenterCard(
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String
) {
    val remoteDataSource = remember { ConvoyRemoteDataSource() }
    val userReferrals by remoteDataSource.fetchReferrals(currentUserId).collectAsState(initial = emptyList())
    val referralCode = remember(currentUserId) { remoteDataSource.getUserReferralCode(currentUserId) }

    var applyCodeInput by remember { mutableStateOf("") }
    var applyFeedbackMsg by remember { mutableStateOf<String?>(null) }
    var applyIsSuccess by remember { mutableStateOf(false) }
    var showPolicyModal by remember { mutableStateOf(false) }
    var codeCopiedText by remember { mutableStateOf(false) }

    val totalEarned = remember(userReferrals) {
        userReferrals.filter { it.status == ReferralStatus.PAID }.sumOf { it.rewardAmount }
    }
    val activeQualified = remember(userReferrals) {
        userReferrals.count { it.status == ReferralStatus.QUALIFIED || it.status == ReferralStatus.APPROVED }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("referral_center_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Refer & Earn $100 USD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Share Convoy with fellow students and get rewarded",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$100 USD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User's Unique Referral Code Box
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "YOUR UNIQUE REFERRAL CODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = referralCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Row {
                            TextButton(
                                onClick = { codeCopiedText = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (codeCopiedText) "Copied!" else "Copy", fontSize = 12.sp)
                            }

                            TextButton(
                                onClick = { /* Share action */ },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Earned", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "$${totalEarned.toInt()} USD",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Active / Qualified", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = "$activeQualified Referrals",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Apply Referral Code Box
            Text("Got referred by a friend?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = applyCodeInput,
                    onValueChange = {
                        applyCodeInput = it
                        applyFeedbackMsg = null
                    },
                    placeholder = { Text("Enter referral code", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_referral_input"),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (applyCodeInput.isBlank()) return@Button
                        val (success, message) = remoteDataSource.applyReferralCode(
                            code = applyCodeInput,
                            referredUserId = currentUserId,
                            referredName = currentUserName,
                            referredEmail = currentUserEmail
                        )
                        applyIsSuccess = success
                        applyFeedbackMsg = message
                        if (success) applyCodeInput = ""
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("apply_referral_btn")
                ) {
                    Text("Apply", fontSize = 12.sp)
                }
            }

            if (applyFeedbackMsg != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = if (applyIsSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = applyFeedbackMsg!!,
                        fontSize = 11.sp,
                        color = if (applyIsSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // My Referrals History Header
            Text("My Referral History (${userReferrals.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            if (userReferrals.isEmpty()) {
                Text(
                    text = "No active referrals yet. Share your code with friends to start earning $100 rewards!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    userReferrals.forEach { ref ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ref.referredStudentName.ifBlank { ref.referredEmail },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = ref.qualificationDetails,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = when (ref.status) {
                                        ReferralStatus.PAID -> MaterialTheme.colorScheme.primaryContainer
                                        ReferralStatus.APPROVED -> MaterialTheme.colorScheme.tertiaryContainer
                                        ReferralStatus.QUALIFIED -> MaterialTheme.colorScheme.secondaryContainer
                                        ReferralStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = when (ref.status) {
                                            ReferralStatus.PAID -> "Paid ($100)"
                                            ReferralStatus.APPROVED -> "Approved"
                                            ReferralStatus.QUALIFIED -> "Qualified"
                                            ReferralStatus.REJECTED -> "Rejected"
                                            else -> "Pending"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Policy & Terms Link
            OutlinedButton(
                onClick = { showPolicyModal = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("referral_policy_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Referral Program Terms & Eligibility Policy", fontSize = 12.sp)
            }
        }
    }

    if (showPolicyModal) {
        ReferralPolicyModal(onDismiss = { showPolicyModal = false })
    }
}

@Composable
private fun ReferralPolicyModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Policy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Convoy Referral Policy & Terms", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Welcome to the Convoy Student Referral Program. Earn $100 USD for every qualified student you introduce to Convoy.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PolicySection(
                    title = "1. Program Reward Structure",
                    body = "Referrers receive a fixed reward of $100 USD per qualified referral once verified and approved by Convoy Administration."
                )

                PolicySection(
                    title = "2. Qualifying Application Condition",
                    body = "A referral becomes 'Qualified' when the referred student registers using your unique code AND submits a complete university/program application on Convoy."
                )

                PolicySection(
                    title = "3. Mandatory Admin Review & Approval",
                    body = "IMPORTANT: Rewards are NOT automatically paid. Every qualifying referral is thoroughly reviewed by Convoy Admins before payout approval and wire/electronic distribution."
                )

                PolicySection(
                    title = "4. Fraud & Abuse Prevention Rules",
                    body = "• Self-Referrals Prohibited: You cannot refer yourself or use an account belonging to the same individual.\n" +
                            "• Duplicate Prevention: An email address or student user can only be referred once across the Convoy platform.\n" +
                            "• Multiple Rewards Limit: Only one $100 reward is granted per qualifying student submission.\n" +
                            "• Zero-Tolerance Abuse: Any detected referral manipulation, fake emails, or automated abuse will result in instant disqualification and permanent account suspension."
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("I Understand")
            }
        }
    )
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
