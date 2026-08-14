package com.example.data.repository

import com.example.data.model.*
import com.example.data.remote.ConvoyRemoteDataSource
import com.example.data.remote.RemoteDataSource
import com.example.data.security.ConvoySecurityManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface SupportRepository {
    fun getSupportRequestsForUser(): Flow<List<SupportRequest>>
    fun getSupportRequestsForAdmin(): Flow<List<SupportRequest>>
    fun getSupportConfig(): Flow<SupportConfig>
    fun createSupportRequest(
        category: SupportCategory,
        subject: String,
        message: String,
        relatedApplicationId: String? = null,
        relatedUniversity: String? = null,
        relatedDocumentContext: String? = null,
        attachmentName: String? = null,
        attachmentUrl: String? = null
    ): Pair<Boolean, String>

    fun addReply(requestId: String, message: String, isAdmin: Boolean): Pair<Boolean, String>
    fun closeRequest(requestId: String): Pair<Boolean, String>
    fun updateSupportStatusByAdmin(
        requestId: String,
        status: SupportStatus,
        internalNotes: String = "",
        assignedStaff: String = ""
    )
    fun updateSupportConfigByAdmin(config: SupportConfig)
}

class RemoteSupportRepository(
    private val remoteDataSource: RemoteDataSource = ConvoyRemoteDataSource()
) : SupportRepository {

    private val currentUser: User
        get() = ConvoySecurityManager.currentUser

    override fun getSupportRequestsForUser(): Flow<List<SupportRequest>> {
        return remoteDataSource.fetchSupportRequests(currentUser.userId)
    }

    override fun getSupportRequestsForAdmin(): Flow<List<SupportRequest>> {
        return remoteDataSource.fetchAllSupportRequestsForAdmin()
    }

    override fun getSupportConfig(): Flow<SupportConfig> {
        return remoteDataSource.fetchSupportConfig()
    }

    override fun createSupportRequest(
        category: SupportCategory,
        subject: String,
        message: String,
        relatedApplicationId: String?,
        relatedUniversity: String?,
        relatedDocumentContext: String?,
        attachmentName: String?,
        attachmentUrl: String?
    ): Pair<Boolean, String> {
        if (subject.isBlank()) return Pair(false, "Subject cannot be empty")
        if (message.isBlank()) return Pair(false, "Message cannot be empty")

        val newRequest = SupportRequest(
            requestId = "req_sup_${UUID.randomUUID().toString().take(6)}",
            userId = currentUser.userId,
            studentName = currentUser.name.ifBlank { "Authenticated Student" },
            studentEmail = currentUser.email.ifBlank { "student@example.com" },
            category = category,
            subject = subject,
            message = message,
            relatedApplicationId = relatedApplicationId,
            relatedUniversity = relatedUniversity,
            relatedDocumentContext = relatedDocumentContext,
            attachmentName = attachmentName,
            attachmentUrl = attachmentUrl,
            status = SupportStatus.NEW,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return remoteDataSource.createSupportRequest(newRequest)
    }

    override fun addReply(requestId: String, message: String, isAdmin: Boolean): Pair<Boolean, String> {
        if (message.isBlank()) return Pair(false, "Reply message cannot be empty")

        val reply = SupportReply(
            replyId = "rep_${UUID.randomUUID().toString().take(6)}",
            senderId = if (isAdmin) "admin_staff" else currentUser.userId,
            senderName = if (isAdmin) "Convoy Support Specialist" else currentUser.name.ifBlank { "Student" },
            isAdmin = isAdmin,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        val newStatus = if (isAdmin) SupportStatus.WAITING_FOR_STUDENT else SupportStatus.OPEN
        remoteDataSource.addSupportReply(requestId, reply, newStatus)
        return Pair(true, "Reply sent successfully")
    }

    override fun closeRequest(requestId: String): Pair<Boolean, String> {
        remoteDataSource.updateSupportStatus(
            requestId = requestId,
            status = SupportStatus.CLOSED,
            internalNotes = "Closed by student"
        )
        return Pair(true, "Support request closed")
    }

    override fun updateSupportStatusByAdmin(
        requestId: String,
        status: SupportStatus,
        internalNotes: String,
        assignedStaff: String
    ) {
        remoteDataSource.updateSupportStatus(requestId, status, internalNotes, assignedStaff)
    }

    override fun updateSupportConfigByAdmin(config: SupportConfig) {
        remoteDataSource.updateSupportConfig(config)
    }
}
