package com.example.data.model

import java.util.UUID

enum class AnalyticsEventType {
    UNIVERSITY_VIEW,
    SCHOLARSHIP_VIEW,
    COUNTRY_VIEW,
    SEARCH_PERFORMED,
    UNIVERSITY_SAVED,
    PROFILE_CREATED,
    INFORMATION_REQUESTED,
    APPLICATION_STARTED,
    APPLICATION_SUBMITTED,
    SERVICE_REQUESTED
}

enum class AnalyticsDateFilter(val displayName: String, val days: Int?) {
    TODAY("Today", 1),
    DAYS_7("7 days", 7),
    DAYS_30("30 days", 30),
    DAYS_90("90 days", 90),
    CUSTOM("Custom range", null)
}

data class AnalyticsEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: AnalyticsEventType,
    val targetId: String = "",
    val targetName: String = "",
    val country: String = "",
    val universityName: String = "",
    val programName: String = "",
    val searchQuery: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class CountryPerformanceMetrics(
    val countryName: String,
    val flagEmoji: String,
    val universityCount: Int = 0,
    val applicationsCount: Int = 0,
    val leadsCount: Int = 0,
    val viewsCount: Int = 0,
    val conversionRate: Double = 0.0
)

data class BusinessAnalyticsMetrics(
    val selectedDateFilter: AnalyticsDateFilter = AnalyticsDateFilter.DAYS_30,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,

    // USER METRICS
    val totalUsers: Int = 0,
    val newUsers: Int = 0,
    val activeUsers: Int = 0,

    // DISCOVERY
    val universityViews: Int = 0,
    val scholarshipViews: Int = 0,
    val countryViews: Int = 0,
    val searchActivityCount: Int = 0,
    val savedUniversitiesCount: Int = 0,
    val topSearches: List<Pair<String, Int>> = emptyList(),

    // LEAD
    val profileCreations: Int = 0,
    val informationRequests: Int = 0,
    val applicationStarts: Int = 0,

    // CONVERSION
    val applicationsSubmitted: Int = 0,
    val applicationsByCountry: Map<String, Int> = emptyMap(),
    val applicationsByUniversity: Map<String, Int> = emptyMap(),
    val applicationsByProgram: Map<String, Int> = emptyMap(),
    val applicationCompletionRate: Double = 0.0,

    // BUSINESS
    val serviceRequests: Int = 0,
    val referralConversions: Int = 0,
    val qualifiedReferrals: Int = 0,
    val sponsoredListingsCount: Int = 0,
    val verifiedPartnerApplications: Int = 0,
    val totalCommissionsRevenue: Double = 0.0,
    val totalReferralDisbursements: Double = 0.0,
    val totalEstimatedRevenue: Double = 0.0,

    // COUNTRY PERFORMANCE
    val countryPerformance: List<CountryPerformanceMetrics> = emptyList()
)
