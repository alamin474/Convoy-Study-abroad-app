package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Universities : Screen(
        route = "universities",
        title = "Universities",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    )

    data object Scholarships : Screen(
        route = "scholarships",
        title = "Scholarships",
        selectedIcon = Icons.Filled.WorkspacePremium,
        unselectedIcon = Icons.Outlined.WorkspacePremium
    )

    data object Applications : Screen(
        route = "applications",
        title = "Applications",
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment
    )

    data object Chat : Screen(
        route = "chat",
        title = "Chat",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline
    )

    data object Profile : Screen(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val bottomNavItems = listOf(Home, Universities, Scholarships, Applications, Chat, Profile)
    }
}

object NavDestinations {
    const val STUDY_ONBOARDING = "study_onboarding"

    const val UNIVERSITY_DETAIL_ROUTE = "university_detail/{universityId}"
    fun universityDetail(id: String) = "university_detail/$id"

    const val SCHOLARSHIP_DETAIL_ROUTE = "scholarship_detail/{scholarshipId}"
    fun scholarshipDetail(id: String) = "scholarship_detail/$id"

    const val CHAT_CONVERSATION_ROUTE = "chat_conversation/{conversationId}"
    fun chatConversation(id: String) = "chat_conversation/$id"

    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_LOGIN = "admin_login"

    const val ABOUT_CONVOY = "about_convoy"
    const val PRIVACY_POLICY = "privacy_policy"
    const val TERMS_CONDITIONS = "terms_conditions"
    const val HELP_SUPPORT = "help_support"
}
