package com.example.data.repository

import com.example.data.model.Country
import com.example.data.model.StudentProfile
import com.example.data.model.StudyDestination
import com.example.data.remote.ConvoyRemoteDataSource
import com.example.data.remote.RemoteDataSource
import com.example.data.security.ConvoySecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface ProfileRepository {
    fun getStudentProfile(): Flow<StudentProfile>
    fun updateStudentProfile(updated: StudentProfile)
    fun getStudyDestinations(): Flow<List<StudyDestination>>
    fun getCountries(): Flow<List<Country>>
    fun getCountryById(id: String): Flow<Country?>
}

class RemoteProfileRepository(
    private val remoteDataSource: RemoteDataSource = ConvoyRemoteDataSource()
) : ProfileRepository {

    private val profilesMap = mutableMapOf<String, StudentProfile>()

    private fun getOrCreateProfileForCurrentUser(): StudentProfile {
        val user = ConvoySecurityManager.currentUser
        return profilesMap.getOrPut(user.userId) {
            StudentProfile(
                id = user.userId,
                fullName = user.name,
                dateOfBirth = "2002-05-14",
                nationality = user.nationality.ifBlank { "United States" },
                countryOfResidence = "United States",
                email = user.email.ifBlank { "student@convoy.org" },
                phone = user.phone.ifBlank { "+1 (555) 382-9102" },

                highestQualification = user.academicInfo.currentLevel,
                institution = "University of California, Berkeley",
                gpaScore = user.academicInfo.gpaScore,
                graduationYear = "2025",
                intendedDegree = "Master's Degree",
                intendedField = user.academicInfo.majorField,

                ieltsScore = "8.0 Overall",
                toeflScore = "105 iBT",
                pteScore = "76 Academic",
                otherEnglishQualification = "MOI Letter",

                budgetRangePerYear = "$15,000 - $30,000 / year",
                preferredCountries = user.academicInfo.preferredCountries,
                preferredDegree = "Master's Degree",
                preferredField = user.academicInfo.preferredFields.firstOrNull() ?: "Computer Science",

                targetIntake = user.academicInfo.targetIntake,
                isVerified = true
            )
        }
    }

    private val profileState = MutableStateFlow(getOrCreateProfileForCurrentUser())

    override fun getStudentProfile(): Flow<StudentProfile> {
        val current = getOrCreateProfileForCurrentUser()
        if (!ConvoySecurityManager.canAccessProfile(current.id)) {
            // Security fallback if somehow unpermitted
            profileState.value = StudentProfile(id = "unauthorized", fullName = "Private Student Profile")
        } else {
            profileState.value = current
        }
        return profileState
    }

    override fun updateStudentProfile(updated: StudentProfile) {
        if (!ConvoySecurityManager.canAccessProfile(updated.id)) {
            throw SecurityException("Access Denied: You are not authorized to modify another student's profile.")
        }
        profilesMap[updated.id] = updated
        profileState.update { updated }
    }

    override fun getStudyDestinations(): Flow<List<StudyDestination>> {
        return remoteDataSource.fetchCountries().map { countries ->
            countries.map { it.toStudyDestination() }
        }
    }

    override fun getCountries(): Flow<List<Country>> {
        return remoteDataSource.fetchCountries()
    }

    override fun getCountryById(id: String): Flow<Country?> {
        return remoteDataSource.fetchCountryById(id)
    }
}

class MockProfileRepository : ProfileRepository by RemoteProfileRepository()
