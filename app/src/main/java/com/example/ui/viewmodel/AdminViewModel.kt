package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AdminOverviewMetrics
import com.example.data.repository.AdminRepository
import com.example.data.repository.RemoteAdminRepository
import com.example.data.repository.SupportRepository
import com.example.data.repository.RemoteSupportRepository
import com.example.data.security.ConvoySecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AdminTab(val label: String) {
    DASHBOARD("Dashboard"),
    CHAT_HUB("Chat & Counsellors"),
    ANALYTICS("Business Analytics"),
    SUPPORT("Support Requests"),
    UNIVERSITIES("Universities"),
    REQUIREMENTS("Requirements"),
    ASSISTANCE("Assistance Services"),
    PARTNERS("Partners & Commissions"),
    SPONSORED_LISTINGS("Sponsored Listings"),
    SCHOLARSHIPS("Scholarships"),
    COUNTRIES("Countries"),
    APPLICATIONS("Applications"),
    DOCUMENTS("Documents"),
    STUDENTS("Students"),
    REFERRALS("Referrals"),
    LEADS("Leads"),
    ANNOUNCEMENTS("Announcements"),
    SETTINGS("Settings")
}

data class AdminUiState(
    val activeTab: AdminTab = AdminTab.DASHBOARD,
    val isAuthorized: Boolean = false,
    val overviewMetrics: AdminOverviewMetrics = AdminOverviewMetrics(),
    val analyticsMetrics: BusinessAnalyticsMetrics = BusinessAnalyticsMetrics(),
    val selectedDateFilter: AnalyticsDateFilter = AnalyticsDateFilter.DAYS_30,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val supportRequests: List<SupportRequest> = emptyList(),
    val supportConfig: SupportConfig = SupportConfig(),
    val universities: List<University> = emptyList(),
    val requirements: List<UniversityRequirement> = emptyList(),
    val assistanceRequests: List<AssistanceRequest> = emptyList(),
    val partners: List<Partner> = emptyList(),
    val sponsoredListings: List<SponsoredListing> = emptyList(),
    val scholarships: List<Scholarship> = emptyList(),
    val countries: List<Country> = emptyList(),
    val applications: List<Application> = emptyList(),
    val documents: List<StudentDocument> = emptyList(),
    val students: List<User> = emptyList(),
    val referrals: List<Referral> = emptyList(),
    val leads: List<Lead> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val selectedApplication: Application? = null,
    val selectedReferral: Referral? = null,
    val selectedLead: Lead? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val adminRepo: AdminRepository = RemoteAdminRepository(),
    private val supportRepo: SupportRepository = RemoteSupportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        checkAuthorizationAndLoadData()
    }

    fun checkAuthorizationAndLoadData() {
        val hasPermission = ConvoySecurityManager.canManageAdminContent()
        _uiState.update { it.copy(isAuthorized = hasPermission) }

        if (hasPermission) {
            loadAdminData()
        }
    }

    private fun loadAdminData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            launch {
                adminRepo.getOverviewMetrics().collect { metrics ->
                    _uiState.update { it.copy(overviewMetrics = metrics) }
                }
            }

            launch {
                supportRepo.getSupportRequestsForAdmin().collect { list ->
                    _uiState.update { it.copy(supportRequests = list) }
                }
            }

            launch {
                supportRepo.getSupportConfig().collect { config ->
                    _uiState.update { it.copy(supportConfig = config) }
                }
            }

            launch {
                adminRepo.getUniversities().collect { list ->
                    _uiState.update { it.copy(universities = list) }
                }
            }

            launch {
                adminRepo.getRequirements().collect { list ->
                    _uiState.update { it.copy(requirements = list) }
                }
            }

            launch {
                adminRepo.getAssistanceRequests().collect { list ->
                    _uiState.update { it.copy(assistanceRequests = list) }
                }
            }

            launch {
                adminRepo.getPartners().collect { list ->
                    _uiState.update { it.copy(partners = list) }
                }
            }

            launch {
                adminRepo.getSponsoredListings().collect { list ->
                    _uiState.update { it.copy(sponsoredListings = list) }
                }
            }

            launch {
                adminRepo.getScholarships().collect { list ->
                    _uiState.update { it.copy(scholarships = list) }
                }
            }

            launch {
                adminRepo.getCountries().collect { list ->
                    _uiState.update { it.copy(countries = list) }
                }
            }

            launch {
                adminRepo.getApplications().collect { list ->
                    _uiState.update { it.copy(applications = list) }
                }
            }

            launch {
                adminRepo.getDocuments().collect { list ->
                    _uiState.update { it.copy(documents = list) }
                }
            }

            launch {
                adminRepo.getStudents().collect { list ->
                    _uiState.update { it.copy(students = list) }
                }
            }

            launch {
                adminRepo.getReferrals().collect { list ->
                    _uiState.update { it.copy(referrals = list) }
                }
            }

            launch {
                adminRepo.getLeads().collect { list ->
                    _uiState.update { it.copy(leads = list) }
                }
            }

            launch {
                adminRepo.getAnnouncements().collect { list ->
                    _uiState.update { it.copy(announcements = list) }
                }
            }

            launch {
                adminRepo.getRecentActivities().collect { list ->
                    _uiState.update { it.copy(recentActivities = list) }
                }
            }

            launch {
                adminRepo.getBusinessAnalyticsMetrics(
                    filter = _uiState.value.selectedDateFilter,
                    customStart = _uiState.value.customStartDate,
                    customEnd = _uiState.value.customEndDate
                ).collect { analytics ->
                    _uiState.update { it.copy(analyticsMetrics = analytics) }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun setAnalyticsDateFilter(filter: AnalyticsDateFilter, customStart: Long? = null, customEnd: Long? = null) {
        _uiState.update { 
            it.copy(
                selectedDateFilter = filter,
                customStartDate = customStart,
                customEndDate = customEnd
            )
        }
        viewModelScope.launch {
            adminRepo.getBusinessAnalyticsMetrics(filter, customStart, customEnd).collect { analytics ->
                _uiState.update { it.copy(analyticsMetrics = analytics) }
            }
        }
    }

    fun trackAnalyticsEvent(event: AnalyticsEvent) {
        adminRepo.trackAnalyticsEvent(event)
    }

    fun selectTab(tab: AdminTab) {
        _uiState.update { it.copy(activeTab = tab, searchQuery = "") }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun saveUniversity(university: University) {
        adminRepo.saveUniversity(university)
    }

    fun deleteUniversity(universityId: String) {
        adminRepo.deleteUniversity(universityId)
    }

    fun toggleUniversityPublishStatus(university: University) {
        val newStatus = if (university.status == EntityStatus.PUBLISHED) EntityStatus.DRAFT else EntityStatus.PUBLISHED
        adminRepo.updateUniversityStatus(university.universityId, newStatus)
    }

    fun saveScholarship(scholarship: Scholarship) {
        adminRepo.saveScholarship(scholarship)
    }

    fun deleteScholarship(scholarshipId: String) {
        adminRepo.deleteScholarship(scholarshipId)
    }

    fun toggleScholarshipPublishStatus(scholarship: Scholarship) {
        val newStatus = if (scholarship.status == EntityStatus.PUBLISHED) EntityStatus.DRAFT else EntityStatus.PUBLISHED
        adminRepo.updateScholarshipStatus(scholarship.scholarshipId, newStatus)
    }

    fun saveRequirement(requirement: UniversityRequirement) {
        adminRepo.saveRequirement(requirement)
    }

    fun deleteRequirement(requirementId: String) {
        adminRepo.deleteRequirement(requirementId)
    }

    fun toggleRequirementPublishStatus(requirement: UniversityRequirement) {
        adminRepo.updateRequirementStatus(requirement.requirementId, !requirement.isPublished)
    }

    fun updateAssistanceStatus(requestId: String, status: AssistanceStatus, counselor: String = "", internalNotes: String = "") {
        adminRepo.updateAssistanceStatus(requestId, status, counselor, internalNotes)
    }

    fun addAdminGuidanceMessage(requestId: String, messageText: String) {
        val user = ConvoySecurityManager.currentUser
        val msg = GuidanceMessage(
            id = "msg_${System.currentTimeMillis().toString().takeLast(6)}",
            senderName = "${user.name} (Convoy Counselor)",
            isFromAdmin = true,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )
        adminRepo.addGuidanceMessage(requestId, msg)
    }

    fun savePartner(partner: Partner) {
        adminRepo.savePartner(partner)
    }

    fun deletePartner(partnerId: String) {
        adminRepo.deletePartner(partnerId)
    }

    fun updatePartnerStatus(partnerId: String, status: PartnershipStatus) {
        adminRepo.updatePartnerStatus(partnerId, status)
    }

    fun saveSponsoredListing(listing: SponsoredListing) {
        adminRepo.saveSponsoredListing(listing)
    }

    fun deleteSponsoredListing(listingId: String) {
        adminRepo.deleteSponsoredListing(listingId)
    }

    fun updateSponsoredListingStatus(listingId: String, status: ListingStatus) {
        adminRepo.updateSponsoredListingStatus(listingId, status)
    }

    fun updateApplicationAttribution(
        applicationId: String,
        partnerId: String?,
        partnerName: String?,
        source: String,
        commissionEligible: Boolean,
        commissionStatus: CommissionStatus,
        commissionAmount: String? = null
    ) {
        adminRepo.updateApplicationAttribution(applicationId, partnerId, partnerName, source, commissionEligible, commissionStatus, commissionAmount)
    }

    fun selectApplication(application: Application?) {
        _uiState.update { it.copy(selectedApplication = application) }
    }

    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, note: String = "") {
        adminRepo.updateApplicationStatus(applicationId, status, note)
    }

    fun updateApplicationInternalNotes(applicationId: String, notes: String) {
        adminRepo.updateApplicationInternalNotes(applicationId, notes)
    }

    fun requestMissingDocuments(applicationId: String, missingDocs: List<String>) {
        adminRepo.requestMissingDocuments(applicationId, missingDocs)
    }

    fun selectReferral(referral: Referral?) {
        _uiState.update { it.copy(selectedReferral = referral) }
    }

    fun updateReferralStatus(referralId: String, status: ReferralStatus, adminNote: String = "") {
        adminRepo.updateReferralStatus(referralId, status, adminNote)
        // Keep selected referral updated
        _uiState.value.referrals.find { it.referralId == referralId }?.let { updatedRef ->
            _uiState.update { it.copy(selectedReferral = updatedRef.copy(status = status, adminNote = adminNote)) }
        }
    }

    fun selectLead(lead: Lead?) {
        _uiState.update { it.copy(selectedLead = lead) }
    }

    fun updateLeadStatus(leadId: String, status: LeadStatus, notes: String = "") {
        adminRepo.updateLeadStatus(leadId, status, notes)
        _uiState.value.leads.find { it.leadId == leadId }?.let { updatedLead ->
            _uiState.update { it.copy(selectedLead = updatedLead.copy(status = status, notes = notes.ifBlank { updatedLead.notes })) }
        }
    }

    // Support Request Management Methods
    fun addSupportReplyByAdmin(requestId: String, message: String) {
        supportRepo.addReply(requestId, message, isAdmin = true)
    }

    fun updateSupportStatusByAdmin(
        requestId: String,
        status: SupportStatus,
        internalNotes: String = "",
        assignedStaff: String = ""
    ) {
        supportRepo.updateSupportStatusByAdmin(requestId, status, internalNotes, assignedStaff)
    }

    fun updateSupportConfigByAdmin(config: SupportConfig) {
        supportRepo.updateSupportConfigByAdmin(config)
    }
}
