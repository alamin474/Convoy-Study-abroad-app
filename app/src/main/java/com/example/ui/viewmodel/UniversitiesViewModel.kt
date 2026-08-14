package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ListingEntityType
import com.example.data.model.SponsoredListing
import com.example.data.model.TuitionCategory
import com.example.data.model.University
import com.example.data.model.UniversityRequirement
import com.example.data.model.User
import com.example.data.repository.RemoteUniversityRepository
import com.example.data.repository.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class UniversitiesFilterState(
    val searchQuery: String = "",
    val selectedCountry: String = "All",
    val selectedTuition: TuitionCategory? = null,
    val selectedMaxRanking: Int? = null, // e.g., 50, 100, 500
    val scholarshipOnly: Boolean = false
)

data class UniversitiesUiState(
    val filterState: UniversitiesFilterState = UniversitiesFilterState(),
    val universities: List<University> = emptyList(),
    val activeSponsoredMap: Map<String, String> = emptyMap(),
    val availableCountries: List<String> = listOf("All", "United Kingdom", "Germany", "Canada", "Australia", "Switzerland", "Singapore"),
    val selectedUniversity: University? = null,
    val selectedUniversityRequirements: List<UniversityRequirement> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UniversitiesViewModel(
    private val universityRepo: UniversityRepository = RemoteUniversityRepository()
) : ViewModel() {

    private val _filterState = MutableStateFlow(UniversitiesFilterState())
    private val _selectedUniversityId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(UniversitiesUiState(isLoading = true))

    val uiState: StateFlow<UniversitiesUiState> = _uiState.asStateFlow()

    init {
        observeUniversities()
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        observeUniversities()
    }

    private fun observeUniversities() {
        val currentDate = "2026-02-09"
        viewModelScope.launch {
            try {
                kotlinx.coroutines.flow.combine(
                    _filterState,
                    universityRepo.getUniversities(),
                    universityRepo.getSponsoredListings()
                ) { filter: UniversitiesFilterState, unis: List<University>, spListings: List<SponsoredListing> ->
                    val activeMap = spListings
                        .filter { it.isCurrentlyActive(currentDate) && it.entityType == ListingEntityType.UNIVERSITY }
                        .associate { it.entityId to it.listingType.badgeLabel }
                        .toMutableMap()

                    spListings
                        .filter { it.isCurrentlyActive(currentDate) && it.entityType == ListingEntityType.UNIVERSITY }
                        .forEach { activeMap[it.entityName] = it.listingType.badgeLabel }

                    val filtered = unis.filter { u ->
                        val matchesQuery = filter.searchQuery.isEmpty() ||
                                u.name.contains(filter.searchQuery, ignoreCase = true) ||
                                u.city.contains(filter.searchQuery, ignoreCase = true) ||
                                u.programs.any { it.contains(filter.searchQuery, ignoreCase = true) }

                        val matchesCountry = filter.selectedCountry == "All" || u.country.equals(filter.selectedCountry, ignoreCase = true)
                        val matchesTuition = filter.selectedTuition == null || u.tuitionCategory == filter.selectedTuition
                        val matchesRanking = filter.selectedMaxRanking == null || u.ranking <= filter.selectedMaxRanking
                        val matchesScholarship = !filter.scholarshipOnly || u.hasScholarships

                        matchesQuery && matchesCountry && matchesTuition && matchesRanking && matchesScholarship
                    }

                    Triple(filter, filtered, activeMap)
                }.collect { (filter, filteredUnis, activeMap) ->
                    val selected = _selectedUniversityId.value?.let { id -> filteredUnis.find { it.id == id } }
                    _uiState.value = _uiState.value.copy(
                        filterState = filter,
                        universities = filteredUnis,
                        activeSponsoredMap = activeMap,
                        selectedUniversity = selected,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load universities"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun onCountrySelect(country: String) {
        _filterState.value = _filterState.value.copy(selectedCountry = country)
    }

    fun onTuitionSelect(tuitionCategory: TuitionCategory?) {
        _filterState.value = _filterState.value.copy(selectedTuition = tuitionCategory)
    }

    fun onRankingSelect(maxRanking: Int?) {
        _filterState.value = _filterState.value.copy(selectedMaxRanking = maxRanking)
    }

    fun onScholarshipOnlyToggle(scholarshipOnly: Boolean) {
        _filterState.value = _filterState.value.copy(scholarshipOnly = scholarshipOnly)
    }

    fun clearFilters() {
        _filterState.value = UniversitiesFilterState()
    }

    fun toggleBookmark(id: String, currentUser: User? = null) {
        universityRepo.toggleBookmark(id, currentUser)
    }

    fun requestInformation(
        university: University,
        currentUser: User?,
        phone: String = "",
        message: String = ""
    ): Pair<Boolean, String> {
        return universityRepo.requestInformation(university, currentUser, phone, message)
    }

    fun createStartApplicationLead(
        university: University,
        currentUser: User?
    ): Pair<Boolean, String> {
        return universityRepo.createStartApplicationLead(university, currentUser)
    }

    fun selectUniversity(id: String?) {
        _selectedUniversityId.value = id
        if (id != null) {
            viewModelScope.launch {
                universityRepo.getUniversityById(id).collect { uni ->
                    _uiState.value = _uiState.value.copy(selectedUniversity = uni)
                }
            }
            viewModelScope.launch {
                universityRepo.getUniversityRequirements(id).collect { reqs ->
                    _uiState.value = _uiState.value.copy(selectedUniversityRequirements = reqs)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                selectedUniversity = null,
                selectedUniversityRequirements = emptyList()
            )
        }
    }
}
