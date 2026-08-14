package com.example.data.model

enum class UserRole {
    STUDENT,
    ADMIN,
    COUNSELOR
}

data class AcademicInfo(
    val currentLevel: String = "Bachelor of Science",
    val majorField: String = "Computer Engineering",
    val gpaScore: String = "3.85 / 4.0",
    val graduationYear: String = "2026",
    val targetIntake: String = "Fall 2026",
    val preferredCountries: List<String> = listOf("United Kingdom", "Germany", "Canada", "Switzerland"),
    val preferredFields: List<String> = listOf("Computer Science", "Artificial Intelligence", "Robotics"),
    val languageTestType: String = "IELTS Academic",
    val languageTestScore: String = "8.0",
    val studyLevel: String = "Postgraduate",
    val selectedSubjects: List<String> = listOf("Computer Science", "Artificial Intelligence")
)

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.STUDENT,
    val phone: String = "+1 (555) 382-9102",
    val nationality: String = "International Student",
    val profileImageUrl: String? = null,
    val academicInfo: AcademicInfo = AcademicInfo(),
    val hasCompletedOnboarding: Boolean = false,
    val isGoogleUser: Boolean = false,
    val createdAt: String = "2026-01-15T10:00:00Z"
)
