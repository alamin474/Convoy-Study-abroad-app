package com.example.data.model

enum class RequirementType(val displayName: String, val category: String) {
    PASSPORT("Passport", "Identity"),
    ACADEMIC_CERTIFICATE("Academic Certificate", "Academics"),
    ACADEMIC_TRANSCRIPT("Academic Transcript", "Academics"),
    IELTS("IELTS", "English Proficiency"),
    TOEFL("TOEFL", "English Proficiency"),
    PTE("PTE", "English Proficiency"),
    MOI("MOI (Medium of Instruction)", "English Proficiency"),
    CV("CV / Resume", "Personal"),
    SOP("Statement of Purpose (SOP)", "Personal"),
    RECOMMENDATION_LETTER("Recommendation Letter", "Reference"),
    BANK_STATEMENT("Bank Statement", "Financial"),
    FINANCIAL_DOCUMENTS("Financial Documents", "Financial"),
    PHOTOGRAPH("Photograph", "Identity"),
    OTHER("Other Documents", "Miscellaneous")
}

data class UniversityRequirement(
    val requirementId: String,
    val universityId: String, // "All" or specific university ID like "uni_1"
    val universityName: String = "All Universities",
    val programName: String = "All Programs", // "All Programs" or specific program
    val intakeSeason: String = "All Intakes", // "All Intakes" or specific intake
    val type: RequirementType = RequirementType.PASSPORT,
    val title: String = "",
    val isRequired: Boolean = true, // true = Required, false = Optional
    val minScore: String = "", // e.g. "6.0", "90 iBT", "62"
    val instructions: String = "",
    val isPublished: Boolean = true
)
