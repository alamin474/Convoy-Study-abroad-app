package com.example.data.model

data class Country(
    val countryId: String,
    val name: String,
    val flagEmoji: String,
    val universityCount: Int = 0,
    val avgTuitionPerYear: String = "€2,000 - €8,000 / year",
    val popularCities: List<String> = emptyList(),
    val imageUrl: String = "",
    val description: String = "",
    val overview: String = "",
    val popularStudyLevels: List<String> = listOf("Bachelor's", "Master's", "PhD", "Diploma"),
    val tuitionRange: String = "€1,500 - €9,000 / year",
    val livingCostOverview: String = "€500 - €900 / month",
    val scholarshipAvailability: String = "High",
    val studentVisaOverview: String = "Standard Student Visa required. Processing time 2-6 weeks.",
    val partTimeWorkInfo: String = "Up to 20 hours/week during term time, full-time on holidays.",
    val popularFields: List<String> = listOf("Computer Science", "Business", "Engineering", "Medicine"),
    val applicationInfo: String = "Direct university portal or central international admissions board.",
    val officialWebsite: String = "",
    val isFeatured: Boolean = true,
    val isLowTuitionDestination: Boolean = true,
    val status: EntityStatus = EntityStatus.PUBLISHED
) {
    fun toStudyDestination(): StudyDestination {
        return StudyDestination(
            id = countryId,
            countryName = name,
            flagEmoji = flagEmoji,
            universityCount = universityCount,
            avgTuitionPerYear = avgTuitionPerYear,
            popularCity = popularCities.joinToString(", "),
            imageUrl = imageUrl
        )
    }
}
