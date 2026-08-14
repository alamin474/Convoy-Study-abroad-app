package com.example.data.model

enum class EntityStatus {
    PUBLISHED,
    DRAFT,
    ARCHIVED
}

enum class TuitionCategory(val label: String) {
    UNDER_10K("Under $10,000 / yr"),
    FROM_10K_TO_25K("$10,000 - $25,000 / yr"),
    OVER_25K("Over $25,000 / yr")
}

data class University(
    val universityId: String,
    val name: String,
    val country: String,
    val city: String,
    val universityType: String = "Public Research",
    val ranking: Int,
    val tuitionFee: String,
    val applicationFee: String = "$75 USD",
    val description: String,
    val programs: List<String> = emptyList(),
    val degreeLevels: List<String> = listOf("Bachelor's", "Master's", "Doctorate"),
    val intakes: List<String> = listOf("Fall 2026", "Spring 2027"),
    val admissionRequirements: List<String> = emptyList(),
    val englishRequirements: String = "IELTS 6.5+ / TOEFL 90+",
    val ieltsRequirement: String = "6.5 Overall",
    val toeflRequirement: String = "90 iBT",
    val pteRequirement: String = "62 Academic",
    val englishWaiverInfo: String = "MOI accepted if prior degree completed in English.",
    val scholarships: List<String> = emptyList(),
    val campusImages: List<String> = emptyList(),
    val officialWebsite: String = "https://www.university.edu",
    val applicationUrl: String = "https://apply.convoy.edu",
    val lastVerified: String = "2026-02-01",
    val status: EntityStatus = EntityStatus.PUBLISHED,
    val flagEmoji: String = "🌐",
    val logoUrl: String = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=300&q=80",
    val bannerUrl: String = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800&q=80",
    val acceptanceRatePercent: Int = 25,
    val isFeatured: Boolean = false,
    val isLowTuition: Boolean = false,
    val isBookmarked: Boolean = false
) {
    // Backward-compatible UI accessors
    val id: String get() = universityId
    val worldRanking: Int get() = ranking
    val annualTuitionUsd: Int
        get() {
            val digitsOnly = tuitionFee.filter { it.isDigit() }
            return digitsOnly.toIntOrNull() ?: 20000
        }
    val tuitionCategory: TuitionCategory
        get() = when {
            annualTuitionUsd < 10000 -> TuitionCategory.UNDER_10K
            annualTuitionUsd in 10000..25000 -> TuitionCategory.FROM_10K_TO_25K
            else -> TuitionCategory.OVER_25K
        }
    val hasScholarships: Boolean get() = scholarships.isNotEmpty()
    val popularPrograms: List<String> get() = programs
    val campusOverview: String get() = description
    val applicationDeadline: String get() = intakes.firstOrNull() ?: "15 Oct 2026"
}
