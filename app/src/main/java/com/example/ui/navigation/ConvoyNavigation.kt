package com.example.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import com.example.ui.components.ConvoyBottomBar
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminLoginScreen
import com.example.ui.screens.applications.ApplicationsScreen
import com.example.ui.screens.chat.ChatConversationScreen
import com.example.ui.screens.chat.ChatHomeScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.StudyOnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.scholarships.ScholarshipDetailScreen
import com.example.ui.screens.scholarships.ScholarshipsScreen
import com.example.ui.screens.support.SupportHelpScreen
import com.example.ui.screens.universities.UniversitiesScreen
import com.example.ui.screens.universities.UniversityDetailScreen
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.ApplicationsViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.ScholarshipsViewModel
import com.example.ui.viewmodel.SupportViewModel
import com.example.ui.viewmodel.UniversitiesViewModel

@Composable
fun ConvoyApp(
    homeViewModel: HomeViewModel = viewModel(),
    universitiesViewModel: UniversitiesViewModel = viewModel(),
    scholarshipsViewModel: ScholarshipsViewModel = viewModel(),
    applicationsViewModel: ApplicationsViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel(),
    supportViewModel: SupportViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val chatUiState by chatViewModel.uiState.collectAsState()
    val isBottomBarVisible = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                ConvoyBottomBar(
                    currentRoute = currentRoute,
                    unreadChatCount = chatUiState.unreadTotalCount,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            // Main Bottom Bar Screens
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToUniversities = {
                        navController.navigate(Screen.Universities.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToScholarships = {
                        navController.navigate(Screen.Scholarships.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToUniversityDetail = { id ->
                        navController.navigate(NavDestinations.universityDetail(id))
                    },
                    onNavigateToScholarshipDetail = { id ->
                        navController.navigate(NavDestinations.scholarshipDetail(id))
                    },
                    onNavigateToSupport = {
                        navController.navigate(NavDestinations.HELP_SUPPORT)
                    }
                )
            }

            composable(Screen.Universities.route) {
                UniversitiesScreen(
                    viewModel = universitiesViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(NavDestinations.universityDetail(id))
                    }
                )
            }

            composable(Screen.Scholarships.route) {
                ScholarshipsScreen(
                    viewModel = scholarshipsViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(NavDestinations.scholarshipDetail(id))
                    }
                )
            }

            composable(Screen.Applications.route) {
                ApplicationsScreen(
                    viewModel = applicationsViewModel,
                    onNavigateToUniversities = {
                        navController.navigate(Screen.Universities.route)
                    },
                    onNavigateToSupport = {
                        navController.navigate(NavDestinations.HELP_SUPPORT)
                    },
                    onNavigateToChat = { app ->
                        chatViewModel.createOrOpenApplicationChat(
                            applicationId = app.id,
                            universityName = app.universityName,
                            programName = app.programName
                        ) { convId ->
                            navController.navigate(NavDestinations.chatConversation(convId))
                        }
                    }
                )
            }

            composable(Screen.Chat.route) {
                ChatHomeScreen(
                    viewModel = chatViewModel,
                    onNavigateToConversation = { convId ->
                        navController.navigate(NavDestinations.chatConversation(convId))
                    },
                    onNavigateToApplications = {
                        navController.navigate(Screen.Applications.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    authViewModel = authViewModel,
                    onNavigateToAdmin = {
                        navController.navigate(NavDestinations.ADMIN_DASHBOARD)
                    },
                    onNavigateToSupport = {
                        navController.navigate(NavDestinations.HELP_SUPPORT)
                    },
                    onNavigateToStudyOnboarding = {
                        navController.navigate(NavDestinations.STUDY_ONBOARDING)
                    }
                )
            }

            // Study Preference Onboarding Route
            composable(NavDestinations.STUDY_ONBOARDING) {
                StudyOnboardingScreen(
                    profileViewModel = profileViewModel,
                    authViewModel = authViewModel,
                    onContinue = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0)
                        }
                    }
                )
            }

            // Student Support & Contact Us Route
            composable(NavDestinations.HELP_SUPPORT) {
                SupportHelpScreen(
                    viewModel = supportViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Admin Control Center Routes
            composable(NavDestinations.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    adminViewModel = adminViewModel,
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(NavDestinations.ADMIN_LOGIN)
                    },
                    onNavigateToStudentView = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(NavDestinations.ADMIN_LOGIN) {
                AdminLoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(NavDestinations.ADMIN_DASHBOARD) {
                            popUpTo(NavDestinations.ADMIN_LOGIN) { inclusive = true }
                        }
                    },
                    onBackToApp = {
                        navController.popBackStack()
                    }
                )
            }

            // Detail Screens
            composable(
                route = NavDestinations.UNIVERSITY_DETAIL_ROUTE,
                arguments = listOf(navArgument("universityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val universityId = backStackEntry.arguments?.getString("universityId") ?: ""
                UniversityDetailScreen(
                    universityId = universityId,
                    universitiesViewModel = universitiesViewModel,
                    applicationsViewModel = applicationsViewModel,
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onApplySuccess = {
                        navController.navigate(Screen.Applications.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = NavDestinations.SCHOLARSHIP_DETAIL_ROUTE,
                arguments = listOf(navArgument("scholarshipId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scholarshipId = backStackEntry.arguments?.getString("scholarshipId") ?: ""
                ScholarshipDetailScreen(
                    scholarshipId = scholarshipId,
                    viewModel = scholarshipsViewModel,
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = NavDestinations.CHAT_CONVERSATION_ROUTE,
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                ChatConversationScreen(
                    conversationId = conversationId,
                    viewModel = chatViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
