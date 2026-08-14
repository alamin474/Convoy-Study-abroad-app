package com.example.data.repository

import com.example.data.model.*
import com.example.data.remote.ConvoyRemoteDataSource
import com.example.data.remote.RemoteDataSource
import com.example.data.security.ConvoySecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

data class AdminOverviewMetrics(
    val totalUniversities: Int = 0,
    val totalScholarships: Int = 0,
    val totalStudents: Int = 0,
    val totalApplications: Int = 0,
    val pendingApplications: Int = 0,
    val pendingDocuments: Int = 0,
    val totalReferrals: Int = 0,
    val recentActivities: List<RecentActivity> = emptyList()
)

interface AdminRepository {
    fun getOverviewMetrics(): Flow<AdminOverviewMetrics>
    fun getUniversities(): Flow<List<University>>
    fun getScholarships(): Flow<List<Scholarship>>
    fun getCountries(): Flow<List<Country>>
    fun saveCountry(country: Country)
    fun deleteCountry(countryId: String)
    fun updateCountryStatus(countryId: String, status: EntityStatus)
    fun getApplications(): Flow<List<Application>>
    fun getDocuments(): Flow<List<StudentDocument>>
    fun getStudents(): Flow<List<User>>
    fun getReferrals(): Flow<List<Referral>>
    fun getLeads(): Flow<List<Lead>>
    fun getAnnouncements(): Flow<List<Announcement>>
    fun getRecentActivities(): Flow<List<RecentActivity>>

    fun getRequirements(): Flow<List<UniversityRequirement>>
    fun saveRequirement(requirement: UniversityRequirement)
    fun deleteRequirement(requirementId: String)
    fun updateRequirementStatus(requirementId: String, isPublished: Boolean)

    fun getAssistanceRequests(): Flow<List<AssistanceRequest>>
    fun updateAssistanceStatus(requestId: String, status: AssistanceStatus, counselor: String = "", internalNotes: String = "")
    fun addGuidanceMessage(requestId: String, message: GuidanceMessage)

    fun getPartners(): Flow<List<Partner>>
    fun savePartner(partner: Partner)
    fun deletePartner(partnerId: String)
    fun updatePartnerStatus(partnerId: String, status: PartnershipStatus)
    fun updateApplicationAttribution(
        applicationId: String,
        partnerId: String?,
        partnerName: String?,
        source: String,
        commissionEligible: Boolean,
        commissionStatus: CommissionStatus,
        commissionAmount: String? = null
    )

    fun getSponsoredListings(): Flow<List<SponsoredListing>>
    fun saveSponsoredListing(listing: SponsoredListing)
    fun deleteSponsoredListing(listingId: String)
    fun updateSponsoredListingStatus(listingId: String, status: ListingStatus)

    fun saveUniversity(university: University)
    fun deleteUniversity(universityId: String)
    fun updateUniversityStatus(universityId: String, status: EntityStatus)

    fun saveScholarship(scholarship: Scholarship)
    fun deleteScholarship(scholarshipId: String)
    fun updateScholarshipStatus(scholarshipId: String, status: EntityStatus)

    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, note: String = "")
    fun updateApplicationInternalNotes(applicationId: String, notes: String)
    fun requestMissingDocuments(applicationId: String, missingDocs: List<String>)
    fun updateReferralStatus(referralId: String, status: ReferralStatus, adminNote: String = "")
    fun updateLeadStatus(leadId: String, status: LeadStatus, notes: String = "")

    fun trackAnalyticsEvent(event: AnalyticsEvent)
    fun getAnalyticsEvents(): Flow<List<AnalyticsEvent>>
    fun getBusinessAnalyticsMetrics(filter: AnalyticsDateFilter, customStart: Long? = null, customEnd: Long? = null): Flow<BusinessAnalyticsMetrics>
}

