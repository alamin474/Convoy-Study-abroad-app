package com.example.data.model

data class Announcement(
    val announcementId: String,
    val title: String,
    val content: String,
    val targetAudience: String = "ALL_STUDENTS", // ALL_STUDENTS, APPLICANTS, COUNSELORS
    val date: String = "2026-02-08",
    val isPinned: Boolean = false,
    val actionUrl: String? = null
)
