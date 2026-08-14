package com.example.data.model

enum class ActivityType {
    APPLICATION_SUBMITTED,
    DOCUMENT_UPLOADED,
    UNIVERSITY_ADDED,
    SCHOLARSHIP_UPDATED,
    STUDENT_REGISTERED,
    REFERRAL_CONVERTED,
    ANNOUNCEMENT_POSTED
}

data class RecentActivity(
    val activityId: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val type: ActivityType,
    val actorName: String
)
