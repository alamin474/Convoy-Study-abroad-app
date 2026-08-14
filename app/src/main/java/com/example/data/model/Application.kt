package com.example.data.model

import java.util.UUID

enum class ApplicationStatus(val label: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    DOCUMENTS_REQUIRED("Documents Required"),
    PROCESSING("Processing"),
    APPLIED("Applied"),
    OFFER_RECEIVED("Offer Received"),
    VISA_PROCESSING("Visa Processing"),
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    companion object {
        val IN_REVIEW = UNDER_REVIEW
        val ACTION_REQUIRED = DOCUMENTS_REQUIRED
    }
}

enum class DocumentCategory(val label: String) {
    PASSPORT("Passport & ID"),
    TRANSCRIPT("Academic Transcripts"),
    SOP("Statement of Purpose"),
    LOR("Letters of Recommendation"),
    LANGUAGE_TEST("Language Score (IELTS/TOEFL)"),
    OTHER("Additional Certificates")
}

data class StudentDocument(
    val documentId: String,
    val userId: String,
    val title: String,
    val category: DocumentCategory,
    val fileName: String? = null,
    val fileSize: String? = null,
    val uploadDate: String? = null,
    val fileUrl: String? = null,
    val isUploaded: Boolean = false
) {
    val id: String get() = documentId
}

data class StatusUpdate(
    val updateId: String = UUID.randomUUID().toString(),
    val status: ApplicationStatus,
    val timestamp: String,
    val note: String = ""
)

data class Application(
    val applicationId: String,
    val userId: String,
    val universityId: String = "uni_1",
    val universityName: String,
    val universityLogoUrl: String = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=300&q=80",
    val programId: String = "prog_1",
    val programName: String,
    val degreeLevel: String,
    val intakeSeason: String,
    val country: String,
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val submittedDate: String? = null,
    val nextMilestone: String = "Complete documentation and pay fee",
    val completionPercentage: Float = 0.5f,
    val updatedAt: String = "2026-02-08",
    val studentNotes: String = "",
    val internalNotes: String = "",
    val requiredDocuments: List<String> = listOf("Passport Scan", "Academic Transcripts", "Statement of Purpose", "Recommendation Letter", "English Proficiency Score"),
    val requestedDocuments: List<String> = emptyList(),
    val statusHistory: List<StatusUpdate> = emptyList(),
    // Partner Attribution & Commission Tracking
    val partnerId: String? = null,
    val partnerName: String? = null,
    val applicationSource: String = "Direct Organic",
    val commissionEligible: Boolean = false,
    val commissionStatus: CommissionStatus = CommissionStatus.NOT_APPLICABLE,
    val commissionAmount: String? = null
) {
    val id: String get() = applicationId
}
