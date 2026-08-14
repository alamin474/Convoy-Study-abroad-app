package com.example.data.model

data class Program(
    val programId: String,
    val universityId: String,
    val universityName: String,
    val title: String,
    val degreeLevel: String,
    val duration: String,
    val tuitionFee: String,
    val intakeSeasons: List<String>,
    val requirements: List<String>,
    val careerOutcomes: List<String> = emptyList(),
    val status: EntityStatus = EntityStatus.PUBLISHED
)
