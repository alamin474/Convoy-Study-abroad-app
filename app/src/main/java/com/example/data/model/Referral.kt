package com.example.data.model

enum class ReferralStatus(val label: String) {
    PENDING("Pending"),
    QUALIFIED("Qualified"),
    APPROVED("Approved"),
    PAID("Paid"),
    REJECTED("Rejected")
}

data class Referral(
    val referralId: String,
    val referrerUserId: String,
    val referrerName: String = "",
    val referralCode: String = "",
    val referredUserId: String? = null,
    val referredStudentName: String = "",
    val referredEmail: String,
    val status: ReferralStatus = ReferralStatus.PENDING,
    val createdAt: String = "2026-02-01T12:00:00Z",
    val rewardAmount: Double = 100.0,
    val rewardAmountFormatted: String = "$100 USD",
    val paymentStatus: String = "Unpaid",
    val qualifyingApplicationId: String? = null,
    val qualificationDetails: String = "Pending Application Submission",
    val abuseFlag: Boolean = false,
    val abuseReason: String = "",
    val adminNote: String = ""
)
