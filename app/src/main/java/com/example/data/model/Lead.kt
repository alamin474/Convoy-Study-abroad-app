package com.example.data.model

enum class LeadStatus(val label: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    QUALIFIED("Qualified"),
    CONVERTED("Converted"),
    CLOSED("Closed")
}

data class Lead(
    val leadId: String,
    val studentUserId: String,
    val studentName: String,
    val studentEmail: String,
    val studentPhone: String = "",
    val country: String = "",
    val universityId: String? = null,
    val universityName: String? = null,
    val scholarshipId: String? = null,
    val scholarshipName: String? = null,
    val source: String,
    val date: String,
    val status: LeadStatus = LeadStatus.NEW,
    val notes: String = ""
)
