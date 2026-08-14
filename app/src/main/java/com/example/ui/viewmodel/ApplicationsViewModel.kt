package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Application
import com.example.data.model.ApplicationStatus
import com.example.data.model.AssistanceRequest
import com.example.data.model.AssistanceType
import com.example.data.model.DocumentCategory
import com.example.data.model.StudentDocument
import com.example.data.repository.ApplicationRepository
import com.example.data.repository.RemoteApplicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ApplicationsUiState(
    val selectedStatusFilter: ApplicationStatus? = null,
    val applications: List<Application> = emptyList(),
    val filteredApplications: List<Application> = emptyList(),
    val selectedApplication: Application? = null,
    val documents: List<StudentDocument> = emptyList(),
    val assistanceRequests: List<AssistanceRequest> = emptyList(),
    val activeTab: Int = 0, // 0 = Applications, 1 = Assistance Services, 2 = Documents
    val showNewAppDialog: Boolean = false,
    val showUploadDocDialog: Boolean = false,
    val showRequestAssistanceDialog: Boolean = false,
    val isLoading: Boolean = false
)

class ApplicationsViewModel(
    private val applicationRepo: ApplicationRepository = RemoteApplicationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                applicationRepo.getApplications(),
                applicationRepo.getDocuments(),
                applicationRepo.getAssistanceRequests()
            ) { apps, docs, reqs ->
                val currentFilter = _uiState.value.selectedStatusFilter
                val filtered = if (currentFilter == null) apps else apps.filter { it.status == currentFilter }
                
                // Keep selected application up to date
                val currentSelectedId = _uiState.value.selectedApplication?.applicationId
                val updatedSelected = if (currentSelectedId != null) {
                    apps.find { it.applicationId == currentSelectedId }
                } else null

                _uiState.value.copy(
                    applications = apps,
                    filteredApplications = filtered,
                    selectedApplication = updatedSelected,
                    documents = docs,
                    assistanceRequests = reqs,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun selectApplication(application: Application?) {
        _uiState.value = _uiState.value.copy(selectedApplication = application)
    }

    fun filterByStatus(status: ApplicationStatus?) {
        val apps = _uiState.value.applications
        val filtered = if (status == null) apps else apps.filter { it.status == status }
        _uiState.value = _uiState.value.copy(
            selectedStatusFilter = status,
            filteredApplications = filtered
        )
    }

    fun toggleNewAppDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showNewAppDialog = show)
    }

    fun toggleUploadDocDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showUploadDocDialog = show)
    }

    fun toggleRequestAssistanceDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRequestAssistanceDialog = show)
    }

    fun createAssistanceRequest(
        serviceType: AssistanceType,
        targetUniversityName: String,
        targetProgramName: String,
        notes: String
    ) {
        applicationRepo.createAssistanceRequest(
            serviceType = serviceType,
            targetUniversityName = targetUniversityName,
            targetProgramName = targetProgramName,
            notes = notes
        )
        toggleRequestAssistanceDialog(false)
    }

    fun addGuidanceMessage(requestId: String, messageText: String) {
        applicationRepo.addGuidanceMessage(requestId, messageText)
    }

    fun createDraftApplication(
        universityName: String,
        programName: String,
        degreeLevel: String,
        country: String,
        intakeSeason: String
    ) {
        applicationRepo.createDraftApplication(
            universityName = universityName,
            programName = programName,
            degreeLevel = degreeLevel,
            country = country,
            intakeSeason = intakeSeason
        )
        toggleNewAppDialog(false)
    }

    fun submitApplication(applicationId: String) {
        applicationRepo.submitApplication(applicationId)
    }

    fun saveDraftNotes(applicationId: String, studentNotes: String) {
        val app = _uiState.value.applications.find { it.applicationId == applicationId }
        if (app != null) {
            applicationRepo.saveDraftApplication(app.copy(studentNotes = studentNotes))
        }
    }

    fun withdrawApplication(applicationId: String) {
        applicationRepo.withdrawApplication(applicationId)
    }

    fun uploadDocument(title: String, category: DocumentCategory) {
        applicationRepo.uploadDocumentPlaceholder(title, category)
        toggleUploadDocDialog(false)
    }

    fun deleteDocument(id: String) {
        applicationRepo.deleteDocument(id)
    }
}
