package com.example.data.repository

import com.example.data.model.FundingType
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.Scholarship
import com.example.data.model.SponsoredListing
import com.example.data.model.User
import com.example.data.remote.ConvoyRemoteDataSource
import com.example.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface ScholarshipRepository {
    fun getScholarships(): Flow<List<Scholarship>>
    fun getScholarshipById(id: String): Flow<Scholarship?>
    fun getSponsoredListings(): Flow<List<SponsoredListing>>
    fun toggleSave(id: String, currentUser: User? = null)
    fun requestInformation(
        scholarship: Scholarship,
        currentUser: User?,
        phone: String = "",
        message: String = ""
    ): Pair<Boolean, String>
    fun searchAndFilter(
        query: String = "",
        countryFilter: String = "All",
        degreeFilter: String = "All",
        fundingTypeFilter: FundingType = FundingType.ALL,
        deadlineFilter: String = "All"
    ): Flow<List<Scholarship>>
}

class RemoteScholarshipRepository(
    private val remoteDataSource: RemoteDataSource = ConvoyRemoteDataSource()
) : ScholarshipRepository {

    private val savedIdsState = MutableStateFlow<Set<String>>(setOf("sch_1", "sch_3"))

    override fun getScholarships(): Flow<List<Scholarship>> {
        return remoteDataSource.fetchScholarships().map { list ->
            val saved = savedIdsState.value
            list.map { s -> s.copy(isSaved = saved.contains(s.scholarshipId)) }
        }
    }

    override fun getScholarshipById(id: String): Flow<Scholarship?> {
        return remoteDataSource.fetchScholarshipById(id).map { sch ->
            sch?.copy(isSaved = savedIdsState.value.contains(sch.scholarshipId))
        }
    }

    override fun getSponsoredListings(): Flow<List<SponsoredListing>> = remoteDataSource.fetchSponsoredListings()

    override fun toggleSave(id: String, currentUser: User?) {
        savedIdsState.update { set ->
            val isNowSaved = !set.contains(id)
            if (isNowSaved && currentUser != null) {
                remoteDataSource.createLead(
                    Lead(
                        leadId = "",
                        studentUserId = currentUser.userId,
                        studentName = currentUser.name,
                        studentEmail = currentUser.email,
                        studentPhone = currentUser.phone,
                        country = currentUser.nationality,
                        scholarshipId = id,
                        scholarshipName = "Scholarship ID: $id",
                        source = "Save Scholarship",
                        date = "2026-02-09",
                        status = LeadStatus.NEW
                    )
                )
            }
            if (set.contains(id)) set - id else set + id
        }
    }

    override fun requestInformation(
        scholarship: Scholarship,
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
            country = scholarship.country,
            scholarshipId = scholarship.scholarshipId,
            scholarshipName = scholarship.name,
            source = "Request Information",
            date = "2026-02-09",
            status = LeadStatus.NEW,
            notes = message
        )
        return remoteDataSource.createLead(lead)
    }

    override fun searchAndFilter(
        query: String,
        countryFilter: String,
        degreeFilter: String,
        fundingTypeFilter: FundingType,
        deadlineFilter: String
    ): Flow<List<Scholarship>> {
        return getScholarships().map { list ->
            list.filter { s ->
                val matchesQuery = query.isBlank() ||
                        s.name.contains(query, ignoreCase = true) ||
                        s.provider.contains(query, ignoreCase = true) ||
                        s.university.contains(query, ignoreCase = true) ||
                        s.country.contains(query, ignoreCase = true) ||
                        s.fieldOfStudy.contains(query, ignoreCase = true)

                val matchesCountry = countryFilter == "All" || s.country.equals(countryFilter, ignoreCase = true)
                val matchesDegree = degreeFilter == "All" || s.degreeLevel.contains(degreeFilter, ignoreCase = true)
                val matchesType = fundingTypeFilter == FundingType.ALL || s.fundingType == fundingTypeFilter
                val matchesDeadline = when (deadlineFilter) {
                    "2026 Deadlines" -> s.deadline.contains("2026")
                    "2027 Deadlines" -> s.deadline.contains("2027")
                    else -> true
                }

                matchesQuery && matchesCountry && matchesDegree && matchesType && matchesDeadline
            }
        }
    }
}

class MockScholarshipRepository : ScholarshipRepository by RemoteScholarshipRepository()
