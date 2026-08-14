package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.RemoteSupportRepository
import com.example.data.repository.SupportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupportUiState(
    val categoryFilter: SupportCategory? = null,
    val statusFilter: SupportStatus? = null,
    val userRequests: List<SupportRequest> = emptyList(),
    val filteredRequests: List<SupportRequest> = emptyList(),
    val selectedRequest: SupportRequest? = null,
    val supportConfig: SupportConfig = SupportConfig(),
    val showNewRequestDialog: Boolean = false,
    val prefilledCategory: SupportCategory = SupportCategory.GENERAL,
    val prefilledApplicationId: String? = null,
    val prefilledUniversity: String? = null,
    val prefilledDocumentContext: String? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val actionFeedback: String? = null
)

class SupportViewModel(
    private val supportRepo: SupportRepository = RemoteSupportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                supportRepo.getSupportRequestsForUser(),
                supportRepo.getSupportConfig()
            ) { reqs, config ->
                val selectedId = _uiState.value.selectedRequest?.requestId
                val updatedSelected = if (selectedId != null) {
                    reqs.find { it.requestId == selectedId }
                } else null

                val currentCat = _uiState.value.categoryFilter
                val currentStat = _uiState.value.statusFilter

                val filtered = reqs.filter { req ->
                    (currentCat == null || req.category == currentCat) &&
                    (currentStat == null || req.status == currentStat)
                }

                _uiState.value.copy(
                    userRequests = reqs,
                    filteredRequests = filtered,
                    selectedRequest = updatedSelected,
                    supportConfig = config,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setCategoryFilter(category: SupportCategory?) {
        _uiState.update { state ->
            val newCat = if (state.categoryFilter == category) null else category
            val filtered = state.userRequests.filter { req ->
                (newCat == null || req.category == newCat) &&
                (state.statusFilter == null || req.status == state.statusFilter)
            }
            state.copy(categoryFilter = newCat, filteredRequests = filtered)
        }
    }

    fun setStatusFilter(status: SupportStatus?) {
        _uiState.update { state ->
            val newStat = if (state.statusFilter == status) null else status
            val filtered = state.userRequests.filter { req ->
                (state.categoryFilter == null || req.category == state.categoryFilter) &&
                (newStat == null || req.status == newStat)
            }
            state.copy(statusFilter = newStat, filteredRequests = filtered)
        }
    }

    fun openNewRequestDialog(
        category: SupportCategory = SupportCategory.GENERAL,
        applicationId: String? = null,
        university: String? = null,
        documentContext: String? = null
    ) {
        _uiState.update {
            it.copy(
                showNewRequestDialog = true,
                prefilledCategory = category,
                prefilledApplicationId = applicationId,
                prefilledUniversity = university,
                prefilledDocumentContext = documentContext,
                actionFeedback = null
            )
        }
    }

    fun dismissNewRequestDialog() {
        _uiState.update { it.copy(showNewRequestDialog = false, actionFeedback = null) }
    }

    fun createSupportRequest(
        category: SupportCategory,
        subject: String,
        message: String,
        applicationId: String? = null,
        university: String? = null,
        documentContext: String? = null,
        attachmentName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val (success, msg) = supportRepo.createSupportRequest(
                category = category,
                subject = subject,
                message = message,
                relatedApplicationId = applicationId,
                relatedUniversity = university,
                relatedDocumentContext = documentContext,
                attachmentName = attachmentName
            )
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    showNewRequestDialog = !success,
                    actionFeedback = msg
                )
            }
        }
    }

    fun selectRequest(request: SupportRequest?) {
        _uiState.update { it.copy(selectedRequest = request) }
    }

    fun addStudentReply(requestId: String, message: String) {
        viewModelScope.launch {
            val (success, msg) = supportRepo.addReply(requestId, message, isAdmin = false)
            _uiState.update { it.copy(actionFeedback = msg) }
        }
    }

    fun closeStudentRequest(requestId: String) {
        viewModelScope.launch {
            val (success, msg) = supportRepo.closeRequest(requestId)
            _uiState.update { it.copy(actionFeedback = msg) }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(actionFeedback = null) }
    }
}