class RemoteAdminRepository(
    private val remoteDataSource: RemoteDataSource = ConvoyRemoteDataSource()
) : AdminRepository {

    override fun getOverviewMetrics(): Flow<AdminOverviewMetrics> {
        // Enforce role check
        if (!ConvoySecurityManager.canManageAdminContent()) {
            return flowOf(AdminOverviewMetrics())
        }

        val baseFlow = combine(
            remoteDataSource.fetchAllUniversitiesForAdmin(),
            remoteDataSource.fetchScholarships(),
            remoteDataSource.fetchStudents(),
            remoteDataSource.fetchAllApplicationsForAdmin(),
            remoteDataSource.fetchAllDocumentsForAdmin()
        ) { unis, schs, students, apps, docs ->
            val pendingAppsCount = apps.count {
                it.status == ApplicationStatus.IN_REVIEW ||
                it.status == ApplicationStatus.ACTION_REQUIRED ||
                it.status == ApplicationStatus.DRAFT
            }
            val pendingDocsCount = docs.count { !it.isUploaded }

            AdminOverviewMetrics(
                totalUniversities = unis.size,
                totalScholarships = schs.size,
                totalStudents = students.size,
                totalApplications = apps.size,
                pendingApplications = pendingAppsCount,
                pendingDocuments = pendingDocsCount
            )
        }

        val extraFlow = combine(
            remoteDataSource.fetchAllReferralsForAdmin(),
            remoteDataSource.fetchRecentActivities()
        ) { refs, activities ->
            refs to activities
        }

        return combine(baseFlow, extraFlow) { metrics, (refs, activities) ->
            metrics.copy(
                totalReferrals = refs.size,
                recentActivities = activities
            )
        }
    }

    override fun getRequirements(): Flow<List<UniversityRequirement>> = remoteDataSource.fetchAllRequirementsForAdmin()
    override fun saveRequirement(requirement: UniversityRequirement) = remoteDataSource.saveRequirement(requirement)
    override fun deleteRequirement(requirementId: String) = remoteDataSource.deleteRequirement(requirementId)
    override fun updateRequirementStatus(requirementId: String, isPublished: Boolean) = remoteDataSource.updateRequirementStatus(requirementId, isPublished)

    override fun getAssistanceRequests(): Flow<List<AssistanceRequest>> = remoteDataSource.fetchAllAssistanceRequestsForAdmin()
    override fun updateAssistanceStatus(requestId: String, status: AssistanceStatus, counselor: String, internalNotes: String) = remoteDataSource.updateAssistanceStatus(requestId, status, counselor, internalNotes)
    override fun addGuidanceMessage(requestId: String, message: GuidanceMessage) = remoteDataSource.addGuidanceMessage(requestId, message)

    override fun getPartners(): Flow<List<Partner>> = remoteDataSource.fetchPartners()
    override fun savePartner(partner: Partner) = remoteDataSource.savePartner(partner)
    override fun deletePartner(partnerId: String) = remoteDataSource.deletePartner(partnerId)
    override fun updatePartnerStatus(partnerId: String, status: PartnershipStatus) = remoteDataSource.updatePartnerStatus(partnerId, status)
    override fun updateApplicationAttribution(
        applicationId: String,
        partnerId: String?,
        partnerName: String?,
        source: String,
        commissionEligible: Boolean,
        commissionStatus: CommissionStatus,
        commissionAmount: String?
    ) = remoteDataSource.updateApplicationAttribution(applicationId, partnerId, partnerName, source, commissionEligible, commissionStatus, commissionAmount)

    override fun getSponsoredListings(): Flow<List<SponsoredListing>> = remoteDataSource.fetchSponsoredListings()
    override fun saveSponsoredListing(listing: SponsoredListing) = remoteDataSource.saveSponsoredListing(listing)
    override fun deleteSponsoredListing(listingId: String) = remoteDataSource.deleteSponsoredListing(listingId)
    override fun updateSponsoredListingStatus(listingId: String, status: ListingStatus) = remoteDataSource.updateSponsoredListingStatus(listingId, status)

    override fun getUniversities(): Flow<List<University>> = remoteDataSource.fetchAllUniversitiesForAdmin()
    override fun saveUniversity(university: University) = remoteDataSource.saveUniversity(university)
    override fun deleteUniversity(universityId: String) = remoteDataSource.deleteUniversity(universityId)
    override fun updateUniversityStatus(universityId: String, status: EntityStatus) = remoteDataSource.updateUniversityStatus(universityId, status)
    override fun getScholarships(): Flow<List<Scholarship>> = remoteDataSource.fetchAllScholarshipsForAdmin()
    override fun saveScholarship(scholarship: Scholarship) = remoteDataSource.saveScholarship(scholarship)
    override fun deleteScholarship(scholarshipId: String) = remoteDataSource.deleteScholarship(scholarshipId)
    override fun updateScholarshipStatus(scholarshipId: String, status: EntityStatus) = remoteDataSource.updateScholarshipStatus(scholarshipId, status)
    override fun getCountries(): Flow<List<Country>> = remoteDataSource.fetchAllCountriesForAdmin()
    override fun saveCountry(country: Country) = remoteDataSource.saveCountry(country)
    override fun deleteCountry(countryId: String) = remoteDataSource.deleteCountry(countryId)
    override fun updateCountryStatus(countryId: String, status: EntityStatus) = remoteDataSource.updateCountryStatus(countryId, status)
    override fun getApplications(): Flow<List<Application>> = remoteDataSource.fetchAllApplicationsForAdmin()
    override fun getDocuments(): Flow<List<StudentDocument>> = remoteDataSource.fetchAllDocumentsForAdmin()
    override fun getStudents(): Flow<List<User>> = remoteDataSource.fetchStudents()
    override fun getReferrals(): Flow<List<Referral>> = remoteDataSource.fetchAllReferralsForAdmin()
    override fun getLeads(): Flow<List<Lead>> = remoteDataSource.fetchLeads()
    override fun getAnnouncements(): Flow<List<Announcement>> = remoteDataSource.fetchAnnouncements()
    override fun getRecentActivities(): Flow<List<RecentActivity>> = remoteDataSource.fetchRecentActivities()

    override fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, note: String) =
        remoteDataSource.updateApplicationStatus(applicationId, status, note)

    override fun updateApplicationInternalNotes(applicationId: String, notes: String) =
        remoteDataSource.updateApplicationInternalNotes(applicationId, notes)

    override fun requestMissingDocuments(applicationId: String, missingDocs: List<String>) =
        remoteDataSource.requestMissingDocuments(applicationId, missingDocs)

    override fun updateReferralStatus(referralId: String, status: ReferralStatus, adminNote: String) =
        remoteDataSource.updateReferralStatus(referralId, status, adminNote)

    override fun updateLeadStatus(leadId: String, status: LeadStatus, notes: String) =
        remoteDataSource.updateLeadStatus(leadId, status, notes)

    override fun trackAnalyticsEvent(event: AnalyticsEvent) {
        remoteDataSource.trackAnalyticsEvent(event)
    }

    override fun getAnalyticsEvents(): Flow<List<AnalyticsEvent>> = remoteDataSource.fetchAnalyticsEvents()

    override fun getBusinessAnalyticsMetrics(
        filter: AnalyticsDateFilter,
        customStart: Long?,
        customEnd: Long?
    ): Flow<BusinessAnalyticsMetrics> {
        val now = System.currentTimeMillis()
        val minTime = when (filter) {
            AnalyticsDateFilter.TODAY -> now - 86400000L
            AnalyticsDateFilter.DAYS_7 -> now - 86400000L * 7
            AnalyticsDateFilter.DAYS_30 -> now - 86400000L * 30
            AnalyticsDateFilter.DAYS_90 -> now - 86400000L * 90
            AnalyticsDateFilter.CUSTOM -> customStart ?: 0L
        }
        val maxTime = if (filter == AnalyticsDateFilter.CUSTOM && customEnd != null) customEnd else Long.MAX_VALUE

        return combine(
            remoteDataSource.fetchStudents(),
            remoteDataSource.fetchAllApplicationsForAdmin(),
            remoteDataSource.fetchLeads(),
            remoteDataSource.fetchAllAssistanceRequestsForAdmin(),
            remoteDataSource.fetchAllReferralsForAdmin(),
            remoteDataSource.fetchSponsoredListings(),
            remoteDataSource.fetchAllUniversitiesForAdmin(),
            remoteDataSource.fetchAnalyticsEvents()
        ) { args: Array<Any> ->
            @Suppress("UNCHECKED_CAST")
            val students = args[0] as List<User>
            @Suppress("UNCHECKED_CAST")
            val apps = args[1] as List<Application>
            @Suppress("UNCHECKED_CAST")
            val leads = args[2] as List<Lead>
            @Suppress("UNCHECKED_CAST")
            val requests = args[3] as List<AssistanceRequest>
            @Suppress("UNCHECKED_CAST")
            val referrals = args[4] as List<Referral>
            @Suppress("UNCHECKED_CAST")
            val sponsoredListings = args[5] as List<SponsoredListing>
            @Suppress("UNCHECKED_CAST")
            val unis = args[6] as List<University>
            @Suppress("UNCHECKED_CAST")
            val events = args[7] as List<AnalyticsEvent>

            val filteredEvents = events.filter { it.timestamp in minTime..maxTime }
            
            // User metrics
            val totalUsers = students.size
            val newUsers = students.size
            val activeUsers = students.count { user ->
                apps.any { it.userId == user.userId } || requests.any { it.userId == user.userId }
            }.coerceAtLeast(students.size.coerceAtMost(5))

            // Discovery
            val uniViews = filteredEvents.count { it.eventType == AnalyticsEventType.UNIVERSITY_VIEW }
            val schViews = filteredEvents.count { it.eventType == AnalyticsEventType.SCHOLARSHIP_VIEW }
            val countryViews = filteredEvents.count { it.eventType == AnalyticsEventType.COUNTRY_VIEW }
            val searchCount = filteredEvents.count { it.eventType == AnalyticsEventType.SEARCH_PERFORMED }
            val savedCount = filteredEvents.count { it.eventType == AnalyticsEventType.UNIVERSITY_SAVED }

            val topSearchesMap = filteredEvents
                .filter { it.eventType == AnalyticsEventType.SEARCH_PERFORMED && it.searchQuery.isNotBlank() }
                .groupBy { it.searchQuery }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            // Lead
            val profileCreations = students.size
            val infoRequests = leads.size + requests.size
            val appStarts = apps.count { it.status == ApplicationStatus.DRAFT || it.status == ApplicationStatus.IN_REVIEW }

            // Conversion
            val submittedApps = apps.count { it.status != ApplicationStatus.DRAFT }
            val appsByCountry = apps.groupBy { it.country }.mapValues { it.value.size }
            val appsByUni = apps.groupBy { it.universityName }.mapValues { it.value.size }
            val appsByProg = apps.groupBy { it.programName }.mapValues { it.value.size }

            val completionRate = if (appStarts + submittedApps > 0) {
                (submittedApps.toDouble() / (appStarts + submittedApps).coerceAtLeast(1)) * 100.0
            } else 0.0

            // Business
            val serviceRequests = requests.size
            val refConversions = referrals.count { it.status == ReferralStatus.PAID || it.status == ReferralStatus.APPROVED }
            val qualReferrals = referrals.count { it.status == ReferralStatus.QUALIFIED || it.status == ReferralStatus.APPROVED || it.status == ReferralStatus.PAID }
            val sponsoredCount = sponsoredListings.size
            val partnerApps = apps.count { it.commissionEligible || it.partnerId != null }

            val commissionsRevenue = apps
                .filter { it.commissionEligible && it.commissionAmount != null }
                .sumOf { app -> app.commissionAmount?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0 }

            val referralDisbursements = referrals
                .filter { it.status == ReferralStatus.PAID }
                .sumOf { it.rewardAmount }

            val estimatedRevenue = commissionsRevenue + (sponsoredCount * 250.0)

            // Country Performance Breakdown
            val keyCountries = listOf(
                "Cyprus" to "🇨🇾",
                "Malaysia" to "🇲🇾",
                "Denmark" to "🇩🇰",
                "United Kingdom" to "🇬🇧",
                "United States" to "🇺🇸",
                "Australia" to "🇦🇺",
                "Germany" to "🇩🇪",
                "Spain" to "🇪🇸"
            )

            val countryMetrics = keyCountries.map { (countryName, flag) ->
                val countryUnis = unis.count { it.country.equals(countryName, ignoreCase = true) }
                val countryApps = apps.count { it.country.equals(countryName, ignoreCase = true) }
                val countryLeads = leads.count { it.country.equals(countryName, ignoreCase = true) }
                val countryViews = filteredEvents.count { it.country.equals(countryName, ignoreCase = true) }
                val convRate = if (countryViews + countryLeads > 0) {
                    (countryApps.toDouble() / (countryViews + countryLeads).coerceAtLeast(1)) * 100.0
                } else if (countryApps > 0) 100.0 else 0.0

                CountryPerformanceMetrics(
                    countryName = countryName,
                    flagEmoji = flag,
                    universityCount = countryUnis,
                    applicationsCount = countryApps,
                    leadsCount = countryLeads,
                    viewsCount = countryViews,
                    conversionRate = convRate
                )
            }.toMutableList()

            // Calculate "Other supported countries"
            val knownNames = keyCountries.map { it.first.lowercase() }.toSet()
            val otherUnis = unis.count { it.country.lowercase() !in knownNames }
            val otherApps = apps.count { it.country.lowercase() !in knownNames }
            val otherLeads = leads.count { it.country.lowercase() !in knownNames }
            val otherViews = filteredEvents.count { it.country.lowercase() !in knownNames }
            val otherConvRate = if (otherViews + otherLeads > 0) {
                (otherApps.toDouble() / (otherViews + otherLeads).coerceAtLeast(1)) * 100.0
            } else if (otherApps > 0) 100.0 else 0.0

            countryMetrics.add(
                CountryPerformanceMetrics(
                    countryName = "Other Supported Countries",
                    flagEmoji = "🌐",
                    universityCount = otherUnis,
                    applicationsCount = otherApps,
                    leadsCount = otherLeads,
                    viewsCount = otherViews,
                    conversionRate = otherConvRate
                )
            )

            BusinessAnalyticsMetrics(
                selectedDateFilter = filter,
                customStartDate = customStart,
                customEndDate = customEnd,

                totalUsers = totalUsers,
                newUsers = newUsers,
                activeUsers = activeUsers,

                universityViews = uniViews,
                scholarshipViews = schViews,
                countryViews = countryViews,
                searchActivityCount = searchCount,
                savedUniversitiesCount = savedCount,
                topSearches = topSearchesMap,

                profileCreations = profileCreations,
                informationRequests = infoRequests,
                applicationStarts = appStarts,

                applicationsSubmitted = submittedApps,
                applicationsByCountry = appsByCountry,
                applicationsByUniversity = appsByUni,
                applicationsByProgram = appsByProg,
                applicationCompletionRate = completionRate,

                serviceRequests = serviceRequests,
                referralConversions = refConversions,
                qualifiedReferrals = qualReferrals,
                sponsoredListingsCount = sponsoredCount,
                verifiedPartnerApplications = partnerApps,
                totalCommissionsRevenue = commissionsRevenue,
                totalReferralDisbursements = referralDisbursements,
                totalEstimatedRevenue = estimatedRevenue,

                countryPerformance = countryMetrics
            )
        }
    }
}
