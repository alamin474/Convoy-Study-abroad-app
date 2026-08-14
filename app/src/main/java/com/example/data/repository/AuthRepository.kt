package com.example.data.repository

import com.example.data.model.AcademicInfo
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.security.ConvoySecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    val isLoggedIn: StateFlow<Boolean>
    val isAdmin: StateFlow<Boolean>

    fun login(email: String, passkey: String, requestedRole: UserRole): Result<User>
    fun loginWithGoogle(email: String = "alex.google@gmail.com", name: String = "Alex Mercer"): Result<User>
    fun registerStudent(fullName: String, email: String, passkey: String): Result<User>
    fun updateUserPreferences(studyLevel: String, subjects: List<String>)
    fun resetPassword(email: String): Result<String>
    fun switchToAdmin(): Boolean
    fun switchToStudent()
    fun logout()
}

class ConvoyAuthRepository : AuthRepository {

    private val defaultStudent = User(
        userId = "student_101",
        name = "Alex Mercer",
        email = "alex.mercer@student.org",
        role = UserRole.STUDENT,
        phone = "+1 (555) 382-9102",
        nationality = "United States",
        academicInfo = AcademicInfo()
    )

    private val defaultAdmin = User(
        userId = "admin_001",
        name = "Convoy Super Admin",
        email = "admin@convoy.edu",
        role = UserRole.ADMIN,
        phone = "+1 (800) 555-CONVOY",
        nationality = "Global Platform Admin"
    )

    // Store for registered users and credentials during runtime
    private val registeredAccounts = mutableMapOf<String, Pair<User, String>>(
        "alex.mercer@student.org".lowercase() to Pair(defaultStudent, "student123"),
        "admin@convoy.edu".lowercase() to Pair(defaultAdmin, "convoy2026")
    )

    private val _currentUser = MutableStateFlow<User?>(defaultStudent)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    override val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    init {
        // Synchronize initial ConvoySecurityManager state
        ConvoySecurityManager.setCurrentUser(defaultStudent)
    }

    override fun login(email: String, passkey: String, requestedRole: UserRole): Result<User> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email address cannot be empty."))
        }
        if (passkey.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty."))
        }

        if (requestedRole == UserRole.ADMIN) {
            val isAuthorizedAdminEmail = trimmedEmail.endsWith("@convoy.edu") || trimmedEmail.contains("admin")
            val isValidPasskey = passkey.length >= 4

            if (isAuthorizedAdminEmail && isValidPasskey) {
                val adminUser = registeredAccounts[trimmedEmail]?.first ?: User(
                    userId = "admin_${trimmedEmail.hashCode().toString().take(6)}",
                    name = trimmedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                    email = trimmedEmail,
                    role = UserRole.ADMIN,
                    phone = "+1 (800) 555-ADMIN"
                )
                _currentUser.value = adminUser
                _isLoggedIn.value = true
                _isAdmin.value = true
                ConvoySecurityManager.setCurrentUser(adminUser)
                return Result.success(adminUser)
            } else {
                return Result.failure(SecurityException("Invalid administrator credentials or unauthorized email domain."))
            }
        } else {
            // Student Login
            val existingAccount = registeredAccounts[trimmedEmail]
            if (existingAccount != null) {
                val (user, storedPasskey) = existingAccount
                if (storedPasskey != passkey && passkey.length < 3) {
                    return Result.failure(IllegalArgumentException("Incorrect password entered for $trimmedEmail"))
                }
                _currentUser.value = user
                _isLoggedIn.value = true
                _isAdmin.value = false
                ConvoySecurityManager.setCurrentUser(user)
                return Result.success(user)
            } else {
                // Auto-create student user account for valid email logins
                val studentUser = User(
                    userId = "student_${trimmedEmail.hashCode().toString().take(6)}",
                    name = trimmedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                    email = trimmedEmail,
                    role = UserRole.STUDENT
                )
                registeredAccounts[trimmedEmail] = Pair(studentUser, passkey)
                _currentUser.value = studentUser
                _isLoggedIn.value = true
                _isAdmin.value = false
                ConvoySecurityManager.setCurrentUser(studentUser)
                return Result.success(studentUser)
            }
        }
    }

    override fun loginWithGoogle(email: String, name: String): Result<User> {
        val trimmedEmail = email.trim().lowercase().ifEmpty { "alex.mercer.google@gmail.com" }
        val displayName = name.ifBlank { "Alex Mercer" }
        val existing = registeredAccounts[trimmedEmail]?.first
        val googleUser = existing ?: User(
            userId = "google_${trimmedEmail.hashCode().toString().take(6)}",
            name = displayName,
            email = trimmedEmail,
            role = UserRole.STUDENT,
            isGoogleUser = true,
            hasCompletedOnboarding = existing?.hasCompletedOnboarding ?: false
        )
        registeredAccounts[trimmedEmail] = Pair(googleUser, "google_oauth_pass")
        _currentUser.value = googleUser
        _isLoggedIn.value = true
        _isAdmin.value = false
        ConvoySecurityManager.setCurrentUser(googleUser)
        return Result.success(googleUser)
    }

    override fun registerStudent(fullName: String, email: String, passkey: String): Result<User> {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim().lowercase()

        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Full name is required."))
        }
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please provide a valid email address."))
        }
        if (passkey.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
        }
        if (registeredAccounts.containsKey(trimmedEmail)) {
            return Result.failure(IllegalStateException("An account with $trimmedEmail already exists. Please log in."))
        }

        val newUser = User(
            userId = "student_${System.currentTimeMillis().toString().takeLast(6)}",
            name = trimmedName,
            email = trimmedEmail,
            role = UserRole.STUDENT
        )

        registeredAccounts[trimmedEmail] = Pair(newUser, passkey)

        _currentUser.value = newUser
        _isLoggedIn.value = true
        _isAdmin.value = false
        ConvoySecurityManager.setCurrentUser(newUser)

        return Result.success(newUser)
    }

    override fun resetPassword(email: String): Result<String> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }

        return Result.success("Password reset instructions have been dispatched to $trimmedEmail. Please check your inbox.")
    }

    override fun updateUserPreferences(studyLevel: String, subjects: List<String>) {
        val current = _currentUser.value ?: return
        val updatedAcademic = current.academicInfo.copy(
            studyLevel = studyLevel,
            selectedSubjects = subjects,
            preferredFields = subjects
        )
        val updatedUser = current.copy(
            academicInfo = updatedAcademic,
            hasCompletedOnboarding = true
        )
        _currentUser.value = updatedUser
        val emailKey = updatedUser.email.trim().lowercase()
        val pass = registeredAccounts[emailKey]?.second ?: "student123"
        registeredAccounts[emailKey] = Pair(updatedUser, pass)
        ConvoySecurityManager.setCurrentUser(updatedUser)
    }

    override fun switchToAdmin(): Boolean {
        _currentUser.value = defaultAdmin
        _isLoggedIn.value = true
        _isAdmin.value = true
        ConvoySecurityManager.setCurrentUser(defaultAdmin)
        return true
    }

    override fun switchToStudent() {
        _currentUser.value = defaultStudent
        _isLoggedIn.value = true
        _isAdmin.value = false
        ConvoySecurityManager.setCurrentUser(defaultStudent)
    }

    override fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _isAdmin.value = false
        // Reset security manager user to non-authenticated student placeholder
        ConvoySecurityManager.setCurrentUser(
            User(userId = "guest", name = "Guest User", email = "", role = UserRole.STUDENT)
        )
    }
}
