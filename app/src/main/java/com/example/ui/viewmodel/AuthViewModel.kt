package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.AuthRepository
import com.example.data.repository.ConvoyAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER,
    RESET_PASSWORD
}

data class AuthUiState(
    val emailInput: String = "alex.mercer@student.org",
    val passkeyInput: String = "student123",
    val registerFullName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerReferralCode: String = "",
    val resetEmailInput: String = "",
    val selectedRole: UserRole = UserRole.STUDENT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = true,
    val isAdmin: Boolean = false,
    val showAuthDialog: Boolean = false,
    val authMode: AuthMode = AuthMode.LOGIN
)

class AuthViewModel(
    private val authRepo: AuthRepository = ConvoyAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        viewModelScope.launch {
            authRepo.isLoggedIn.collect { loggedIn ->
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
        viewModelScope.launch {
            authRepo.isAdmin.collect { admin ->
                _uiState.update { it.copy(isAdmin = admin) }
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail, errorMessage = null, infoMessage = null) }
    }

    fun onPasskeyChange(newPasskey: String) {
        _uiState.update { it.copy(passkeyInput = newPasskey, errorMessage = null, infoMessage = null) }
    }

    fun onRegisterFullNameChange(name: String) {
        _uiState.update { it.copy(registerFullName = name, errorMessage = null) }
    }

    fun onRegisterEmailChange(email: String) {
        _uiState.update { it.copy(registerEmail = email, errorMessage = null) }
    }

    fun onRegisterPasswordChange(password: String) {
        _uiState.update { it.copy(registerPassword = password, errorMessage = null) }
    }

    fun onRegisterReferralCodeChange(code: String) {
        _uiState.update { it.copy(registerReferralCode = code, errorMessage = null) }
    }

    fun onResetEmailChange(email: String) {
        _uiState.update { it.copy(resetEmailInput = email, errorMessage = null, infoMessage = null) }
    }

    fun setAuthMode(mode: AuthMode) {
        _uiState.update { it.copy(authMode = mode, errorMessage = null, infoMessage = null) }
    }

    fun toggleAuthDialog(show: Boolean, mode: AuthMode = AuthMode.LOGIN) {
        _uiState.update {
            it.copy(
                showAuthDialog = show,
                authMode = mode,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun onRoleChange(role: UserRole) {
        _uiState.update {
            it.copy(
                selectedRole = role,
                emailInput = if (role == UserRole.ADMIN) "admin@convoy.edu" else "alex.mercer@student.org",
                passkeyInput = if (role == UserRole.ADMIN) "convoy2026" else "student123",
                errorMessage = null
            )
        }
    }

    fun login(onSuccess: () -> Unit = {}) {
        val currentState = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val result = authRepo.login(
            email = currentState.emailInput,
            passkey = currentState.passkeyInput,
            requestedRole = currentState.selectedRole
        )

        result.onSuccess {
            _uiState.update { it.copy(isLoading = false, errorMessage = null, showAuthDialog = false) }
            onSuccess()
        }.onFailure { err ->
            _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Authentication failed") }
        }
    }

    fun loginWithGoogle(onResult: (needsOnboarding: Boolean) -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = authRepo.loginWithGoogle()
        result.onSuccess { user ->
            _uiState.update { it.copy(isLoading = false, errorMessage = null, showAuthDialog = false) }
            onResult(!user.hasCompletedOnboarding)
        }.onFailure { err ->
            _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Google Sign In failed") }
        }
    }

    fun saveUserPreferences(studyLevel: String, subjects: List<String>) {
        authRepo.updateUserPreferences(studyLevel, subjects)
    }

    fun registerStudent(onSuccess: () -> Unit = {}) {
        val currentState = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val result = authRepo.registerStudent(
            fullName = currentState.registerFullName,
            email = currentState.registerEmail,
            passkey = currentState.registerPassword
        )

        result.onSuccess { newUser ->
            if (currentState.registerReferralCode.isNotBlank()) {
                val remoteDataSource = com.example.data.remote.ConvoyRemoteDataSource()
                remoteDataSource.applyReferralCode(
                    code = currentState.registerReferralCode,
                    referredUserId = newUser.userId,
                    referredName = newUser.name,
                    referredEmail = newUser.email
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    showAuthDialog = false,
                    registerFullName = "",
                    registerEmail = "",
                    registerPassword = "",
                    registerReferralCode = ""
                )
            }
            onSuccess()
        }.onFailure { err ->
            _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Registration failed") }
        }
    }

    fun resetPassword() {
        val currentState = _uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

        val result = authRepo.resetPassword(currentState.resetEmailInput)

        result.onSuccess { msg ->
            _uiState.update { it.copy(isLoading = false, infoMessage = msg, errorMessage = null) }
        }.onFailure { err ->
            _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Password reset failed") }
        }
    }

    fun loginAsAdminDemo() {
        authRepo.switchToAdmin()
        _uiState.update {
            it.copy(
                emailInput = "admin@convoy.edu",
                passkeyInput = "convoy2026",
                selectedRole = UserRole.ADMIN,
                errorMessage = null,
                isLoading = false,
                showAuthDialog = false
            )
        }
    }

    fun loginAsStudentDemo() {
        authRepo.switchToStudent()
        _uiState.update {
            it.copy(
                emailInput = "alex.mercer@student.org",
                passkeyInput = "student123",
                selectedRole = UserRole.STUDENT,
                errorMessage = null,
                isLoading = false,
                showAuthDialog = false
            )
        }
    }

    fun logout() {
        authRepo.logout()
        _uiState.update {
            it.copy(
                emailInput = "",
                passkeyInput = "",
                errorMessage = null,
                infoMessage = null,
                isLoading = false
            )
        }
    }
}
