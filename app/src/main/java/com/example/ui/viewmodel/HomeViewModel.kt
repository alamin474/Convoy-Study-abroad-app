package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Country
import com.example.data.model.Scholarship
import com.example.data.model.StudyDestination
import com.example.data.model.University
import com.example.data.repository.ProfileRepository
import com.example.data.repository.RemoteProfileRepository
import com.example.data.repository.RemoteScholarshipRepository
import com.example.data.repository.RemoteUniversityRepository
import com.example.data.repository.ScholarshipRepository
import com.example.data.repository.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val searchQuery: String = "",
    val featuredUniversities: List<University> = emptyList(),
    val featuredScholarships: List<Scholarship> = emptyList(),
    val studyDestinations: List<StudyDestination> = emptyList(),
    val countries: List<Country> = emptyList(),
    val lowTuitionUniversities: List<University> = emptyList(),
    val upcomingDeadlines: List<DeadlineItem> = emptyList(),
    val latestOpportunities: List<OpportunityItem> = emptyList(),
    val isLoading: Boolean = false
)

data class DeadlineItem(
    val id: String,
    val title: String,
    val institution: String,
    val flagEmoji: String,
    val daysRemaining: Int,
    val category: String
)

data class OpportunityItem(
    val id: String,
    val title: String,
    val badge: String,
    val description: String,
    val actionText: String
)

class HomeViewModel(
    private val universityRepo: UniversityRepository = RemoteUniversityRepository(),
    private val scholarshipRepo: ScholarshipRepository = RemoteScholarshipRepository(),
    private val profileRepo: ProfileRepository = RemoteProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                universityRepo.getUniversities(),
                scholarshipRepo.getScholarships(),
                profileRepo.getStudyDestinations(),
                profileRepo.getCountries(),
                universityRepo.getLowTuitionUniversities()
            ) { unis, scholarships, destinations, countriesList, lowTuitionUnis ->
                val deadlines = listOf(
                    DeadlineItem("d1", "Chevening UK Scholarship", "UK FCDO", "🇬🇧", 85, "Scholarship"),
                    DeadlineItem("d2", "Oxford MSc Computer Science", "University of Oxford", "🇬🇧", 68, "University"),
                    DeadlineItem("d3", "DAAD Master's Germany", "DAAD", "🇩🇪", 68, "Scholarship"),
                    DeadlineItem("d4", "TUM Informatics Application", "TU Munich", "🇩🇪", 112, "University")
                )

                val opportunities = listOf(
                    OpportunityItem("op1", "100% Tuition Fee Waiver Intakes", "Global", "Discover 45+ European public universities with zero tuition fees for Fall 2026.", "Explore Low-Tuition"),
                    OpportunityItem("op2", "IELTS Waiver Available", "UK & Canada", "Selected partner institutions offer admission based on English Medium of Instruction certificates.", "Check Criteria"),
                    OpportunityItem("op3", "Convoy Fast-Track Processing", "Exclusive", "Get personalized document pre-screening and application submission within 48 hours.", "Get Started")
                )

                HomeUiState(
                    searchQuery = _uiState.value.searchQuery,
                    featuredUniversities = unis.filter { it.isFeatured },
                    featuredScholarships = scholarships.filter { it.isFeatured },
                    studyDestinations = destinations,
                    countries = countriesList,
                    lowTuitionUniversities = lowTuitionUnis,
                    upcomingDeadlines = deadlines,
                    latestOpportunities = opportunities,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleUniversityBookmark(id: String) {
        universityRepo.toggleBookmark(id)
    }

    fun toggleScholarshipSave(id: String) {
        scholarshipRepo.toggleSave(id)
    }
}
