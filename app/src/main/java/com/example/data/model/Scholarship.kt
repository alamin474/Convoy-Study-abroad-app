package com.example.data.model

enum class FundingType(val label: String) {
    ALL("All Types"),
    FULLY_FUNDED("Fully Funded"),
    MERIT("Merit-Based"),
    NEED_BASED("Need-Based"),
    GOVERNMENT("Government"),
    UNIVERSITY("University")
}

data class Scholarship(
    val scholarshipId: String,
    val name: String,
    val providerName: String = "Global Foundation / Ministry of Education",
    val country: String,
    val university: String = "Global Partner Institutions",
    val degreeLevel: String = "Master's Degree",
    val fieldOfStudy: String = "All Fields of Study",
    val scholarshipType: String = "Fully Funded",
    val isFullyFunded: Boolean = true,
    val tuitionCoverage: String = "100% Tuition Fee Covered",
    val stipend: String = "£1,400 / month living stipend",
    val accommodation: String = "Free On-Campus Housing / Allowance",
    val travelAllowance: String = "Roundtrip Economy Flights Included",
    val insurance: String = "Comprehensive Health Insurance Covered",
    val fundingDetails: String = "Full Tuition + Monthly Stipend + Travel",
    val eligibility: List<String> = emptyList(),
    val academicRequirements: String = "Minimum 3.0 GPA or 2:1 Honors Degree equivalent",
    val englishRequirements: String = "IELTS 6.5+ or TOEFL 90+ iBT or MOI waiver",
    val deadline: String = "03 November 2026",
    val applicationOpeningDate: String = "01 August 2026",
    val requiredDocuments: List<String> = emptyList(),
    val officialWebsite: String = "https://www.scholarships.org",
    val applicationUrl: String = "https://apply.convoy.edu/scholarships",
    val lastVerified: String = "2026-02-01",
    val status: EntityStatus = EntityStatus.PUBLISHED,
    val logoUrl: String = "https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=300&q=80",
    val description: String = "",
    val isFeatured: Boolean = false,
    val isSaved: Boolean = false
) {
    // Backward-compatible UI accessors
    val id: String get() = scholarshipId
    val title: String get() = name
    val provider: String get() = if (providerName.isNotBlank()) providerName else university
    val hostCountry: String get() = country
    val coverageAmount: String get() = if (fundingDetails.isNotBlank()) fundingDetails else tuitionCoverage
    val eligibleDegrees: List<String> get() = listOf(degreeLevel)
    val eligibilityCriteria: List<String> get() = eligibility
    val officialApplicationUrl: String get() = applicationUrl
    val fundingType: FundingType
        get() = when {
            scholarshipType.contains("Fully", ignoreCase = true) || isFullyFunded -> FundingType.FULLY_FUNDED
            scholarshipType.contains("Gov", ignoreCase = true) -> FundingType.GOVERNMENT
            scholarshipType.contains("Merit", ignoreCase = true) -> FundingType.MERIT
            scholarshipType.contains("Need", ignoreCase = true) -> FundingType.NEED_BASED
            else -> FundingType.UNIVERSITY
        }
}
