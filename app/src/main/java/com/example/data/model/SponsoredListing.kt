package com.example.data.model

import java.util.UUID

enum class ListingEntityType(val displayName: String) {
    UNIVERSITY("University"),
    SCHOLARSHIP("Scholarship"),
    PROGRAM("Program")
}

enum class ListingType(val displayName: String, val badgeLabel: String) {
    FEATURED("Featured Listing", "FEATURED"),
    SPONSORED("Sponsored Listing", "SPONSORED")
}

enum class ListingStatus(val displayName: String) {
    ACTIVE("Active"),
    SCHEDULED("Scheduled"),
    EXPIRED("Expired"),
    PAUSED("Paused")
}

data class SponsoredListing(
    val listingId: String = "sp_${UUID.randomUUID().toString().take(6)}",
    val entityType: ListingEntityType,
    val entityId: String,
    val entityName: String,
    val listingType: ListingType = ListingType.FEATURED,
    val startDate: String = "2026-02-01",
    val endDate: String = "2026-12-31",
    val placement: String = "Search & Discovery Top",
    val status: ListingStatus = ListingStatus.ACTIVE,
    val sponsorPartner: String = "Direct University Partnership",
    val internalNotes: String = "",
    val createdDate: String = "2026-02-09"
) {
    fun computedStatus(currentDate: String = "2026-02-09"): ListingStatus {
        if (status == ListingStatus.PAUSED) return ListingStatus.PAUSED
        return when {
            endDate < currentDate -> ListingStatus.EXPIRED
            startDate > currentDate -> ListingStatus.SCHEDULED
            else -> ListingStatus.ACTIVE
        }
    }

    fun isCurrentlyActive(currentDate: String = "2026-02-09"): Boolean {
        return computedStatus(currentDate) == ListingStatus.ACTIVE
    }
}
