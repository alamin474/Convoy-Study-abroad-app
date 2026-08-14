package com.example.data.repository

import com.example.data.model.Application
import com.example.data.model.ApplicationStatus
import com.example.data.model.AssistanceRequest
import com.example.data.model.AssistanceStatus
import com.example.data.model.AssistanceType
import com.example.data.model.DocumentCategory
import com.example.data.model.GuidanceMessage
import com.example.data.model.StudentDocument
import com.example.data.remote.ConvoyRemoteDataSource
import com.example.data.remote.RemoteDataSource
import com.example.data.security.ConvoySecurityManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ApplicationRepository {
    fun getApplications(): Flow<List<Application>>
    fun getDocuments(): Flow<List<StudentDocument>>
    fun createDraftApplication(
        universityName: String,
        programName: String,
        degreeLevel: String,
        country: String,
        intakeSeason: String
    )
    fun submitApplication(applicationId: String)
    fun saveDraftApplication(application: Application)
    fun withdrawApplication(applicationId: String)
    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, note: String = "")
    fun updateApplicationInternalNotes(applicationId: String, notes: String)
    fun requestMissingDocuments(applicationId: String, missingDocs: List<String>)
    fun uploadDocumentPlaceholder(title: String, category: DocumentCategory)
    fun deleteDocument(id: String)

    fun getAssistanceRequests(): Flow<List<AssistanceRequest>>
    fun createAssistanceRequest(
        serviceType: AssistanceType,
        targetUniversityName: String,
        targetProgramName: String,
        notes: String
    )
    fun addGuidanceMessage(requestId: String, messageText: String)
}

class RemoteApplicationRepository(
    private val remoteDataSource: RemoteDataSource = ConvoyRemoteDataSource()
) : ApplicationRepository {

    private val currentUserId: String
        get() = ConvoySecurityManager.currentUser.userId

    override fun getApplications(): Flow<List<Application>> {
        return remoteDataSource.fetchApplications(currentUserId)
    }

    override fun getDocuments(): Flow<List<StudentDocument>> {
        return remoteDataSource.fetchDocuments(currentUserId)
    }

    override fun createDraftApplication(
        universityName: String,
        programName: String,
        degreeLevel: String,
        country: String,
        intakeSeason: String
    ) {
        val newApp = Application(
            applicationId = "app_${UUID.randomUUID().toString().take(6)}",
            userId = currentUserId,
            universityName = universityName,
            programName = programName,
            degreeLevel = degreeLevel,
            intakeSeason = intakeSeason,
            country = country,
            status = ApplicationStatus.DRAFT,
            submittedDate = null,
            nextMilestone = "Complete Statement of Purpose & Review Document Requirements",
            completionPercentage = 0.20f
        )
        remoteDataSource.createApplication(newApp)
    }

    override fun submitApplication(applicationId: String) {
        remoteDataSource.submitApplication(applicationId)
    }

    override fun saveDraftApplication(application: Application) {
        remoteDataSource.saveDraftApplication(application)
    }

    override fun withdrawApplication(applicationId: String) {
        remoteDataSource.withdrawApplication(applicationId)
    }

    override fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, note: String) {
        remoteDataSource.updateApplicationStatus(applicationId, status, note)
    }

    override fun updateApplicationInternalNotes(applicationId: String, notes: String) {
        remoteDataSource.updateApplicationInternalNotes(applicationId, notes)
    }

    override fun requestMissingDocuments(applicationId: String, missingDocs: List<String>) {
        remoteDataSource.requestMissingDocuments(applicationId, missingDocs)
    }

    override fun uploadDocumentPlaceholder(title: String, category: DocumentCategory) {
        val newDoc = StudentDocument(
            documentId = "doc_${UUID.randomUUID().toString().take(6)}",
            userId = currentUserId,
            title = title,
            category = category,
            fileName = "${title.replace(" ", "_")}.pdf",
            fileSize = "1.8 MB",
            uploadDate = "Just now",
            fileUrl = "https://convoy.storage/docs/${UUID.randomUUID()}.pdf",
            isUploaded = true
        )
        remoteDataSource.addDocument(newDoc)
    }

    override fun deleteDocument(id: String) {
        remoteDataSource.deleteDocument(id)
    }

    override fun getAssistanceRequests(): Flow<List<AssistanceRequest>> {
        return remoteDataSource.fetchAssistanceRequests(currentUserId)
    }

    override fun createAssistanceRequest(
        serviceType: AssistanceType,
        targetUniversityName: String,
        targetProgramName: String,
        notes: String
    ) {
        val user = ConvoySecurityManager.currentUser
        val request = AssistanceRequest(
            requestId = "req_ast_${UUID.randomUUID().toString().take(6)}",
            userId = currentUserId,
            studentName = user.name,
            studentEmail = user.email,
            studentPhone = user.phone,
            serviceType = serviceType,
            targetUniversityName = targetUniversityName,
            targetProgramName = targetProgramName,
            studentNotes = notes,
            status = AssistanceStatus.REQUESTED
        )
        remoteDataSource.createAssistanceRequest(request)
    }

    override fun addGuidanceMessage(requestId: String, messageText: String) {
        val user = ConvoySecurityManager.currentUser
        val msg = GuidanceMessage(
            id = "msg_${UUID.randomUUID().toString().take(6)}",
            senderName = user.name,
            isFromAdmin = false,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )
        remoteDataSource.addGuidanceMessage(requestId, msg)
    }
}

class MockApplicationRepository : ApplicationRepository by RemoteApplicationRepository()
