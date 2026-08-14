package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FundingType
import com.example.data.model.ListingEntityType
import com.example.data.model.Scholarship
import com.example.data.model.SponsoredListing
import com.example.data.model.User
import com.example.data.repository.RemoteScholarshipRepository
import com.example.data.repository.ScholarshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ScholarshipFilterState(
    val query: String = "",
    val country: String = "All",
    val degree: String = "All",
    val fundingType: FundingType = FundingType.ALL,
    val deadlineFilter: String = "All"
)

data class ScholarshipsUiState(
    val searchQuery: String = "",
    val selectedCountry: String = "All",
    val selectedDegree: String = "All",
    val selectedFundingType: FundingType = FundingType.ALL,
    val selectedDeadlineFilter: String = "All",
    val scholarships: List<Scholarship> = emptyList(),
    val activeSponsoredMap: Map<String, String> = emptyMap(),
    val selectedScholarship: Scholarship? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ScholarshipsViewModel(
    private val scholarshipRepo: ScholarshipRepository = RemoteScholarshipRepository()
) : ViewModel() {

    private val _filterState = MutableStateFlow(ScholarshipFilterState())
    private val _selectedScholarshipId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(ScholarshipsUiState(isLoading = true))

    val uiState: StateFlow<ScholarshipsUiState> = _uiState.asStateFlow()

    init {
        observeScholarships()
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        observeScholarships()
    }

    private fun observeScholarships() {
        val currentDate = "2026-02-09"
        viewModelScope.launch {
            try {
                combine(
                    scholarshipRepo.getScholarships(),
                    scholarshipRepo.getSponsoredListings(),
                    _filterState
                ) { schs: List<Scholarship>, spListings: List<SponsoredListing>, filters: ScholarshipFilterState ->
                    val activeMap = spListings
                        .filter { it.isCurrentlyActive(currentDate) && it.entityType == ListingEntityType.SCHOLARSHIP }
                        .associate { it.entityId to it.listingType.badgeLabel }
                        .toMutableMap()

                    spListings
                        .filter { it.isCurrentlyActive(currentDate) && it.entityType == ListingEntityType.SCHOLARSHIP }
                        .forEach { activeMap[it.entityName] = it.listingType.badgeLabel }

                    val filtered = schs.filter { s ->
                        val matchesQuery = filters.query.isBlank() ||
                                s.name.contains(filters.query, ignoreCase = true) ||
                                s.provider.contains(filters.query, ignoreCase = true) ||
                                s.university.contains(filters.query, ignoreCase = true) ||
                                s.country.contains(filters.query, ignoreCase = true) ||
                                s.fieldOfStudy.contains(filters.query, ignoreCase = true)

                        val matchesCountry = filters.country == "All" || s.country.equals(filters.country, ignoreCase = true)
                        val matchesDegree = filters.degree == "All" || s.degreeLevel.contains(filters.degree, ignoreCase = true)
                        val matchesType = filters.fundingType == FundingType.ALL || s.fundingType == filters.fundingType
                        val matchesDeadline = when (filters.deadlineFilter) {
                            "2026 Deadlines" -> s.deadline.contains("2026")
                            "2027 Deadlines" -> s.deadline.contains("2027")
                            else -> true
                        }

                        matchesQuery && matchesCountry && matchesDegree && matchesType && matchesDeadline
                    }

                    Triple(filtered, activeMap, filters)
                }.collect { (filtered, activeMap, filters) ->
                    val selected = _selectedScholarshipId.value?.let { id -> filtered.find { it.id == id } }
                    _uiState.value = _uiState.value.copy(
                        searchQuery = filters.query,
                        selectedCountry = filters.country,
                        selectedDegree = filters.degree,
                        selectedFundingType = filters.fundingType,
                        selectedDeadlineFilter = filters.deadlineFilter,
                        scholarships = filtered,
                        activeSponsoredMap = activeMap,
                        selectedScholarship = selected,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load scholarships"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun onCountrySelect(country: String) {
        _filterState.value = _filterState.value.copy(country = country)
    }

    fun onDegreeSelect(degree: String) {
        _filterState.value = _filterState.value.copy(degree = degree)
    }

    fun onFundingTypeSelect(type: FundingType) {
        _filterState.value = _filterState.value.copy(fundingType = type)
    }

    fun onDeadlineFilterSelect(deadlineFilter: String) {
        _filterState.value = _filterState.value.copy(deadlineFilter = deadlineFilter)
    }

    fun toggleSave(id: String, currentUser: User? = null) {
        scholarshipRepo.toggleSave(id, currentUser)
    }

    fun requestInformation(
        scholarship: Scholarship,
        currentUser: User?,
        phone: String = "",
        message: String = ""
    ): Pair<Boolean, String> {
        return scholarshipRepo.requestInformation(scholarship, currentUser, phone, message)
    }

    fun selectScholarship(id: String?) {
        _selectedScholarshipId.value = id
        if (id != null) {
            viewModelScope.launch {
                scholarshipRepo.getScholarshipById(id).collect { sch ->
                    _uiState.value = _uiState.value.copy(selectedScholarship = sch)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(selectedScholarship = null)
        }
    }
}
