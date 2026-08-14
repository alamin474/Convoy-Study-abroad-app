package com.example.data.model

enum class SupportCategory(val displayName: String, val description: String) {
    GENERAL("General Inquiry", "Questions about Convoy services, platform features, or general guidance"),
    UNIVERSITY("University Inquiry", "Questions about specific universities, admissions criteria, or intakes"),
    SCHOLARSHIP("Scholarship Inquiry", "Questions regarding funding, grants, or scholarship eligibility"),
    APPLICATION("Application Inquiry", "Help with your active or draft university applications"),
    DOCUMENT("Document Support", "Guidance on document verification, SOP review, or translations"),
    TECHNICAL("Technical Support", "Bug reports, login assistance, or app performance issues"),
    OTHER("Other", "Any other inquiry or general feedback")
}

enum class SupportStatus(val displayName: String) {
    NEW("New"),
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    WAITING_FOR_STUDENT("Waiting for Student"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

data class SupportReply(
    val replyId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val isAdmin: Boolean = false,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SupportRequest(
    val requestId: String = "",
    val userId: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val category: SupportCategory = SupportCategory.GENERAL,
    val subject: String = "",
    val message: String = "",
    val relatedApplicationId: String? = null,
    val relatedUniversity: String? = null,
    val relatedDocumentContext: String? = null,
    val attachmentName: String? = null,
    val attachmentUrl: String? = null,
    val status: SupportStatus = SupportStatus.NEW,
    val replies: List<SupportReply> = emptyList(),
    val internalNotes: String = "",
    val assignedStaff: String = "Unassigned",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SupportConfig(
    val supportEmail: String = "support@convoy.edu",
    val phoneNumber: String = "+1 (800) 555-2668",
    val whatsappNumber: String = "+1 (800) 555-2668",
    val officeHours: String = "Monday – Friday, 9:00 AM – 6:00 PM EST",
    val linkedinUrl: String = "https://linkedin.com/company/convoy-edu",
    val twitterUrl: String = "https://x.com/convoy_edu",
    val instagramUrl: String = "https://instagram.com/convoy_edu",
    val lastUpdatedBy: String = "System Admin",
    val updatedAt: Long = System.currentTimeMillis()
)
