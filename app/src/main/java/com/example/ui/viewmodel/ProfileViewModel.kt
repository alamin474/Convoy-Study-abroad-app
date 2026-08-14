package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.StudentProfile
import com.example.data.repository.ProfileRepository
import com.example.data.repository.RemoteProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val studentProfile: StudentProfile? = null,
    val notificationsEnabled: Boolean = true,
    val selectedCurrency: String = "USD ($)",
    val selectedLanguage: String = "English",
    val showEditProfileModal: Boolean = false,
    val showAboutModal: Boolean = false,
    val showPrivacyModal: Boolean = false,
    val showTermsModal: Boolean = false,
    val showSupportModal: Boolean = false,
    val isLoading: Boolean = false
)

class ProfileViewModel(
    private val profileRepo: ProfileRepository = RemoteProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun refreshProfile() {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            profileRepo.getStudentProfile().collect { profile ->
                _uiState.value = _uiState.value.copy(studentProfile = profile)
            }
        }
    }

    fun updateProfile(updated: StudentProfile) {
        profileRepo.updateStudentProfile(updated)
        toggleEditProfileModal(false)
    }

    fun updateStudyPreferences(studyLevel: String, subjects: List<String>) {
        val current = _uiState.value.studentProfile ?: StudentProfile()
        val updated = current.copy(
            preferredDegree = studyLevel,
            selectedStudyLevel = studyLevel,
            selectedSubjects = subjects,
            preferredField = subjects.firstOrNull() ?: current.preferredField,
            hasCompletedOnboarding = true
        )
        profileRepo.updateStudentProfile(updated)
        _uiState.value = _uiState.value.copy(studentProfile = updated)
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun setCurrency(currency: String) {
        _uiState.value = _uiState.value.copy(selectedCurrency = currency)
    }

    fun toggleEditProfileModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showEditProfileModal = show)
    }

    fun toggleAboutModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAboutModal = show)
    }

    fun togglePrivacyModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPrivacyModal = show)
    }

    fun toggleTermsModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTermsModal = show)
    }

    fun toggleSupportModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSupportModal = show)
    }
}
