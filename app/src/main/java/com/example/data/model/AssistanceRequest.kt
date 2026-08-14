package com.example.data.model

enum class AssistanceType(val title: String, val description: String) {
    APPLICATION_GUIDANCE(
        "Application Guidance",
        "Step-by-step guidance on intake timelines, eligibility requirements, and application procedures."
    ),
    UNIVERSITY_SELECTION(
        "University Selection Assistance",
        "Personalized shortlisting of reach, target, and safety universities aligned with your profile and budget."
    ),
    DOCUMENT_CHECKLIST(
        "Document Checklist & Verification",
        "Comprehensive review of academic transcripts, SOP, letters of recommendation, and financial proofs."
    ),
    APPLICATION_REVIEW(
        "Pre-Submission Application Review",
        "Expert inspection of your complete university application before final submission to minimize errors."
    ),
    STATUS_SUPPORT(
        "Application Status & Follow-up Support",
        "Dedicated tracking and direct counselor follow-ups with university admissions offices on your behalf."
    )
}

enum class AssistanceStatus(val displayName: String) {
    REQUESTED("Requested"),
    UNDER_REVIEW("Under Review"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class AssistanceRequest(
    val requestId: String = "",
    val userId: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val studentPhone: String = "",
    val serviceType: AssistanceType = AssistanceType.APPLICATION_GUIDANCE,
    val targetUniversityName: String = "",
    val targetProgramName: String = "",
    val studentNotes: String = "",
    val status: AssistanceStatus = AssistanceStatus.REQUESTED,
    val assignedCounselor: String = "Unassigned",
    val internalNotes: String = "",
    val guidanceMessages: List<GuidanceMessage> = emptyList(),
    val requestedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class GuidanceMessage(
    val id: String = "",
    val senderName: String = "",
    val isFromAdmin: Boolean = false,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
