package com.example.data.repository

import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.TuitionCategory
import com.example.data.model.University
import com.example.data.model.UniversityRequirement
import com.example.data.model.SponsoredListing
import com.example.data.model.User
import com.example.data.remote.ConvoyRemoteDataSource
import com.example.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface UniversityRepository {
    fun getUniversities(): Flow<List<University>>
    fun getUniversityById(id: String): Flow<University?>
    fun getSponsoredListings(): Flow<List<SponsoredListing>>
    fun toggleBookmark(id: String, currentUser: User? = null)
    fun requestInformation(
        university: University,
        currentUser: User?,
        phone: String = "",
        message: String = ""
    ): Pair<Boolean, String>
    fun createStartApplicationLead(
        university: University,
        currentUser: User?
    ): Pair<Boolean, String>
    fun getUniversityRequirements(
        universityId: String,
        programName: String? = null,
        intakeSeason: String? = null
    ): Flow<List<UniversityRequirement>>
    fun searchAndFilter(
        query: String = "",
        countryFilter: String = "All",
        tuitionFilter: TuitionCategory? = null,
        maxRankingFilter: Int? = null,
        scholarshipOnly: Boolean = false
    ): Flow<List<University>>
    fun getLowTuitionUniversities(): Flow<List<University>>
    fun getUniversitiesByCountry(countryName: String): Flow<List<University>>
}

class RemoteUniversityRepository(
    private val remoteDataSource: RemoteDataSource = ConvoyRemoteDataSource()
) : UniversityRepository {

    private val bookmarkedIdsState = MutableStateFlow<Set<String>>(setOf("uni_1", "uni_3"))

    override fun getUniversities(): Flow<List<University>> {
        return remoteDataSource.fetchUniversities().map { list ->
            val bookmarks = bookmarkedIdsState.value
            list.map { u -> u.copy(isBookmarked = bookmarks.contains(u.universityId)) }
        }
    }

    override fun getUniversityById(id: String): Flow<University?> {
        return remoteDataSource.fetchUniversityById(id).map { uni ->
            uni?.copy(isBookmarked = bookmarkedIdsState.value.contains(uni.universityId))
        }
    }

    override fun getSponsoredListings(): Flow<List<SponsoredListing>> = remoteDataSource.fetchSponsoredListings()

    override fun toggleBookmark(id: String, currentUser: User?) {
        bookmarkedIdsState.update { set ->
            val isNowBookmarked = !set.contains(id)
            if (isNowBookmarked && currentUser != null) {
                remoteDataSource.createLead(
                    Lead(
                        leadId = "",
                        studentUserId = currentUser.userId,
                        studentName = currentUser.name,
                        studentEmail = currentUser.email,
                        studentPhone = currentUser.phone,
                        country = currentUser.nationality,
                        universityId = id,
                        universityName = "University ID: $id",
                        source = "Save University",
                        date = "2026-02-09",
                        status = LeadStatus.NEW
                    )
                )
            }
            if (set.contains(id)) set - id else set + id
        }
    }

    override fun requestInformation(
        university: University,
        currentUser: User?,
        phone: String,
        message: String
    ): Pair<Boolean, String> {
        val studentName = currentUser?.name ?: "Guest Student"
        val studentEmail = currentUser?.email ?: "guest@student.org"
        val studentUserId = currentUser?.userId ?: "usr_guest"

        val lead = Lead(
            leadId = "",
            studentUserId = studentUserId,
            studentName = studentName,
            studentEmail = studentEmail,
            studentPhone = phone.ifBlank { currentUser?.phone ?: "" },
            country = university.country,
            universityId = university.universityId,
            universityName = university.name,
            source = "Request Information",
            date = "2026-02-09",
            status = LeadStatus.NEW,
            notes = message
        )
        return remoteDataSource.createLead(lead)
    }

    override fun createStartApplicationLead(
        university: University,
        currentUser: User?
    ): Pair<Boolean, String> {
        val studentName = currentUser?.name ?: "Guest Student"
        val studentEmail = currentUser?.email ?: "guest@student.org"
        val studentUserId = currentUser?.userId ?: "usr_guest"

        val lead = Lead(
            leadId = "",
            studentUserId = studentUserId,
            studentName = studentName,
            studentEmail = studentEmail,
            studentPhone = currentUser?.phone ?: "",
            country = university.country,
            universityId = university.universityId,
            universityName = university.name,
            source = "Start Application",
            date = "2026-02-09",
            status = LeadStatus.NEW
        )
        return remoteDataSource.createLead(lead)
    }

    override fun getUniversityRequirements(
        universityId: String,
        programName: String?,
        intakeSeason: String?
    ): Flow<List<UniversityRequirement>> {
        return remoteDataSource.fetchRequirements(universityId, programName, intakeSeason)
    }

    override fun searchAndFilter(
        query: String,
        countryFilter: String,
        tuitionFilter: TuitionCategory?,
        maxRankingFilter: Int?,
        scholarshipOnly: Boolean
    ): Flow<List<University>> {
        return getUniversities().map { list ->
            list.filter { u ->
                val matchesQuery = query.isEmpty() ||
                        u.name.contains(query, ignoreCase = true) ||
                        u.city.contains(query, ignoreCase = true) ||
                        u.country.contains(query, ignoreCase = true) ||
                        u.programs.any { it.contains(query, ignoreCase = true) }

                val matchesCountry = countryFilter == "All" || u.country.equals(countryFilter, ignoreCase = true)
                val matchesTuition = tuitionFilter == null || u.tuitionCategory == tuitionFilter
                val matchesRanking = maxRankingFilter == null || u.ranking <= maxRankingFilter
                val matchesScholarship = !scholarshipOnly || u.hasScholarships

                matchesQuery && matchesCountry && matchesTuition && matchesRanking && matchesScholarship
            }
        }
    }

    override fun getLowTuitionUniversities(): Flow<List<University>> {
        return getUniversities().map { list ->
            list.filter { it.isLowTuition || it.tuitionCategory == TuitionCategory.UNDER_10K }
        }
    }

    override fun getUniversitiesByCountry(countryName: String): Flow<List<University>> {
        return getUniversities().map { list ->
            list.filter { it.country.equals(countryName, ignoreCase = true) }
        }
    }
}

// Keep Mock for test compatibility if referenced anywhere
class MockUniversityRepository : UniversityRepository by RemoteUniversityRepository()
