package com.example.data.model

import java.util.UUID

enum class PartnerType(val displayName: String) {
    UNIVERSITY("University"),
    AUTHORIZED_EDUCATION_PROVIDER("Authorized Education Provider"),
    RECRUITMENT_PARTNER("Recruitment Partner"),
    OTHER_VERIFIED_PARTNER("Other Verified Partner")
}

enum class PartnershipStatus(val displayName: String) {
    PROSPECT("Prospect"),
    CONTACTED("Contacted"),
    NEGOTIATING("Negotiating"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    TERMINATED("Terminated")
}

enum class CommissionStatus(val displayName: String) {
    NOT_APPLICABLE("Not Applicable"),
    PENDING("Pending"),
    ELIGIBLE("Eligible"),
    RECEIVED("Received"),
    CANCELLED("Cancelled")
}

data class Partner(
    val partnerId: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PartnerType,
    val country: String,
    val contactInfo: String,
    val website: String,
    val partnershipStatus: PartnershipStatus = PartnershipStatus.PROSPECT,
    val agreementStatus: String = "No Formal Agreement",
    val commissionInfo: String = "None / Unverified",
    val worksWithAgencies: Boolean = true,
    val agencyPartnerName: String = "Convoy Global Education Network",
    val contactPerson: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val contractDocumentUrl: String = "https://convoy.admin/contracts/agreement.pdf",
    val contractStartDate: String = "2025-01-01",
    val contractExpiryDate: String = "2028-12-31",
    val internalAdminNotes: String = "",
    val notes: String = "",
    val createdDate: String = "2026-02-09",
    val lastUpdated: String = "2026-02-09"
) {
    val isVerifiedActivePartnership: Boolean
        get() = partnershipStatus == PartnershipStatus.ACTIVE && agreementStatus.contains("Signed", ignoreCase = true)
}
