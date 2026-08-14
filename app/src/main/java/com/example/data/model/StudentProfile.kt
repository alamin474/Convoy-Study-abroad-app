package com.example.data.model

data class StudentProfile(
    val id: String = "student_101",
    // Personal
    val fullName: String = "Alex Mercer",
    val dateOfBirth: String = "2002-05-14",
    val nationality: String = "United States",
    val countryOfResidence: String = "United States",
    val email: String = "alex.mercer@student.org",
    val phone: String = "+1 (555) 382-9102",

    // Academic
    val highestQualification: String = "Bachelor of Science in Computer Science",
    val institution: String = "University of California, Berkeley",
    val gpaScore: String = "3.85 / 4.0",
    val graduationYear: String = "2025",
    val intendedDegree: String = "Master's Degree",
    val intendedField: String = "Artificial Intelligence & Data Science",

    // English Qualifications
    val ieltsScore: String = "8.0 Overall (R:8.5, L:8.0, S:7.5, W:7.5)",
    val toeflScore: String = "105 iBT",
    val pteScore: String = "76 Academic",
    val otherEnglishQualification: String = "Medium of Instruction (MOI) Certificate",

    // Preferences
    val budgetRangePerYear: String = "$15,000 - $30,000 / year",
    val preferredCountries: List<String> = listOf("United Kingdom", "Germany", "Canada", "Australia"),
    val preferredDegree: String = "Postgraduate",
    val preferredField: String = "Artificial Intelligence & Software Systems",
    val selectedStudyLevel: String = "Postgraduate",
    val selectedSubjects: List<String> = listOf("Computer Science", "Artificial Intelligence"),
    val hasCompletedOnboarding: Boolean = false,

    // App state
    val targetIntake: String = "Fall 2026",
    val isVerified: Boolean = true
) {
    // Backward-compatible computed accessors
    val currentAcademicLevel: String get() = highestQualification
    val languageTestScore: String
        get() = when {
            ieltsScore.isNotBlank() -> "IELTS: $ieltsScore"
            toeflScore.isNotBlank() -> "TOEFL: $toeflScore"
            pteScore.isNotBlank() -> "PTE: $pteScore"
            else -> otherEnglishQualification.ifBlank { "Not Specified" }
        }
    val preferredFields: List<String>
        get() = listOf(intendedField, preferredField).filter { it.isNotBlank() }.distinct()
}
