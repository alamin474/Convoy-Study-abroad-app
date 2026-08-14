package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.security.ConvoySecurityManager
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AdminUiState
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToStudentView: () -> Unit
) {
    val adminUiState by adminViewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    // Refresh auth state whenever screen opens
    LaunchedEffect(authUiState.isAdmin) {
        adminViewModel.checkAuthorizationAndLoadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ADMIN",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Convoy Control Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToStudentView) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Student View")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        adminViewModel.checkAuthorizationAndLoadData()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (!adminUiState.isAuthorized) {
            // Unauthorized Guard Screen
            UnauthorizedAccessCard(
                onLoginClick = onNavigateToLogin,
                onStudentViewClick = onNavigateToStudentView,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            // Authorized Admin System Interface
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Active Session Info Banner
                SessionInfoBanner(
                    email = authUiState.currentUser?.email ?: "admin@convoy.edu",
                    onSwitchToStudent = {
                        authViewModel.loginAsStudentDemo()
                        onNavigateToStudentView()
                    }
                )

                // Scrollable Admin Navigation Tabs (10 Items)
                ScrollableTabRow(
                    selectedTabIndex = AdminTab.entries.indexOf(adminUiState.activeTab),
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    AdminTab.entries.forEach { tab ->
                        Tab(
                            selected = adminUiState.activeTab == tab,
                            onClick = { adminViewModel.selectTab(tab) },
                            text = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (adminUiState.activeTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Main Dynamic Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (adminUiState.activeTab) {
                        AdminTab.DASHBOARD -> AdminOverviewContent(
                            uiState = adminUiState,
                            onTabNavigate = adminViewModel::selectTab
                        )
                        AdminTab.CHAT_HUB -> AdminChatContent()
                        AdminTab.ANALYTICS -> ConvoyAnalyticsDashboardContent(
                            metrics = adminUiState.analyticsMetrics,
                            selectedFilter = adminUiState.selectedDateFilter,
                            onFilterSelected = { filter, start, end ->
                                adminViewModel.setAnalyticsDateFilter(filter, start, end)
                            },
                            onTabNavigate = adminViewModel::selectTab
                        )
                        AdminTab.UNIVERSITIES -> AdminUniversitiesContent(
                            universities = adminUiState.universities,
                            onSaveUniversity = { adminViewModel.saveUniversity(it) },
                            onDeleteUniversity = { adminViewModel.deleteUniversity(it) },
                            onTogglePublishStatus = { adminViewModel.toggleUniversityPublishStatus(it) }
                        )
                        AdminTab.REQUIREMENTS -> AdminRequirementsContent(
                            requirements = adminUiState.requirements,
                            universities = adminUiState.universities,
                            onSaveRequirement = { adminViewModel.saveRequirement(it) },
                            onDeleteRequirement = { adminViewModel.deleteRequirement(it) },
                            onTogglePublishStatus = { adminViewModel.toggleRequirementPublishStatus(it) }
                        )
                        AdminTab.SUPPORT -> AdminSupportContent(
                            supportRequests = adminUiState.supportRequests,
                            supportConfig = adminUiState.supportConfig,
                            onSendAdminReply = { id, msg -> adminViewModel.addSupportReplyByAdmin(id, msg) },
                            onUpdateStatus = { id, status, notes, staff -> adminViewModel.updateSupportStatusByAdmin(id, status, notes, staff) },
                            onUpdateConfig = { config -> adminViewModel.updateSupportConfigByAdmin(config) }
                        )
                        AdminTab.ASSISTANCE -> AdminAssistanceContent(
                            assistanceRequests = adminUiState.assistanceRequests,
                            onUpdateStatus = { id, status, counselor, notes ->
                                adminViewModel.updateAssistanceStatus(id, status, counselor, notes)
                            },
                            onAddMessage = { id, text ->
                                adminViewModel.addAdminGuidanceMessage(id, text)
                            }
                        )
                        AdminTab.PARTNERS -> AdminPartnersContent(
                            partners = adminUiState.partners,
                            onSavePartner = { adminViewModel.savePartner(it) },
                            onDeletePartner = { adminViewModel.deletePartner(it) },
                            onUpdateStatus = { id, status -> adminViewModel.updatePartnerStatus(id, status) }
                        )
                        AdminTab.SPONSORED_LISTINGS -> AdminSponsoredListingsContent(
                            sponsoredListings = adminUiState.sponsoredListings,
                            universities = adminUiState.universities,
                            scholarships = adminUiState.scholarships,
                            onSaveListing = { adminViewModel.saveSponsoredListing(it) },
                            onDeleteListing = { adminViewModel.deleteSponsoredListing(it) },
                            onUpdateStatus = { id, status -> adminViewModel.updateSponsoredListingStatus(id, status) }
                        )
                        AdminTab.SCHOLARSHIPS -> AdminScholarshipsContent(
                            scholarships = adminUiState.scholarships,
                            onSaveScholarship = { adminViewModel.saveScholarship(it) },
                            onDeleteScholarship = { adminViewModel.deleteScholarship(it) },
                            onTogglePublishStatus = { adminViewModel.toggleScholarshipPublishStatus(it) }
                        )
                        AdminTab.COUNTRIES -> AdminCountriesContent(countries = adminUiState.countries)
                        AdminTab.APPLICATIONS -> AdminApplicationsContent(
                            applications = adminUiState.applications,
                            partners = adminUiState.partners,
                            selectedApplication = adminUiState.selectedApplication,
                            onSelectApplication = adminViewModel::selectApplication,
                            onUpdateStatus = { id, status, note -> adminViewModel.updateApplicationStatus(id, status, note) },
                            onUpdateInternalNotes = { id, notes -> adminViewModel.updateApplicationInternalNotes(id, notes) },
                            onRequestMissingDocuments = { id, docs -> adminViewModel.requestMissingDocuments(id, docs) },
                            onUpdateAttribution = { appId, pId, pName, src, cEligible, cStatus, cAmt ->
                                adminViewModel.updateApplicationAttribution(appId, pId, pName, src, cEligible, cStatus, cAmt)
                            }
                        )
                        AdminTab.DOCUMENTS -> AdminDocumentsContent(documents = adminUiState.documents)
                        AdminTab.STUDENTS -> AdminStudentsContent(students = adminUiState.students)
                        AdminTab.REFERRALS -> AdminReferralsContent(
                            referrals = adminUiState.referrals,
                            selectedReferral = adminUiState.selectedReferral,
                            onSelectReferral = adminViewModel::selectReferral,
                            onUpdateStatus = { id, status, note -> adminViewModel.updateReferralStatus(id, status, note) }
                        )
                        AdminTab.LEADS -> AdminLeadsContent(
                            leads = adminUiState.leads,
                            selectedLead = adminUiState.selectedLead,
                            onSelectLead = adminViewModel::selectLead,
                            onUpdateStatus = { id, status, notes -> adminViewModel.updateLeadStatus(id, status, notes) }
                        )
                        AdminTab.ANNOUNCEMENTS -> AdminAnnouncementsContent(announcements = adminUiState.announcements)
                        AdminTab.SETTINGS -> AdminSettingsContent(authUiState = authUiState, onLogout = {
                            authViewModel.logout()
                            adminViewModel.checkAuthorizationAndLoadData()
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionInfoBanner(
    email: String,
    onSwitchToStudent: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Admin Session: $email",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            TextButton(
                onClick = onSwitchToStudent,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Student View",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun UnauthorizedAccessCard(
    onLoginClick: () -> Unit,
    onStudentViewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Access Denied",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You are currently signed in as a Student User. Administrative functions require authorized Convoy Administrator credentials.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Key, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Authenticate as Administrator")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onStudentViewClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Return to Student Portal")
        }
    }
}

@Composable
private fun AdminOverviewContent(
    uiState: AdminUiState,
    onTabNavigate: (AdminTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Dashboard Overview",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Real-time metrics, system stats & pending queue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Total Universities",
                    value = uiState.overviewMetrics.totalUniversities.toString(),
                    icon = Icons.Filled.School,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onTabNavigate(AdminTab.UNIVERSITIES) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Scholarships",
                    value = uiState.overviewMetrics.totalScholarships.toString(),
                    icon = Icons.Filled.WorkspacePremium,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { onTabNavigate(AdminTab.SCHOLARSHIPS) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Total Students",
                    value = uiState.overviewMetrics.totalStudents.toString(),
                    icon = Icons.Filled.People,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { onTabNavigate(AdminTab.STUDENTS) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Applications",
                    value = uiState.overviewMetrics.totalApplications.toString(),
                    icon = Icons.Filled.Assignment,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onTabNavigate(AdminTab.APPLICATIONS) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Pending Applications",
                    value = uiState.overviewMetrics.pendingApplications.toString(),
                    icon = Icons.Filled.PendingActions,
                    containerColor = if (uiState.overviewMetrics.pendingApplications > 0) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (uiState.overviewMetrics.pendingApplications > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onTabNavigate(AdminTab.APPLICATIONS) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending Documents",
                    value = uiState.overviewMetrics.pendingDocuments.toString(),
                    icon = Icons.Filled.FolderSpecial,
                    containerColor = if (uiState.overviewMetrics.pendingDocuments > 0) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (uiState.overviewMetrics.pendingDocuments > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onTabNavigate(AdminTab.DOCUMENTS) },
                    modifier = Modifier.weight(1f)
                )
            }

            MetricCard(
                title = "Total Referrals",
                value = uiState.overviewMetrics.totalReferrals.toString(),
                icon = Icons.Filled.CardGiftcard,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onTabNavigate(AdminTab.REFERRALS) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Analytics & Monetization Overview Section
        AdminAnalyticsSection(
            uiState = uiState,
            onTabNavigate = onTabNavigate
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity Feed
        Text(
            text = "Recent Activity Log",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val activities = uiState.overviewMetrics.recentActivities
                activities.forEachIndexed { index, activity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (activity.type) {
                                    ActivityType.APPLICATION_SUBMITTED -> Icons.Filled.Assignment
                                    ActivityType.DOCUMENT_UPLOADED -> Icons.Filled.FileUpload
                                    ActivityType.UNIVERSITY_ADDED -> Icons.Filled.School
                                    ActivityType.SCHOLARSHIP_UPDATED -> Icons.Filled.WorkspacePremium
                                    ActivityType.STUDENT_REGISTERED -> Icons.Filled.PersonAdd
                                    ActivityType.REFERRAL_CONVERTED -> Icons.Filled.CardGiftcard
                                    ActivityType.ANNOUNCEMENT_POSTED -> Icons.Filled.Campaign
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = activity.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = activity.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = activity.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (index < activities.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAnalyticsSection(
    uiState: AdminUiState,
    onTabNavigate: (AdminTab) -> Unit
) {
    val totalUsers = uiState.students.size
    val activeUsers = uiState.students.size
    val totalLeads = uiState.leads.size
    val convertedLeads = uiState.leads.count { it.status == LeadStatus.CONVERTED }
    val leadConversionRate = if (totalLeads > 0) (convertedLeads.toDouble() / totalLeads * 100) else 0.0

    val totalApplications = uiState.applications.size
    val userAppConversionRate = if (totalUsers > 0) (totalApplications.toDouble() / totalUsers * 100) else 0.0

    val referralConversions = uiState.referrals.count { 
        it.status == ReferralStatus.PAID || it.status == ReferralStatus.APPROVED 
    }

    val serviceRequests = uiState.assistanceRequests.size
    val sponsoredListingsCount = uiState.sponsoredListings.size

    val totalCommissionSum = uiState.applications
        .filter { it.commissionEligible && it.commissionAmount != null }
        .sumOf { app ->
            app.commissionAmount?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0
        }

    val referralRewardsDisbursed = uiState.referrals
        .filter { it.status == ReferralStatus.PAID }
        .sumOf { it.rewardAmount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_analytics_view"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System Analytics & Business Intelligence",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "REAL DATA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Users & Leads Analytics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsMetricItem(
                    title = "Total Users",
                    value = totalUsers.toString(),
                    subtitle = "Active Users: $activeUsers",
                    icon = Icons.Filled.People,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabNavigate(AdminTab.STUDENTS) }
                )
                AnalyticsMetricItem(
                    title = "Leads",
                    value = totalLeads.toString(),
                    subtitle = "Lead Conv: ${"%.1f".format(leadConversionRate)}%",
                    icon = Icons.Filled.FilterAlt,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabNavigate(AdminTab.LEADS) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Applications & Referral Conversions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsMetricItem(
                    title = "Applications",
                    value = totalApplications.toString(),
                    subtitle = "App Conv: ${"%.1f".format(userAppConversionRate)}%",
                    icon = Icons.Filled.Assignment,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabNavigate(AdminTab.APPLICATIONS) }
                )
                AnalyticsMetricItem(
                    title = "Referral Conversions",
                    value = referralConversions.toString(),
                    subtitle = "Total Refs: ${uiState.referrals.size}",
                    icon = Icons.Filled.CardGiftcard,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabNavigate(AdminTab.REFERRALS) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service Requests & Sponsored Listings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsMetricItem(
                    title = "Service Requests",
                    value = serviceRequests.toString(),
                    subtitle = "Assistance Queue",
                    icon = Icons.Filled.SupportAgent,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabNavigate(AdminTab.ASSISTANCE) }
                )
                AnalyticsMetricItem(
                    title = "Sponsored Listings",
                    value = sponsoredListingsCount.toString(),
                    subtitle = "Active Ads/Features",
                    icon = Icons.Filled.Campaign,
                    modifier = Modifier.weight(1f),
                    onClick = { onTabNavigate(AdminTab.SPONSORED_LISTINGS) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // Verified Revenue Fields
            Text(
                text = "Tracked Revenue & Financial Summary",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tracked Commissions",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${"%.2f".format(totalCommissionSum)} USD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Disbursed Referral Rewards",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${"%.2f".format(referralRewardsDisbursed)} USD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMetricItem(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AdminUniversitiesContent(
    universities: List<University>,
    onSaveUniversity: (University) -> Unit,
    onDeleteUniversity: (String) -> Unit,
    onTogglePublishStatus: (University) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<EntityStatus?>(null) }
    var selectedCountryFilter by remember { mutableStateOf("All") }

    var isFormOpen by remember { mutableStateOf(false) }
    var universityToEdit by remember { mutableStateOf<University?>(null) }
    var universityToDelete by remember { mutableStateOf<University?>(null) }

    val filteredList = remember(universities, searchQuery, selectedStatusFilter, selectedCountryFilter) {
        universities.filter { uni ->
            val matchesSearch = searchQuery.isBlank() ||
                    uni.name.contains(searchQuery, ignoreCase = true) ||
                    uni.city.contains(searchQuery, ignoreCase = true) ||
                    uni.country.contains(searchQuery, ignoreCase = true) ||
                    uni.programs.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesStatus = selectedStatusFilter == null || uni.status == selectedStatusFilter
            val matchesCountry = selectedCountryFilter == "All" || uni.country.equals(selectedCountryFilter, ignoreCase = true)

            matchesSearch && matchesStatus && matchesCountry
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Row with Title & Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "University Management",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${filteredList.size} of ${universities.size} universities displayed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    universityToEdit = null
                    isFormOpen = true
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add University", fontWeight = FontWeight.Bold)
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search university name, city, country, or programs...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                label = { Text("All Status") }
            )
            FilterChip(
                selected = selectedStatusFilter == EntityStatus.PUBLISHED,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == EntityStatus.PUBLISHED) null else EntityStatus.PUBLISHED
                },
                label = { Text("Published") }
            )
            FilterChip(
                selected = selectedStatusFilter == EntityStatus.DRAFT,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == EntityStatus.DRAFT) null else EntityStatus.DRAFT
                },
                label = { Text("Draft") }
            )
            FilterChip(
                selected = selectedStatusFilter == EntityStatus.ARCHIVED,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == EntityStatus.ARCHIVED) null else EntityStatus.ARCHIVED
                },
                label = { Text("Archived") }
            )
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No universities found matching your filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.universityId }) { uni ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = uni.flagEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = uni.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${uni.city}, ${uni.country} • ${uni.universityType} • Rank #${uni.ranking}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (uni.status) {
                                        EntityStatus.PUBLISHED -> Color(0xFFE8F5E9)
                                        EntityStatus.DRAFT -> Color(0xFFFFF3E0)
                                        EntityStatus.ARCHIVED -> Color(0xFFECEFF1)
                                    }
                                ) {
                                    Text(
                                        text = uni.status.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (uni.status) {
                                            EntityStatus.PUBLISHED -> Color(0xFF2E7D32)
                                            EntityStatus.DRAFT -> Color(0xFFE65100)
                                            EntityStatus.ARCHIVED -> Color(0xFF455A64)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tuition: ${uni.tuitionFee}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "App Fee: ${uni.applicationFee}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (uni.programs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Programs: ${uni.programs.take(3).joinToString(", ")}${if (uni.programs.size > 3) " +${uni.programs.size - 3} more" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            // Action Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onTogglePublishStatus(uni) },
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (uni.status == EntityStatus.PUBLISHED) "Unpublish" else "Publish",
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        universityToEdit = uni
                                        isFormOpen = true
                                    },
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { universityToDelete = uni },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Form Dialog
    if (isFormOpen) {
        UniversityFormDialog(
            university = universityToEdit,
            onDismiss = { isFormOpen = false },
            onSave = { updatedUni ->
                onSaveUniversity(updatedUni)
                isFormOpen = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (universityToDelete != null) {
        AlertDialog(
            onDismissRequest = { universityToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${universityToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        universityToDelete?.let { onDeleteUniversity(it.universityId) }
                        universityToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { universityToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniversityFormDialog(
    university: University?,
    onDismiss: () -> Unit,
    onSave: (University) -> Unit
) {
    var name by remember { mutableStateOf(university?.name ?: "") }
    var country by remember { mutableStateOf(university?.country ?: "") }
    var city by remember { mutableStateOf(university?.city ?: "") }
    var universityType by remember { mutableStateOf(university?.universityType ?: "Public Research") }
    var rankingStr by remember { mutableStateOf(university?.ranking?.toString() ?: "50") }
    var tuitionFee by remember { mutableStateOf(university?.tuitionFee ?: "$20,000 USD / year") }
    var applicationFee by remember { mutableStateOf(university?.applicationFee ?: "$75 USD") }
    var programsStr by remember { mutableStateOf(university?.programs?.joinToString(", ") ?: "") }
    var degreeLevelsStr by remember { mutableStateOf(university?.degreeLevels?.joinToString(", ") ?: "Bachelor's, Master's, Doctorate") }
    var intakesStr by remember { mutableStateOf(university?.intakes?.joinToString(", ") ?: "Fall 2026, Spring 2027") }
    var admissionReqsStr by remember { mutableStateOf(university?.admissionRequirements?.joinToString("\n") ?: "") }
    var ieltsReq by remember { mutableStateOf(university?.ieltsRequirement ?: "6.5 Overall") }
    var toeflReq by remember { mutableStateOf(university?.toeflRequirement ?: "90 iBT") }
    var pteReq by remember { mutableStateOf(university?.pteRequirement ?: "62 Academic") }
    var englishWaiver by remember { mutableStateOf(university?.englishWaiverInfo ?: "MOI accepted if prior degree completed in English.") }
    var scholarshipsStr by remember { mutableStateOf(university?.scholarships?.joinToString("\n") ?: "") }
    var description by remember { mutableStateOf(university?.description ?: "") }
    var campusImagesStr by remember { mutableStateOf(university?.campusImages?.joinToString(", ") ?: "") }
    var officialWebsite by remember { mutableStateOf(university?.officialWebsite ?: "https://www.university.edu") }
    var applicationUrl by remember { mutableStateOf(university?.applicationUrl ?: "https://apply.convoy.edu") }
    var lastVerified by remember { mutableStateOf(university?.lastVerified ?: "2026-02-01") }
    var status by remember { mutableStateOf(university?.status ?: EntityStatus.PUBLISHED) }
    var flagEmoji by remember { mutableStateOf(university?.flagEmoji ?: "🌐") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (university == null) "Add New University" else "Edit University",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("University Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = universityType,
                        onValueChange = { universityType = it },
                        label = { Text("University Type") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rankingStr,
                        onValueChange = { rankingStr = it },
                        label = { Text("Ranking #") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = flagEmoji,
                        onValueChange = { flagEmoji = it },
                        label = { Text("Flag") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tuitionFee,
                        onValueChange = { tuitionFee = it },
                        label = { Text("Tuition Fee") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = applicationFee,
                        onValueChange = { applicationFee = it },
                        label = { Text("App Fee") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = programsStr,
                    onValueChange = { programsStr = it },
                    label = { Text("Programs (Comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = degreeLevelsStr,
                    onValueChange = { degreeLevelsStr = it },
                    label = { Text("Degree Levels Offered") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = intakesStr,
                    onValueChange = { intakesStr = it },
                    label = { Text("Intakes (Comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ieltsReq,
                    onValueChange = { ieltsReq = it },
                    label = { Text("IELTS Requirement") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = toeflReq,
                    onValueChange = { toeflReq = it },
                    label = { Text("TOEFL Requirement") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pteReq,
                    onValueChange = { pteReq = it },
                    label = { Text("PTE Requirement") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = englishWaiver,
                    onValueChange = { englishWaiver = it },
                    label = { Text("English Waiver / MOI Info") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = admissionReqsStr,
                    onValueChange = { admissionReqsStr = it },
                    label = { Text("Admission Requirements (One per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = scholarshipsStr,
                    onValueChange = { scholarshipsStr = it },
                    label = { Text("Scholarship Information (One per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Campus Description / Overview") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = officialWebsite,
                    onValueChange = { officialWebsite = it },
                    label = { Text("Official Website URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = applicationUrl,
                    onValueChange = { applicationUrl = it },
                    label = { Text("Application URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = lastVerified,
                    onValueChange = { lastVerified = it },
                    label = { Text("Last Verified Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Status", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == EntityStatus.PUBLISHED,
                        onClick = { status = EntityStatus.PUBLISHED },
                        label = { Text("PUBLISHED") }
                    )
                    FilterChip(
                        selected = status == EntityStatus.DRAFT,
                        onClick = { status = EntityStatus.DRAFT },
                        label = { Text("DRAFT") }
                    )
                    FilterChip(
                        selected = status == EntityStatus.ARCHIVED,
                        onClick = { status = EntityStatus.ARCHIVED },
                        label = { Text("ARCHIVED") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && country.isNotBlank() && city.isNotBlank(),
                onClick = {
                    val uniId = university?.universityId ?: "uni_${System.currentTimeMillis()}"
                    val programs = programsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val degreeLevels = degreeLevelsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val intakes = intakesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val admissionReqs = admissionReqsStr.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val scholarships = scholarshipsStr.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val images = campusImagesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                    val savedUni = University(
                        universityId = uniId,
                        name = name.trim(),
                        country = country.trim(),
                        city = city.trim(),
                        universityType = universityType.trim(),
                        ranking = rankingStr.toIntOrNull() ?: 50,
                        tuitionFee = tuitionFee.trim(),
                        applicationFee = applicationFee.trim(),
                        description = description.trim(),
                        programs = if (programs.isEmpty()) listOf("General Program") else programs,
                        degreeLevels = if (degreeLevels.isEmpty()) listOf("Bachelor's", "Master's") else degreeLevels,
                        intakes = if (intakes.isEmpty()) listOf("Fall 2026") else intakes,
                        admissionRequirements = admissionReqs,
                        englishRequirements = "IELTS: $ieltsReq / TOEFL: $toeflReq",
                        ieltsRequirement = ieltsReq.trim(),
                        toeflRequirement = toeflReq.trim(),
                        pteRequirement = pteReq.trim(),
                        englishWaiverInfo = englishWaiver.trim(),
                        scholarships = scholarships,
                        campusImages = if (images.isEmpty()) listOf("https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800&q=80") else images,
                        officialWebsite = officialWebsite.trim(),
                        applicationUrl = applicationUrl.trim(),
                        lastVerified = lastVerified.trim(),
                        status = status,
                        flagEmoji = flagEmoji.ifBlank { "🌐" },
                        logoUrl = university?.logoUrl ?: "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=300&q=80",
                        bannerUrl = university?.bannerUrl ?: "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800&q=80"
                    )

                    onSave(savedUni)
                }
            ) {
                Text("Save University")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AdminScholarshipsContent(
    scholarships: List<Scholarship>,
    onSaveScholarship: (Scholarship) -> Unit,
    onDeleteScholarship: (String) -> Unit,
    onTogglePublishStatus: (Scholarship) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<EntityStatus?>(null) }
    var selectedTypeFilter by remember { mutableStateOf("All") }

    var isFormOpen by remember { mutableStateOf(false) }
    var scholarshipToEdit by remember { mutableStateOf<Scholarship?>(null) }
    var scholarshipToDelete by remember { mutableStateOf<Scholarship?>(null) }

    val filteredList = remember(scholarships, searchQuery, selectedStatusFilter, selectedTypeFilter) {
        scholarships.filter { sch ->
            val matchesSearch = searchQuery.isBlank() ||
                    sch.name.contains(searchQuery, ignoreCase = true) ||
                    sch.provider.contains(searchQuery, ignoreCase = true) ||
                    sch.country.contains(searchQuery, ignoreCase = true) ||
                    sch.university.contains(searchQuery, ignoreCase = true) ||
                    sch.fieldOfStudy.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatusFilter == null || sch.status == selectedStatusFilter
            val matchesType = selectedTypeFilter == "All" || sch.scholarshipType.equals(selectedTypeFilter, ignoreCase = true)

            matchesSearch && matchesStatus && matchesType
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Row with Title & Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Scholarship Management",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${filteredList.size} of ${scholarships.size} scholarships displayed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    scholarshipToEdit = null
                    isFormOpen = true
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Scholarship", fontWeight = FontWeight.Bold)
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search scholarship name, provider, country, university...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                label = { Text("All Status") }
            )
            FilterChip(
                selected = selectedStatusFilter == EntityStatus.PUBLISHED,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == EntityStatus.PUBLISHED) null else EntityStatus.PUBLISHED
                },
                label = { Text("Published") }
            )
            FilterChip(
                selected = selectedStatusFilter == EntityStatus.DRAFT,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == EntityStatus.DRAFT) null else EntityStatus.DRAFT
                },
                label = { Text("Draft") }
            )
            FilterChip(
                selected = selectedStatusFilter == EntityStatus.ARCHIVED,
                onClick = {
                    selectedStatusFilter = if (selectedStatusFilter == EntityStatus.ARCHIVED) null else EntityStatus.ARCHIVED
                },
                label = { Text("Archived") }
            )
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No scholarships found matching your filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.scholarshipId }) { sch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sch.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Provider: ${sch.provider} • Host: ${sch.country}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${sch.degreeLevel} • ${sch.fieldOfStudy}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when (sch.status) {
                                        EntityStatus.PUBLISHED -> Color(0xFFE8F5E9)
                                        EntityStatus.DRAFT -> Color(0xFFFFF3E0)
                                        EntityStatus.ARCHIVED -> Color(0xFFECEFF1)
                                    }
                                ) {
                                    Text(
                                        text = sch.status.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (sch.status) {
                                            EntityStatus.PUBLISHED -> Color(0xFF2E7D32)
                                            EntityStatus.DRAFT -> Color(0xFFE65100)
                                            EntityStatus.ARCHIVED -> Color(0xFF455A64)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Type: ${sch.scholarshipType} ${if (sch.isFullyFunded) "(Fully Funded)" else "(Partial)"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Deadline: ${sch.deadline}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Coverage: ${sch.coverageAmount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            // Action Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onTogglePublishStatus(sch) },
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (sch.status == EntityStatus.PUBLISHED) "Unpublish" else "Publish",
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        scholarshipToEdit = sch
                                        isFormOpen = true
                                    },
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { scholarshipToDelete = sch },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Form Dialog
    if (isFormOpen) {
        ScholarshipFormDialog(
            scholarship = scholarshipToEdit,
            onDismiss = { isFormOpen = false },
            onSave = { updatedSch ->
                onSaveScholarship(updatedSch)
                isFormOpen = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (scholarshipToDelete != null) {
        AlertDialog(
            onDismissRequest = { scholarshipToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${scholarshipToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scholarshipToDelete?.let { onDeleteScholarship(it.scholarshipId) }
                        scholarshipToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { scholarshipToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScholarshipFormDialog(
    scholarship: Scholarship?,
    onDismiss: () -> Unit,
    onSave: (Scholarship) -> Unit
) {
    var name by remember { mutableStateOf(scholarship?.name ?: "") }
    var providerName by remember { mutableStateOf(scholarship?.providerName ?: "") }
    var country by remember { mutableStateOf(scholarship?.country ?: "") }
    var university by remember { mutableStateOf(scholarship?.university ?: "") }
    var degreeLevel by remember { mutableStateOf(scholarship?.degreeLevel ?: "Master's Degree") }
    var fieldOfStudy by remember { mutableStateOf(scholarship?.fieldOfStudy ?: "All Fields of Study") }
    var scholarshipType by remember { mutableStateOf(scholarship?.scholarshipType ?: "Fully Funded") }
    var isFullyFunded by remember { mutableStateOf(scholarship?.isFullyFunded ?: true) }
    var tuitionCoverage by remember { mutableStateOf(scholarship?.tuitionCoverage ?: "100% Tuition Fee Covered") }
    var stipend by remember { mutableStateOf(scholarship?.stipend ?: "£1,400 / month living stipend") }
    var accommodation by remember { mutableStateOf(scholarship?.accommodation ?: "University Residence / Monthly Housing Allowance") }
    var travelAllowance by remember { mutableStateOf(scholarship?.travelAllowance ?: "Roundtrip Economy Flights Included") }
    var insurance by remember { mutableStateOf(scholarship?.insurance ?: "Full Health Insurance Covered") }
    var fundingDetails by remember { mutableStateOf(scholarship?.fundingDetails ?: "Full Tuition + Monthly Stipend + Travel Allowance") }
    var eligibilityStr by remember { mutableStateOf(scholarship?.eligibility?.joinToString("\n") ?: "") }
    var academicRequirements by remember { mutableStateOf(scholarship?.academicRequirements ?: "Minimum 3.0 CGPA equivalent") }
    var englishRequirements by remember { mutableStateOf(scholarship?.englishRequirements ?: "IELTS 6.5+ / TOEFL 90+ iBT") }
    var requiredDocsStr by remember { mutableStateOf(scholarship?.requiredDocuments?.joinToString("\n") ?: "") }
    var deadline by remember { mutableStateOf(scholarship?.deadline ?: "03 November 2026") }
    var applicationOpeningDate by remember { mutableStateOf(scholarship?.applicationOpeningDate ?: "01 August 2026") }
    var officialWebsite by remember { mutableStateOf(scholarship?.officialWebsite ?: "https://www.scholarships.org") }
    var applicationUrl by remember { mutableStateOf(scholarship?.applicationUrl ?: "https://apply.convoy.edu/scholarships") }
    var lastVerified by remember { mutableStateOf(scholarship?.lastVerified ?: "2026-02-01") }
    var description by remember { mutableStateOf(scholarship?.description ?: "") }
    var status by remember { mutableStateOf(scholarship?.status ?: EntityStatus.PUBLISHED) }

    val scholarshipTypesList = listOf("Fully Funded", "Partial", "Merit", "Need-Based", "Government", "University", "Tuition Waiver")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (scholarship == null) "Add New Scholarship" else "Edit Scholarship",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Scholarship Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = providerName,
                        onValueChange = { providerName = it },
                        label = { Text("Provider Organization *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Host Country *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = university,
                        onValueChange = { university = it },
                        label = { Text("Host University") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = degreeLevel,
                        onValueChange = { degreeLevel = it },
                        label = { Text("Degree Level") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = fieldOfStudy,
                    onValueChange = { fieldOfStudy = it },
                    label = { Text("Field of Study") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Scholarship Type", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(scholarshipTypesList) { type ->
                        FilterChip(
                            selected = scholarshipType.equals(type, ignoreCase = true),
                            onClick = {
                                scholarshipType = type
                                if (type.equals("Fully Funded", ignoreCase = true)) {
                                    isFullyFunded = true
                                } else if (type.equals("Partial", ignoreCase = true) || type.equals("Tuition Waiver", ignoreCase = true)) {
                                    isFullyFunded = false
                                }
                            },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = isFullyFunded,
                        onClick = { isFullyFunded = !isFullyFunded },
                        label = { Text(if (isFullyFunded) "Fully Funded" else "Partial Funding") }
                    )
                }

                OutlinedTextField(
                    value = tuitionCoverage,
                    onValueChange = { tuitionCoverage = it },
                    label = { Text("Tuition Coverage") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stipend,
                    onValueChange = { stipend = it },
                    label = { Text("Stipend / Allowance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = accommodation,
                    onValueChange = { accommodation = it },
                    label = { Text("Accommodation Support") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = travelAllowance,
                    onValueChange = { travelAllowance = it },
                    label = { Text("Travel Allowance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = insurance,
                    onValueChange = { insurance = it },
                    label = { Text("Health Insurance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fundingDetails,
                    onValueChange = { fundingDetails = it },
                    label = { Text("Funding Overview Banner Text") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = academicRequirements,
                    onValueChange = { academicRequirements = it },
                    label = { Text("Academic Requirements") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = englishRequirements,
                    onValueChange = { englishRequirements = it },
                    label = { Text("English Language Requirements") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = eligibilityStr,
                    onValueChange = { eligibilityStr = it },
                    label = { Text("Eligibility Criteria (One per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = requiredDocsStr,
                    onValueChange = { requiredDocsStr = it },
                    label = { Text("Required Application Documents (One per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = applicationOpeningDate,
                        onValueChange = { applicationOpeningDate = it },
                        label = { Text("Opening Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline Date *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = officialWebsite,
                    onValueChange = { officialWebsite = it },
                    label = { Text("Official Website URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = applicationUrl,
                    onValueChange = { applicationUrl = it },
                    label = { Text("Official Application URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = lastVerified,
                    onValueChange = { lastVerified = it },
                    label = { Text("Last Verified Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Overview Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("Status", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == EntityStatus.PUBLISHED,
                        onClick = { status = EntityStatus.PUBLISHED },
                        label = { Text("PUBLISHED") }
                    )
                    FilterChip(
                        selected = status == EntityStatus.DRAFT,
                        onClick = { status = EntityStatus.DRAFT },
                        label = { Text("DRAFT") }
                    )
                    FilterChip(
                        selected = status == EntityStatus.ARCHIVED,
                        onClick = { status = EntityStatus.ARCHIVED },
                        label = { Text("ARCHIVED") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && country.isNotBlank() && deadline.isNotBlank() && lastVerified.isNotBlank(),
                onClick = {
                    val schId = scholarship?.scholarshipId ?: "sch_${System.currentTimeMillis()}"
                    val eligibilityList = eligibilityStr.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val docsList = requiredDocsStr.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

                    val savedSch = Scholarship(
                        scholarshipId = schId,
                        name = name.trim(),
                        providerName = providerName.trim().ifBlank { "Global Foundation" },
                        country = country.trim(),
                        university = university.trim().ifBlank { "Host Institution" },
                        degreeLevel = degreeLevel.trim(),
                        fieldOfStudy = fieldOfStudy.trim(),
                        scholarshipType = scholarshipType.trim(),
                        isFullyFunded = isFullyFunded,
                        tuitionCoverage = tuitionCoverage.trim(),
                        stipend = stipend.trim(),
                        accommodation = accommodation.trim(),
                        travelAllowance = travelAllowance.trim(),
                        insurance = insurance.trim(),
                        fundingDetails = fundingDetails.trim(),
                        eligibility = eligibilityList,
                        academicRequirements = academicRequirements.trim(),
                        englishRequirements = englishRequirements.trim(),
                        requiredDocuments = docsList,
                        deadline = deadline.trim(),
                        applicationOpeningDate = applicationOpeningDate.trim(),
                        officialWebsite = officialWebsite.trim(),
                        applicationUrl = applicationUrl.trim(),
                        lastVerified = lastVerified.trim(),
                        status = status,
                        description = description.trim(),
                        logoUrl = scholarship?.logoUrl ?: "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=300&q=80",
                        isFeatured = scholarship?.isFeatured ?: false,
                        isSaved = scholarship?.isSaved ?: false
                    )

                    onSave(savedSch)
                }
            ) {
                Text("Save Scholarship")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AdminCountriesContent(countries: List<Country>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Study Destinations (${countries.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        items(countries) { country ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = country.flagEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${country.universityCount} Partner Universities • Avg Tuition: ${country.avgTuitionPerYear}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminApplicationsContent(
    applications: List<Application>,
    partners: List<Partner> = emptyList(),
    selectedApplication: Application?,
    onSelectApplication: (Application?) -> Unit,
    onUpdateStatus: (String, ApplicationStatus, String) -> Unit,
    onUpdateInternalNotes: (String, String) -> Unit,
    onRequestMissingDocuments: (String, List<String>) -> Unit,
    onUpdateAttribution: (appId: String, partnerId: String?, partnerName: String?, source: String, commissionEligible: Boolean, commissionStatus: CommissionStatus, commissionAmount: String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<ApplicationStatus?>(null) }

    val filteredApps = remember(applications, searchQuery, statusFilter) {
        applications.filter { app ->
            val matchesQuery = searchQuery.isBlank() ||
                app.universityName.contains(searchQuery, ignoreCase = true) ||
                app.programName.contains(searchQuery, ignoreCase = true) ||
                app.userId.contains(searchQuery, ignoreCase = true) ||
                app.country.contains(searchQuery, ignoreCase = true)
            val matchesStatus = statusFilter == null || app.status == statusFilter
            matchesQuery && matchesStatus
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Student Applications Manager (${applications.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by university, program, country, or student ID...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All (${applications.size})") }
                )
            }
            items(ApplicationStatus.entries) { status ->
                val count = applications.count { it.status == status }
                FilterChip(
                    selected = statusFilter == status,
                    onClick = { statusFilter = status },
                    label = { Text("${status.label} ($count)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredApps, key = { it.id }) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectApplication(app) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.universityName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            StatusBadge(status = app.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${app.programName} (${app.degreeLevel}) • ${app.intakeSeason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Student ID: ${app.userId} • Country: ${app.country}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (app.internalNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Internal Note: ${app.internalNotes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedApplication != null) {
        AdminApplicationDetailDialog(
            application = selectedApplication,
            partners = partners,
            onDismiss = { onSelectApplication(null) },
            onUpdateStatus = { status, note ->
                onUpdateStatus(selectedApplication.applicationId, status, note)
            },
            onUpdateInternalNotes = { notes ->
                onUpdateInternalNotes(selectedApplication.applicationId, notes)
            },
            onRequestMissingDocuments = { docs ->
                onRequestMissingDocuments(selectedApplication.applicationId, docs)
            },
            onUpdateAttribution = { pId, pName, src, cEligible, cStatus, cAmt ->
                onUpdateAttribution(selectedApplication.applicationId, pId, pName, src, cEligible, cStatus, cAmt)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminApplicationDetailDialog(
    application: Application,
    partners: List<Partner> = emptyList(),
    onDismiss: () -> Unit,
    onUpdateStatus: (ApplicationStatus, String) -> Unit,
    onUpdateInternalNotes: (String) -> Unit,
    onRequestMissingDocuments: (List<String>) -> Unit,
    onUpdateAttribution: (partnerId: String?, partnerName: String?, source: String, commissionEligible: Boolean, commissionStatus: CommissionStatus, commissionAmount: String?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(application.status) }
    var statusNote by remember { mutableStateOf("") }
    var internalNotes by remember { mutableStateOf(application.internalNotes) }
    var missingDocInput by remember { mutableStateOf("") }
    var isStatusExpanded by remember { mutableStateOf(false) }

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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${application.programName} (${application.degreeLevel})",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Student ID: ${application.userId} • Country: ${application.country}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                // Section 1: Change Status
                Text("Update Application Status", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                ExposedDropdownMenuBox(
                    expanded = isStatusExpanded,
                    onExpandedChange = { isStatusExpanded = !isStatusExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedStatus.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select New Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isStatusExpanded,
                        onDismissRequest = { isStatusExpanded = false }
                    ) {
                        ApplicationStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label) },
                                onClick = {
                                    selectedStatus = status
                                    isStatusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = statusNote,
                    onValueChange = { statusNote = it },
                    label = { Text("Status Change Note for Student") },
                    placeholder = { Text("e.g. Conditional offer issued, please review terms.") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        onUpdateStatus(selectedStatus, statusNote)
                        statusNote = ""
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Update Status")
                }

                Divider()

                // Section 2: Internal Admin Notes
                Text("Internal Admin Notes (Private)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                OutlinedTextField(
                    value = internalNotes,
                    onValueChange = { internalNotes = it },
                    placeholder = { Text("Private notes visible only to Convoy Admin team...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Button(
                    onClick = { onUpdateInternalNotes(internalNotes) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Internal Notes")
                }

                Divider()

                // Section 3.5: Contact Student / Send Application Guidance
                Text("Contact Student / Guidance Message", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                var guidanceMessageInput by remember { mutableStateOf("") }
                var showGuidanceSentFeedback by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = guidanceMessageInput,
                    onValueChange = { guidanceMessageInput = it },
                    placeholder = { Text("Enter guidance note or update for student (e.g. Please check email for offer letter)...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showGuidanceSentFeedback) {
                        Text("Guidance note sent to student!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = {
                            if (guidanceMessageInput.isNotBlank()) {
                                onUpdateStatus(selectedStatus, "Application Guidance: ${guidanceMessageInput.trim()}")
                                guidanceMessageInput = ""
                                showGuidanceSentFeedback = true
                            }
                        },
                        modifier = Modifier.testTag("admin_send_guidance_btn")
                    ) {
                        Text("Send Guidance")
                    }
                }

                Divider()

                // Section 3: Request Missing Documents
                Text("Request Missing Documents", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                OutlinedTextField(
                    value = missingDocInput,
                    onValueChange = { missingDocInput = it },
                    placeholder = { Text("Enter document title (e.g. Bank Financial Statement)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (missingDocInput.isNotBlank()) {
                            onRequestMissingDocuments(listOf(missingDocInput.trim()))
                            missingDocInput = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Request Document")
                }

                if (application.requestedDocuments.isNotEmpty()) {
                    Text("Currently Requested:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    application.requestedDocuments.forEach { doc ->
                        Text("• $doc", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }

                Divider()

                // Section 3.8: Application Attribution & Commission (Admin Only)
                Text("Application Attribution & Commission (Admin Only)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                var attrPartnerName by remember { mutableStateOf(application.partnerName ?: "") }
                var attrSource by remember { mutableStateOf(application.applicationSource) }
                var attrEligible by remember { mutableStateOf(application.commissionEligible) }
                var attrCommStatus by remember { mutableStateOf(application.commissionStatus) }
                var attrAmount by remember { mutableStateOf(application.commissionAmount ?: "") }
                var commDropdownExpanded by remember { mutableStateOf(false) }

                Text("Partner Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = attrPartnerName,
                    onValueChange = { attrPartnerName = it },
                    placeholder = { Text("e.g. University of Melbourne, Global Network") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Application Source", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = attrSource,
                    onValueChange = { attrSource = it },
                    placeholder = { Text("e.g. Direct Organic, Partner Referral, Agency") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = attrEligible,
                        onCheckedChange = { attrEligible = it },
                        modifier = Modifier.testTag("commission_eligible_checkbox")
                    )
                    Text("Commission Eligible", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                if (attrEligible) {
                    Text("Commission Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = commDropdownExpanded,
                        onExpandedChange = { commDropdownExpanded = !commDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = attrCommStatus.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = commDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = commDropdownExpanded,
                            onDismissRequest = { commDropdownExpanded = false }
                        ) {
                            CommissionStatus.entries.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.displayName) },
                                    onClick = {
                                        attrCommStatus = st
                                        commDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Commission Info / Amount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = attrAmount,
                        onValueChange = { attrAmount = it },
                        placeholder = { Text("e.g. $1,500 USD or 15% Year 1 tuition") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        val selectedP = partners.find { it.name.equals(attrPartnerName, ignoreCase = true) }
                        onUpdateAttribution(
                            selectedP?.partnerId,
                            attrPartnerName.ifBlank { null },
                            attrSource.ifBlank { "Direct Organic" },
                            attrEligible,
                            if (attrEligible) attrCommStatus else CommissionStatus.NOT_APPLICABLE,
                            attrAmount.ifBlank { null }
                        )
                    },
                    modifier = Modifier.align(Alignment.End).testTag("save_attribution_btn")
                ) {
                    Text("Save Attribution & Commission")
                }

                Divider()

                // Section 4: History
                if (application.statusHistory.isNotEmpty()) {
                    Text("Status Update History", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    application.statusHistory.reversed().forEach { update ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(update.status.label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(update.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                if (update.note.isNotBlank()) {
                                    Text(update.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun StatusBadge(status: ApplicationStatus) {
    val (bgColor, textColor) = when (status) {
        ApplicationStatus.OFFER_RECEIVED, ApplicationStatus.COMPLETED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SUBMITTED -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        ApplicationStatus.DOCUMENTS_REQUIRED -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        ApplicationStatus.PROCESSING, ApplicationStatus.APPLIED, ApplicationStatus.VISA_PROCESSING -> Color(0xFFEDE7F6) to Color(0xFF512DA8)
        ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        ApplicationStatus.DRAFT -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AdminDocumentsContent(documents: List<StudentDocument>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Sensitive Student Documents Queue (${documents.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        items(documents) { doc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (doc.isUploaded) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = if (doc.isUploaded) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Category: ${doc.category.label} • Student ID: ${doc.userId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (doc.isUploaded) "Uploaded" else "Pending",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (doc.isUploaded) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminStudentsContent(students: List<User>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Registered Student Accounts (${students.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        items(students) { student ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${student.email} • ${student.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Country: ${student.nationality}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReferralsContent(
    referrals: List<Referral>,
    selectedReferral: Referral?,
    onSelectReferral: (Referral?) -> Unit,
    onUpdateStatus: (String, ReferralStatus, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<ReferralStatus?>(null) }

    val filteredReferrals = remember(referrals, searchQuery, selectedFilter) {
        referrals.filter { ref ->
            val matchesQuery = searchQuery.isBlank() ||
                    ref.referralId.contains(searchQuery, ignoreCase = true) ||
                    ref.referrerName.contains(searchQuery, ignoreCase = true) ||
                    ref.referrerUserId.contains(searchQuery, ignoreCase = true) ||
                    ref.referralCode.contains(searchQuery, ignoreCase = true) ||
                    ref.referredStudentName.contains(searchQuery, ignoreCase = true) ||
                    ref.referredEmail.contains(searchQuery, ignoreCase = true)

            val matchesFilter = selectedFilter == null || ref.status == selectedFilter
            matchesQuery && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title & Description
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Referral Program Management (${referrals.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Review qualifying referrals, verify application submissions & approve $100 payouts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by ID, Referrer, Student, Email, or Code...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${referrals.size})") }
                )
            }
            items(ReferralStatus.entries.toTypedArray()) { status ->
                val count = referrals.count { it.status == status }
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = if (selectedFilter == status) null else status },
                    label = { Text("${status.label} ($count)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredReferrals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No referrals found matching search criteria",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredReferrals) { ref ->
                    AdminReferralCard(
                        referral = ref,
                        onClick = { onSelectReferral(ref) }
                    )
                }
            }
        }
    }

    if (selectedReferral != null) {
        AdminReferralDetailDialog(
            referral = selectedReferral,
            onDismiss = { onSelectReferral(null) },
            onUpdateStatus = { newStatus, adminNote ->
                onUpdateStatus(selectedReferral.referralId, newStatus, adminNote)
            }
        )
    }
}

@Composable
private fun AdminReferralCard(
    referral: Referral,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = referral.referralId,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = referral.rewardAmountFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                ReferralStatusBadge(status = referral.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Two-column layout for Referrer & Referred Student
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Referrer Box
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "REFERRER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = referral.referrerName.ifBlank { referral.referrerUserId },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Code: ${referral.referralCode.ifBlank { "N/A" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Referred Student Box
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "REFERRED STUDENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = referral.referredStudentName.ifBlank { referral.referredEmail.substringBefore("@") },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = referral.referredEmail,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Qualification & Payment Status Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Status: ${referral.paymentStatus}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (referral.status) {
                        ReferralStatus.PAID -> MaterialTheme.colorScheme.primary
                        ReferralStatus.APPROVED -> MaterialTheme.colorScheme.tertiary
                        ReferralStatus.QUALIFIED -> MaterialTheme.colorScheme.secondary
                        ReferralStatus.REJECTED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    },
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Date: ${referral.createdAt.take(10)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (referral.qualificationDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qualification: ${referral.qualificationDetails}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ReferralStatusBadge(status: ReferralStatus) {
    val (bgColor, textColor, label) = when (status) {
        ReferralStatus.PENDING -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Pending")
        ReferralStatus.QUALIFIED -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "Qualified")
        ReferralStatus.APPROVED -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, "Approved")
        ReferralStatus.PAID -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "Paid ($100)")
        ReferralStatus.REJECTED -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "Rejected")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AdminReferralDetailDialog(
    referral: Referral,
    onDismiss: () -> Unit,
    onUpdateStatus: (ReferralStatus, String) -> Unit
) {
    var adminNoteInput by remember(referral) { mutableStateOf(referral.adminNote) }
    var selectedNewStatus by remember(referral) { mutableStateOf(referral.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Referral Review & Payout",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "ID: ${referral.referralId} • Reward: ${referral.rewardAmountFormatted}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ReferralStatusBadge(status = referral.status)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Referrer & Referred Details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("REFERRER DETAILS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Name: ${referral.referrerName.ifBlank { "N/A" }}")
                        Text("User ID: ${referral.referrerUserId}")
                        Text("Referral Code Used: ${referral.referralCode}")
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("REFERRED STUDENT DETAILS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Name: ${referral.referredStudentName.ifBlank { "N/A" }}")
                        Text("Email: ${referral.referredEmail}")
                        Text("Student ID: ${referral.referredUserId ?: "Awaiting account association"}")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Qualification & Application Link
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("QUALIFICATION AUDIT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Qualifying Application ID: ${referral.qualifyingApplicationId ?: "None (Application Not Submitted Yet)"}")
                        Text("Details: ${referral.qualificationDetails}")
                        Text("Current Payment Status: ${referral.paymentStatus}")
                        Text("Created Date: ${referral.createdAt}")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Policy Note Warning
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Mandate: Referrals are never automatically paid. Admin must verify qualifying application before approving or releasing $100 payout.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Selector
                Text("Update Referral Status:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReferralStatus.entries.forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNewStatus = status }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedNewStatus == status,
                                onClick = { selectedNewStatus = status }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = status.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = when (status) {
                                        ReferralStatus.PENDING -> "Waiting for referred student to submit qualifying application"
                                        ReferralStatus.QUALIFIED -> "Qualifying application submitted. Awaiting admin review."
                                        ReferralStatus.APPROVED -> "Referral approved. Ready for finance payout disbursement."
                                        ReferralStatus.PAID -> "Mark $100 reward as paid and disbursed to referrer."
                                        ReferralStatus.REJECTED -> "Reject referral (Self-referral, abuse, or duplicate)."
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Admin Notes
                OutlinedTextField(
                    value = adminNoteInput,
                    onValueChange = { adminNoteInput = it },
                    label = { Text("Internal Admin Note") },
                    placeholder = { Text("e.g., Verified submission for Munich MSc; Wire transfer ref #88921") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateStatus(selectedNewStatus, adminNoteInput)
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
private fun AdminAnnouncementsContent(announcements: List<Announcement>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Broadcast Announcements (${announcements.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        items(announcements) { ann ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = ann.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = ann.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Audience: ${ann.targetAudience} • Date: ${ann.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsContent(
    authUiState: com.example.ui.viewmodel.AuthUiState,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Platform Settings & Security Inspection",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Session Details",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "User ID: ${authUiState.currentUser?.userId ?: "N/A"}")
                Text(text = "Email: ${authUiState.currentUser?.email ?: "N/A"}")
                Text(text = "Role: ${authUiState.currentUser?.role?.name ?: "N/A"}")
                Text(text = "Authorization Token: Active Session (ConvoySecurityManager)")

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invalidate Session & Logout")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Firestore Security Rules Specification",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = ConvoySecurityManager.FIRESTORE_SECURITY_RULES,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminLeadsContent(
    leads: List<Lead>,
    selectedLead: Lead?,
    onSelectLead: (Lead?) -> Unit,
    onUpdateStatus: (String, LeadStatus, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<LeadStatus?>(null) }

    val filteredLeads = remember(leads, searchQuery, statusFilter) {
        leads.filter { lead ->
            val matchesSearch = searchQuery.isBlank() ||
                    lead.leadId.contains(searchQuery, ignoreCase = true) ||
                    lead.studentName.contains(searchQuery, ignoreCase = true) ||
                    lead.studentEmail.contains(searchQuery, ignoreCase = true) ||
                    (lead.universityName?.contains(searchQuery, ignoreCase = true) == true) ||
                    (lead.scholarshipName?.contains(searchQuery, ignoreCase = true) == true) ||
                    lead.source.contains(searchQuery, ignoreCase = true)

            val matchesStatus = statusFilter == null || lead.status == statusFilter
            matchesSearch && matchesStatus
        }
    }

    val totalLeads = leads.size
    val newLeads = leads.count { it.status == LeadStatus.NEW }
    val qualifiedLeads = leads.count { it.status == LeadStatus.QUALIFIED }
    val convertedLeads = leads.count { it.status == LeadStatus.CONVERTED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_leads_content")
    ) {
        // Summary Header Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Leads",
                value = totalLeads.toString(),
                icon = Icons.Default.Groups,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { statusFilter = null },
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "New",
                value = newLeads.toString(),
                icon = Icons.Default.NewReleases,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = { statusFilter = LeadStatus.NEW },
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Qualified",
                value = qualifiedLeads.toString(),
                icon = Icons.Default.CheckCircle,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = { statusFilter = LeadStatus.QUALIFIED },
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Converted",
                value = convertedLeads.toString(),
                icon = Icons.Default.MonetizationOn,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { statusFilter = LeadStatus.CONVERTED },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search leads...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_leads_search"),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                }
                items(LeadStatus.entries.toTypedArray()) { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = if (statusFilter == status) null else status },
                        label = { Text(status.label, fontSize = 11.sp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLeads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No lead records found",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLeads, key = { it.leadId }) { lead ->
                    LeadRecordRowItem(
                        lead = lead,
                        onSelect = { onSelectLead(lead) }
                    )
                }
            }
        }
    }

    if (selectedLead != null) {
        var selectedStatus by remember(selectedLead) { mutableStateOf(selectedLead.status) }
        var currentNoteInput by remember(selectedLead) { mutableStateOf(selectedLead.notes) }

        AlertDialog(
            onDismissRequest = { onSelectLead(null) },
            title = {
                Text(
                    text = "Manage Lead (${selectedLead.leadId})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Student: ${selectedLead.studentName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Email: ${selectedLead.studentEmail}", fontSize = 11.sp)
                            if (selectedLead.studentPhone.isNotBlank()) {
                                Text("Phone: ${selectedLead.studentPhone}", fontSize = 11.sp)
                            }
                            Text("Target: ${selectedLead.universityName ?: selectedLead.scholarshipName ?: "General"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Country: ${selectedLead.country}", fontSize = 11.sp)
                            Text("Source: ${selectedLead.source} • Date: ${selectedLead.date}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Text("Update Lead Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LeadStatus.entries.toTypedArray()) { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                label = { Text(status.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = currentNoteInput,
                        onValueChange = { currentNoteInput = it },
                        label = { Text("Lead Notes / Advisor Comments", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(selectedLead.leadId, selectedStatus, currentNoteInput)
                        onSelectLead(null)
                    }
                ) {
                    Text("Save Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { onSelectLead(null) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LeadRecordRowItem(
    lead: Lead,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("lead_item_${lead.leadId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lead.studentName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = lead.source,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${lead.universityName ?: lead.scholarshipName ?: "General"} (${lead.country})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "${lead.studentEmail} • ${lead.date}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Surface(
                color = when (lead.status) {
                    LeadStatus.NEW -> MaterialTheme.colorScheme.errorContainer
                    LeadStatus.CONTACTED -> MaterialTheme.colorScheme.secondaryContainer
                    LeadStatus.QUALIFIED -> MaterialTheme.colorScheme.tertiaryContainer
                    LeadStatus.CONVERTED -> MaterialTheme.colorScheme.primaryContainer
                    LeadStatus.CLOSED -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = lead.status.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (lead.status) {
                        LeadStatus.NEW -> MaterialTheme.colorScheme.onErrorContainer
                        LeadStatus.CONTACTED -> MaterialTheme.colorScheme.onSecondaryContainer
                        LeadStatus.QUALIFIED -> MaterialTheme.colorScheme.onTertiaryContainer
                        LeadStatus.CONVERTED -> MaterialTheme.colorScheme.onPrimaryContainer
                        LeadStatus.CLOSED -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminRequirementsContent(
    requirements: List<UniversityRequirement>,
    universities: List<University>,
    onSaveRequirement: (UniversityRequirement) -> Unit,
    onDeleteRequirement: (String) -> Unit,
    onTogglePublishStatus: (UniversityRequirement) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUniFilter by remember { mutableStateOf("All") }
    var selectedTypeFilter by remember { mutableStateOf<RequirementType?>(null) }
    var selectedPublishFilter by remember { mutableStateOf<Boolean?>(null) }

    var isFormOpen by remember { mutableStateOf(false) }
    var requirementToEdit by remember { mutableStateOf<UniversityRequirement?>(null) }
    var requirementToDelete by remember { mutableStateOf<UniversityRequirement?>(null) }

    val filteredList = remember(requirements, searchQuery, selectedUniFilter, selectedTypeFilter, selectedPublishFilter) {
        requirements.filter { req ->
            val matchesSearch = searchQuery.isBlank() ||
                    req.title.contains(searchQuery, ignoreCase = true) ||
                    req.instructions.contains(searchQuery, ignoreCase = true) ||
                    req.type.displayName.contains(searchQuery, ignoreCase = true) ||
                    req.universityName.contains(searchQuery, ignoreCase = true)
            val matchesUni = selectedUniFilter == "All" || req.universityId == "All" || req.universityId == selectedUniFilter || req.universityName.equals(selectedUniFilter, ignoreCase = true)
            val matchesType = selectedTypeFilter == null || req.type == selectedTypeFilter
            val matchesPublish = selectedPublishFilter == null || req.isPublished == selectedPublishFilter

            matchesSearch && matchesUni && matchesType && matchesPublish
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_requirements_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dynamic University Requirements",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Configure official university & program requirement criteria, minimum scores, and document rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    requirementToEdit = null
                    isFormOpen = true
                },
                modifier = Modifier.testTag("add_requirement_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Requirement")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Filter Controls
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by requirement title, score, or instructions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "Clear") } }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_requirements_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedUniFilter == "All",
                    onClick = { selectedUniFilter = "All" },
                    label = { Text("All Universities") }
                )
            }
            universities.forEach { uni ->
                item {
                    FilterChip(
                        selected = selectedUniFilter == uni.universityId,
                        onClick = {
                            selectedUniFilter = if (selectedUniFilter == uni.universityId) "All" else uni.universityId
                        },
                        label = { Text(uni.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type filter chips row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All Types") }
                )
            }
            RequirementType.entries.forEach { reqType ->
                item {
                    FilterChip(
                        selected = selectedTypeFilter == reqType,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == reqType) null else reqType
                        },
                        label = { Text(reqType.displayName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Requirements Count & List
        Text(
            text = "Requirements (${filteredList.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No requirement entries found matching selected filters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.requirementId }) { req ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_requirement_card_${req.requirementId}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (req.title.isNotBlank()) req.title else req.type.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (req.isRequired) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (req.isRequired) "REQUIRED" else "OPTIONAL",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (req.isRequired) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (req.isPublished) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (req.isPublished) "PUBLISHED" else "UNPUBLISHED",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (req.isPublished) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = req.universityName,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (req.programName != "All Programs") {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = req.programName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (req.intakeSeason != "All Intakes") {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = req.intakeSeason,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onTogglePublishStatus(req) },
                                        modifier = Modifier.testTag("toggle_req_publish_${req.requirementId}")
                                    ) {
                                        Icon(
                                            imageVector = if (req.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Publish Status",
                                            tint = if (req.isPublished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            requirementToEdit = req
                                            isFormOpen = true
                                        },
                                        modifier = Modifier.testTag("edit_req_${req.requirementId}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Requirement", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = { requirementToDelete = req },
                                        modifier = Modifier.testTag("delete_req_${req.requirementId}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Requirement", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (req.minScore.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Minimum Score / Criteria: ${req.minScore}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (req.instructions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Instructions: ${req.instructions}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Form Dialog: Add or Edit Requirement
    if (isFormOpen) {
        RequirementFormDialog(
            requirement = requirementToEdit,
            universities = universities,
            onDismiss = { isFormOpen = false },
            onSave = { saved ->
                onSaveRequirement(saved)
                isFormOpen = false
            }
        )
    }

    // Delete Confirmation Dialog
    requirementToDelete?.let { reqToDelete ->
        AlertDialog(
            onDismissRequest = { requirementToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Requirement") },
            text = { Text("Are you sure you want to delete requirement '${reqToDelete.title.ifBlank { reqToDelete.type.displayName }}' for ${reqToDelete.universityName}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRequirement(reqToDelete.requirementId)
                        requirementToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_requirement_btn")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { requirementToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequirementFormDialog(
    requirement: UniversityRequirement?,
    universities: List<University>,
    onDismiss: () -> Unit,
    onSave: (UniversityRequirement) -> Unit
) {
    var selectedUniId by remember { mutableStateOf(requirement?.universityId ?: "All") }
    var selectedUniName by remember { mutableStateOf(requirement?.universityName ?: "All Universities") }
    var programNameInput by remember { mutableStateOf(requirement?.programName ?: "All Programs") }
    var intakeSeasonInput by remember { mutableStateOf(requirement?.intakeSeason ?: "All Intakes") }
    var selectedType by remember { mutableStateOf(requirement?.type ?: RequirementType.PASSPORT) }
    var titleInput by remember { mutableStateOf(requirement?.title ?: "") }
    var isRequired by remember { mutableStateOf(requirement?.isRequired ?: true) }
    var minScoreInput by remember { mutableStateOf(requirement?.minScore ?: "") }
    var instructionsInput by remember { mutableStateOf(requirement?.instructions ?: "") }
    var isPublished by remember { mutableStateOf(requirement?.isPublished ?: true) }

    var uniExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (requirement == null) "Add Requirement" else "Edit Requirement",
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
                // University Selector Dropdown
                Text("Target University", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = uniExpanded,
                    onExpandedChange = { uniExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedUniName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uniExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = uniExpanded,
                        onDismissRequest = { uniExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Universities") },
                            onClick = {
                                selectedUniId = "All"
                                selectedUniName = "All Universities"
                                uniExpanded = false
                            }
                        )
                        universities.forEach { uni ->
                            DropdownMenuItem(
                                text = { Text(uni.name) },
                                onClick = {
                                    selectedUniId = uni.universityId
                                    selectedUniName = uni.name
                                    uniExpanded = false
                                }
                            )
                        }
                    }
                }

                // Program Scope
                OutlinedTextField(
                    value = programNameInput,
                    onValueChange = { programNameInput = it },
                    label = { Text("Program Scope") },
                    placeholder = { Text("e.g. All Programs or MSc Computer Science") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Intake Scope
                OutlinedTextField(
                    value = intakeSeasonInput,
                    onValueChange = { intakeSeasonInput = it },
                    label = { Text("Intake Scope") },
                    placeholder = { Text("e.g. All Intakes or Fall 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Requirement Type Dropdown
                Text("Requirement Category Type", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
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
                        RequirementType.entries.forEach { reqType ->
                            DropdownMenuItem(
                                text = { Text("${reqType.displayName} (${reqType.category})") },
                                onClick = {
                                    selectedType = reqType
                                    if (titleInput.isBlank()) {
                                        titleInput = reqType.displayName
                                    }
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Requirement Title
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Requirement Title") },
                    placeholder = { Text("e.g. Official Academic Transcripts") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("req_title_input"),
                    singleLine = true
                )

                // Required vs Optional Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mark as Required", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isRequired,
                        onCheckedChange = { isRequired = it },
                        modifier = Modifier.testTag("req_required_switch")
                    )
                }

                // Minimum Score / Criteria
                OutlinedTextField(
                    value = minScoreInput,
                    onValueChange = { minScoreInput = it },
                    label = { Text("Minimum Score / Criteria (if applicable)") },
                    placeholder = { Text("e.g. 6.5 Overall, 90 iBT, 3.0 GPA") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("req_min_score_input"),
                    singleLine = true
                )

                // Instructions & Guidelines
                OutlinedTextField(
                    value = instructionsInput,
                    onValueChange = { instructionsInput = it },
                    label = { Text("Instructions & Guidance Notes") },
                    placeholder = { Text("e.g. Submit scanned original or certified copy with official university seal...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("req_instructions_input"),
                    maxLines = 3
                )

                // Published Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Publish Requirement", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it },
                        modifier = Modifier.testTag("req_publish_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val saved = UniversityRequirement(
                        requirementId = requirement?.requirementId ?: "",
                        universityId = selectedUniId,
                        universityName = selectedUniName,
                        programName = programNameInput.ifBlank { "All Programs" },
                        intakeSeason = intakeSeasonInput.ifBlank { "All Intakes" },
                        type = selectedType,
                        title = titleInput.ifBlank { selectedType.displayName },
                        isRequired = isRequired,
                        minScore = minScoreInput.trim(),
                        instructions = instructionsInput.trim(),
                        isPublished = isPublished
                    )
                    onSave(saved)
                },
                modifier = Modifier.testTag("save_requirement_dialog_btn")
            ) {
                Text("Save Requirement")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_requirement_dialog_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAssistanceContent(
    assistanceRequests: List<AssistanceRequest>,
    onUpdateStatus: (requestId: String, status: AssistanceStatus, counselor: String, internalNotes: String) -> Unit,
    onAddMessage: (requestId: String, messageText: String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<AssistanceStatus?>(null) }

    val filteredList = if (selectedFilter == null) assistanceRequests else assistanceRequests.filter { it.status == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_assistance_content")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Application Assistance Operations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Assign counselors, update request statuses, and communicate with students",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${assistanceRequests.size})") }
                )
            }
            items(items = AssistanceStatus.entries.toList()) { status ->
                val count = assistanceRequests.count { it.status == status }
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = status },
                    label = { Text("${status.displayName} ($count)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No assistance service requests found.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = filteredList, key = { it.requestId }) { req ->
                    AdminAssistanceRequestCard(
                        request = req,
                        onUpdate = { status, counselor, notes ->
                            onUpdateStatus(req.requestId, status, counselor, notes)
                        },
                        onSendMessage = { text ->
                            onAddMessage(req.requestId, text)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAssistanceRequestCard(
    request: AssistanceRequest,
    onUpdate: (AssistanceStatus, String, String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf(request.status) }
    var counselorInput by remember { mutableStateOf(request.assignedCounselor) }
    var internalNotesInput by remember { mutableStateOf(request.internalNotes) }
    var messageInput by remember { mutableStateOf("") }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

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
            .testTag("admin_assistance_card_${request.requestId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
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
                        text = "Student: ${request.studentName} (${request.studentEmail})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
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

            Spacer(modifier = Modifier.height(6.dp))

            if (request.targetUniversityName.isNotBlank()) {
                Text(
                    text = "Target: ${request.targetUniversityName}${if (request.targetProgramName.isNotBlank()) " • ${request.targetProgramName}" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (request.studentNotes.isNotBlank()) {
                Text(
                    text = "Student Notes: ${request.studentNotes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand toggle button
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.testTag("toggle_expand_admin_assistance_${request.requestId}")
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isExpanded) "Hide Management Controls" else "Manage Request & Assign Counselor",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Status Dropdown
                Text("Update Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = currentStatus.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        AssistanceStatus.entries.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.displayName) },
                                onClick = {
                                    currentStatus = st
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Assign Counselor
                Text("Assign Counselor / Administrator", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = counselorInput,
                    onValueChange = { counselorInput = it },
                    placeholder = { Text("e.g. Counselor Sarah, Senior Admin David") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Internal Notes
                Text("Internal Admin Notes (Private)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = internalNotesInput,
                    onValueChange = { internalNotesInput = it },
                    placeholder = { Text("Add confidential internal notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onUpdate(currentStatus, counselorInput.trim(), internalNotesInput.trim())
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("save_assistance_update_btn_${request.requestId}")
                ) {
                    Text("Save Request Updates", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Guidance Thread with Student
                Text("Send Guidance Message to Student", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                if (request.guidanceMessages.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        request.guidanceMessages.forEach { msg ->
                            Text(
                                text = "${msg.senderName}: ${msg.message}",
                                fontSize = 11.sp,
                                color = if (msg.isFromAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Write message or guidance update to student...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                onSendMessage(messageInput.trim())
                                messageInput = ""
                            }
                        },
                        modifier = Modifier.testTag("send_admin_assistance_msg_${request.requestId}")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPartnersContent(
    partners: List<Partner>,
    onSavePartner: (Partner) -> Unit,
    onDeletePartner: (String) -> Unit,
    onUpdateStatus: (String, PartnershipStatus) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<PartnerType?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<PartnershipStatus?>(null) }
    var partnerToEdit by remember { mutableStateOf<Partner?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(partners, searchQuery, selectedTypeFilter, selectedStatusFilter) {
        partners.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.country.contains(searchQuery, ignoreCase = true) ||
                    p.website.contains(searchQuery, ignoreCase = true) ||
                    p.notes.contains(searchQuery, ignoreCase = true)
            val matchesType = selectedTypeFilter == null || p.type == selectedTypeFilter
            val matchesStatus = selectedStatusFilter == null || p.partnershipStatus == selectedStatusFilter
            matchesQuery && matchesType && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_partners_content")
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Convoy Verified Partner Operations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage university partnerships, authorized providers, recruitment agents & commission agreements",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    partnerToEdit = null
                    showAddDialog = true
                },
                modifier = Modifier.testTag("add_partner_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Partner", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compliance Notice Box
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Compliance Standard & Verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "The system records verified agreements. Unverified commission claims are NOT displayed to students. Representation claims require verified active status.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalCount = partners.size
            val activeCount = partners.count { it.partnershipStatus == PartnershipStatus.ACTIVE }
            val negotiatingCount = partners.count { it.partnershipStatus == PartnershipStatus.NEGOTIATING || it.partnershipStatus == PartnershipStatus.CONTACTED }
            val verifiedCount = partners.count { it.isVerifiedActivePartnership }

            val statItems = listOf(
                "Total Partners" to "$totalCount",
                "Active Partners" to "$activeCount",
                "In Pipeline" to "$negotiatingCount",
                "Verified Direct" to "$verifiedCount"
            )

            statItems.forEach { (label, value) ->
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by partner name, country, website, or notes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips for Partner Type
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All Types (${partners.size})", fontSize = 11.sp) }
                )
            }
            items(items = PartnerType.entries.toList()) { pType ->
                val count = partners.count { it.type == pType }
                FilterChip(
                    selected = selectedTypeFilter == pType,
                    onClick = { selectedTypeFilter = pType },
                    label = { Text("${pType.displayName} ($count)", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Filter Chips for Partnership Status
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("All Statuses", fontSize = 11.sp) }
                )
            }
            items(items = PartnershipStatus.entries.toList()) { pStatus ->
                val count = partners.count { it.partnershipStatus == pStatus }
                FilterChip(
                    selected = selectedStatusFilter == pStatus,
                    onClick = { selectedStatusFilter = pStatus },
                    label = { Text("${pStatus.displayName} ($count)", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No partners found matching the filters.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = filteredList, key = { it.partnerId }) { partner ->
                    AdminPartnerCard(
                        partner = partner,
                        onEdit = {
                            partnerToEdit = partner
                            showAddDialog = true
                        },
                        onDelete = { onDeletePartner(partner.partnerId) },
                        onUpdateStatus = { st -> onUpdateStatus(partner.partnerId, st) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditPartnerDialog(
            initialPartner = partnerToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { p ->
                onSavePartner(p)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPartnerCard(
    partner: Partner,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStatus: (PartnershipStatus) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val statusColor = when (partner.partnershipStatus) {
        PartnershipStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
        PartnershipStatus.NEGOTIATING, PartnershipStatus.CONTACTED -> MaterialTheme.colorScheme.primary
        PartnershipStatus.PROSPECT -> MaterialTheme.colorScheme.secondary
        PartnershipStatus.PAUSED -> MaterialTheme.colorScheme.outline
        PartnershipStatus.TERMINATED -> MaterialTheme.colorScheme.error
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_partner_card_${partner.partnerId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Name, Type, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = partner.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${partner.type.displayName} • ${partner.country}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = partner.partnershipStatus.displayName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verification Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (partner.isVerifiedActivePartnership) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VERIFIED DIRECT PARTNER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "INDEPENDENT / UNVERIFIED AGREEMENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Contact & Agreement Overview
            Text(
                text = "Agreement Status: ${partner.agreementStatus}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (partner.contactInfo.isNotBlank()) {
                Text(
                    text = "Contact: ${partner.contactInfo}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (partner.website.isNotBlank()) {
                Text(
                    text = "Website: ${partner.website}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand Toggle & Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_expand_partner_${partner.partnerId}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) "Hide Commission & Details" else "View Commission Terms & Notes",
                        fontSize = 12.sp
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_partner_btn_${partner.partnerId}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Partner", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_partner_btn_${partner.partnerId}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Partner", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Commission Information (Private Admin Only)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "COMMISSION INFORMATION (CONFIDENTIAL ADMIN ONLY)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = partner.commissionInfo.ifBlank { "No commission structure specified." },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (partner.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Admin Notes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Text(partner.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Status Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Update Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = partner.partnershipStatus.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier.menuAnchor().width(180.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            PartnershipStatus.entries.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.displayName) },
                                    onClick = {
                                        onUpdateStatus(st)
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPartnerDialog(
    initialPartner: Partner?,
    onDismiss: () -> Unit,
    onSave: (Partner) -> Unit
) {
    var name by remember { mutableStateOf(initialPartner?.name ?: "") }
    var type by remember { mutableStateOf(initialPartner?.type ?: PartnerType.UNIVERSITY) }
    var country by remember { mutableStateOf(initialPartner?.country ?: "") }
    var contactInfo by remember { mutableStateOf(initialPartner?.contactInfo ?: "") }
    var website by remember { mutableStateOf(initialPartner?.website ?: "") }
    var status by remember { mutableStateOf(initialPartner?.partnershipStatus ?: PartnershipStatus.PROSPECT) }
    var agreementStatus by remember { mutableStateOf(initialPartner?.agreementStatus ?: "Under Review") }
    var commissionInfo by remember { mutableStateOf(initialPartner?.commissionInfo ?: "") }
    var notes by remember { mutableStateOf(initialPartner?.notes ?: "") }

    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialPartner == null) "Add Partner Record" else "Edit Partner Details")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Partner Name *") },
                    placeholder = { Text("e.g. University of Melbourne") },
                    modifier = Modifier.fillMaxWidth().testTag("partner_name_input"),
                    singleLine = true
                )

                Text("Partner Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = type.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        PartnerType.entries.forEach { pType ->
                            DropdownMenuItem(
                                text = { Text(pType.displayName) },
                                onClick = {
                                    type = pType
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country *") },
                    placeholder = { Text("e.g. Australia, United Kingdom") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Contact Information") },
                    placeholder = { Text("e.g. email, phone, main contact name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Official Website") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Partnership Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = status.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        PartnershipStatus.entries.forEach { pStatus ->
                            DropdownMenuItem(
                                text = { Text(pStatus.displayName) },
                                onClick = {
                                    status = pStatus
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = agreementStatus,
                    onValueChange = { agreementStatus = it },
                    label = { Text("Agreement Status") },
                    placeholder = { Text("e.g. Signed Direct Contract, MOU Draft") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = commissionInfo,
                    onValueChange = { commissionInfo = it },
                    label = { Text("Commission Information (Private)") },
                    placeholder = { Text("e.g. 15% Year 1 tuition, or Flat $2000 USD") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Admin Notes") },
                    placeholder = { Text("Confidential internal notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && country.isNotBlank()) {
                        val partner = (initialPartner ?: Partner(
                            name = name.trim(),
                            type = type,
                            country = country.trim(),
                            contactInfo = contactInfo.trim(),
                            website = website.trim()
                        )).copy(
                            name = name.trim(),
                            type = type,
                            country = country.trim(),
                            contactInfo = contactInfo.trim(),
                            website = website.trim(),
                            partnershipStatus = status,
                            agreementStatus = agreementStatus.trim().ifBlank { "No Formal Agreement" },
                            commissionInfo = commissionInfo.trim().ifBlank { "None / Unverified" },
                            notes = notes.trim(),
                            lastUpdated = "2026-02-09"
                        )
                        onSave(partner)
                    }
                },
                modifier = Modifier.testTag("save_partner_dialog_btn")
            ) {
                Text("Save Partner")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSponsoredListingsContent(
    sponsoredListings: List<SponsoredListing>,
    universities: List<University> = emptyList(),
    scholarships: List<Scholarship> = emptyList(),
    onSaveListing: (SponsoredListing) -> Unit,
    onDeleteListing: (String) -> Unit,
    onUpdateStatus: (String, ListingStatus) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<ListingStatus?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<ListingEntityType?>(null) }
    var listingToEdit by remember { mutableStateOf<SponsoredListing?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val currentDate = "2026-02-09"

    val filteredList = remember(sponsoredListings, searchQuery, selectedStatusFilter, selectedTypeFilter) {
        sponsoredListings.filter { sp ->
            val matchesQuery = searchQuery.isBlank() ||
                    sp.entityName.contains(searchQuery, ignoreCase = true) ||
                    sp.sponsorPartner.contains(searchQuery, ignoreCase = true) ||
                    sp.placement.contains(searchQuery, ignoreCase = true) ||
                    sp.internalNotes.contains(searchQuery, ignoreCase = true)
            val computed = sp.computedStatus(currentDate)
            val matchesStatus = selectedStatusFilter == null || computed == selectedStatusFilter
            val matchesType = selectedTypeFilter == null || sp.entityType == selectedTypeFilter
            matchesQuery && matchesStatus && matchesType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_sponsored_listings_content")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sponsored & Featured Listings Operations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manage paid/promotional placement for Universities, Scholarships & Programs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    listingToEdit = null
                    showAddDialog = true
                },
                modifier = Modifier.testTag("add_sponsored_listing_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Listing", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mandatory Transparency Box
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TRANSPARENCY MANDATE & AUTOMATION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Sponsored & Featured items are clearly labeled on user screens. Expired listings are automatically hidden from student view. Normal organic rankings are never disguised.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stat Badges Row: Active, Scheduled, Expired
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val activeCount = sponsoredListings.count { it.computedStatus(currentDate) == ListingStatus.ACTIVE }
            val scheduledCount = sponsoredListings.count { it.computedStatus(currentDate) == ListingStatus.SCHEDULED }
            val expiredCount = sponsoredListings.count { it.computedStatus(currentDate) == ListingStatus.EXPIRED }
            val totalCount = sponsoredListings.size

            val statItems = listOf(
                "Total Listings" to "$totalCount",
                "Active Now" to "$activeCount",
                "Scheduled" to "$scheduledCount",
                "Expired / Hidden" to "$expiredCount"
            )

            statItems.forEach { (label, value) ->
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by entity name, sponsor, placement, or notes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips for Status: Active, Scheduled, Expired
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                label = { Text("All Statuses", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedStatusFilter == ListingStatus.ACTIVE,
                onClick = { selectedStatusFilter = ListingStatus.ACTIVE },
                label = { Text("Active (${sponsoredListings.count { it.computedStatus(currentDate) == ListingStatus.ACTIVE }})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedStatusFilter == ListingStatus.SCHEDULED,
                onClick = { selectedStatusFilter = ListingStatus.SCHEDULED },
                label = { Text("Scheduled (${sponsoredListings.count { it.computedStatus(currentDate) == ListingStatus.SCHEDULED }})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedStatusFilter == ListingStatus.EXPIRED,
                onClick = { selectedStatusFilter = ListingStatus.EXPIRED },
                label = { Text("Expired (${sponsoredListings.count { it.computedStatus(currentDate) == ListingStatus.EXPIRED }})", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Filter Chips for Entity Type: Universities, Scholarships, Programs
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedTypeFilter == null,
                onClick = { selectedTypeFilter = null },
                label = { Text("All Entities", fontSize = 11.sp) }
            )
            ListingEntityType.entries.forEach { eType ->
                FilterChip(
                    selected = selectedTypeFilter == eType,
                    onClick = { selectedTypeFilter = eType },
                    label = { Text(eType.displayName, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No sponsored listings match the selected filters.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = filteredList, key = { it.listingId }) { listing ->
                    AdminSponsoredListingCard(
                        listing = listing,
                        currentDate = currentDate,
                        onEdit = {
                            listingToEdit = listing
                            showAddDialog = true
                        },
                        onDelete = { onDeleteListing(listing.listingId) },
                        onUpdateStatus = { st -> onUpdateStatus(listing.listingId, st) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditSponsoredListingDialog(
            initialListing = listingToEdit,
            universities = universities,
            scholarships = scholarships,
            onDismiss = { showAddDialog = false },
            onSave = { sp ->
                onSaveListing(sp)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSponsoredListingCard(
    listing: SponsoredListing,
    currentDate: String = "2026-02-09",
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStatus: (ListingStatus) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val computed = listing.computedStatus(currentDate)

    val (statusBg, statusFg, statusLabel) = when (computed) {
        ListingStatus.ACTIVE -> Triple(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, "ACTIVE (SHOWING)")
        ListingStatus.SCHEDULED -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "SCHEDULED")
        ListingStatus.EXPIRED -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.outline, "EXPIRED (AUTO-HIDDEN)")
        ListingStatus.PAUSED -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "PAUSED")
    }

    val typeColor = if (listing.listingType == ListingType.SPONSORED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_sponsored_card_${listing.listingId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Entity Name & Type Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listing.entityName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${listing.entityType.displayName} • Placement: ${listing.placement}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = typeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = listing.listingType.badgeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details Grid: Sponsor, Schedule Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Sponsor / Partner:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Text(listing.sponsorPartner, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Campaign Duration:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Text("${listing.startDate} → ${listing.endDate}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable details & action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_expand_sponsored_${listing.listingId}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isExpanded) "Hide Notes & Settings" else "View Internal Notes", fontSize = 12.sp)
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_sponsored_btn_${listing.listingId}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Listing", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_sponsored_btn_${listing.listingId}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Listing", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (listing.internalNotes.isNotBlank()) {
                    Text("Internal Notes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Text(listing.internalNotes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Quick Status Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manual Status Override:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = listing.status.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier.menuAnchor().width(160.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            ListingStatus.entries.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.displayName) },
                                    onClick = {
                                        onUpdateStatus(st)
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditSponsoredListingDialog(
    initialListing: SponsoredListing?,
    universities: List<University> = emptyList(),
    scholarships: List<Scholarship> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (SponsoredListing) -> Unit
) {
    var entityType by remember { mutableStateOf(initialListing?.entityType ?: ListingEntityType.UNIVERSITY) }
    var entityId by remember { mutableStateOf(initialListing?.entityId ?: "") }
    var entityName by remember { mutableStateOf(initialListing?.entityName ?: "") }
    var listingType by remember { mutableStateOf(initialListing?.listingType ?: ListingType.FEATURED) }
    var startDate by remember { mutableStateOf(initialListing?.startDate ?: "2026-02-01") }
    var endDate by remember { mutableStateOf(initialListing?.endDate ?: "2026-12-31") }
    var placement by remember { mutableStateOf(initialListing?.placement ?: "Search & Discovery Top") }
    var status by remember { mutableStateOf(initialListing?.status ?: ListingStatus.ACTIVE) }
    var sponsorPartner by remember { mutableStateOf(initialListing?.sponsorPartner ?: "") }
    var internalNotes by remember { mutableStateOf(initialListing?.internalNotes ?: "") }

    var entityTypeDropdownExpanded by remember { mutableStateOf(false) }
    var listingTypeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var entitySelectDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialListing == null) "Create Sponsored / Featured Listing" else "Edit Sponsored Listing")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Entity Type Selection
                Text("Target Entity Type *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = entityTypeDropdownExpanded,
                    onExpandedChange = { entityTypeDropdownExpanded = !entityTypeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = entityType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = entityTypeDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = entityTypeDropdownExpanded,
                        onDismissRequest = { entityTypeDropdownExpanded = false }
                    ) {
                        ListingEntityType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    entityType = type
                                    entityTypeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Quick selector or text input for Entity Name & ID
                if (entityType == ListingEntityType.UNIVERSITY && universities.isNotEmpty()) {
                    Text("Select University or Enter Custom", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = entitySelectDropdownExpanded,
                        onExpandedChange = { entitySelectDropdownExpanded = !entitySelectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = entityName,
                            onValueChange = { entityName = it },
                            placeholder = { Text("Search university...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = entitySelectDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = entitySelectDropdownExpanded,
                            onDismissRequest = { entitySelectDropdownExpanded = false }
                        ) {
                            universities.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.name} (${u.country})") },
                                    onClick = {
                                        entityName = u.name
                                        entityId = u.universityId
                                        if (sponsorPartner.isBlank()) sponsorPartner = "${u.name} Admissions"
                                        entitySelectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else if (entityType == ListingEntityType.SCHOLARSHIP && scholarships.isNotEmpty()) {
                    Text("Select Scholarship or Enter Custom", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = entitySelectDropdownExpanded,
                        onExpandedChange = { entitySelectDropdownExpanded = !entitySelectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = entityName,
                            onValueChange = { entityName = it },
                            placeholder = { Text("Search scholarship...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = entitySelectDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = entitySelectDropdownExpanded,
                            onDismissRequest = { entitySelectDropdownExpanded = false }
                        ) {
                            scholarships.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        entityName = s.name
                                        entityId = s.scholarshipId
                                        if (sponsorPartner.isBlank()) sponsorPartner = s.provider
                                        entitySelectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = entityName,
                        onValueChange = { entityName = it },
                        label = { Text("Entity Name *") },
                        placeholder = { Text("e.g. MSc Data Science & AI") },
                        modifier = Modifier.fillMaxWidth().testTag("sponsored_entity_name_input"),
                        singleLine = true
                    )
                }

                // Listing Type: Featured vs Sponsored
                Text("Placement Classification *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = listingTypeDropdownExpanded,
                    onExpandedChange = { listingTypeDropdownExpanded = !listingTypeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = listingType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = listingTypeDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = listingTypeDropdownExpanded,
                        onDismissRequest = { listingTypeDropdownExpanded = false }
                    ) {
                        ListingType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    listingType = type
                                    listingTypeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Start Date & End Date
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Placement Slot
                OutlinedTextField(
                    value = placement,
                    onValueChange = { placement = it },
                    label = { Text("Placement Slot / Location") },
                    placeholder = { Text("e.g. Search Top Banner, Scholarship Spotlight") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Sponsor / Partner
                OutlinedTextField(
                    value = sponsorPartner,
                    onValueChange = { sponsorPartner = it },
                    label = { Text("Sponsor / Partner Name") },
                    placeholder = { Text("e.g. Direct University Admissions Office") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Status Override
                Text("Initial Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = status.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        ListingStatus.entries.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.displayName) },
                                onClick = {
                                    status = st
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Internal Notes
                OutlinedTextField(
                    value = internalNotes,
                    onValueChange = { internalNotes = it },
                    label = { Text("Internal Admin Notes") },
                    placeholder = { Text("Agreement details, campaign manager, notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (entityName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()) {
                        val listing = (initialListing ?: SponsoredListing(
                            entityType = entityType,
                            entityId = entityId.ifBlank { "ent_${System.currentTimeMillis().toString().takeLast(6)}" },
                            entityName = entityName.trim()
                        )).copy(
                            entityType = entityType,
                            entityId = entityId.ifBlank { "ent_${System.currentTimeMillis().toString().takeLast(6)}" },
                            entityName = entityName.trim(),
                            listingType = listingType,
                            startDate = startDate.trim(),
                            endDate = endDate.trim(),
                            placement = placement.trim().ifBlank { "Search Top Banner" },
                            status = status,
                            sponsorPartner = sponsorPartner.trim().ifBlank { "Partner Institution" },
                            internalNotes = internalNotes.trim()
                        )
                        onSave(listing)
                    }
                },
                modifier = Modifier.testTag("save_sponsored_dialog_btn")
            ) {
                Text("Save Listing")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

