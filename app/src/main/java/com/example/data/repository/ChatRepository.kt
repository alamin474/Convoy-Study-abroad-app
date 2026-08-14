package com.example.data.repository

import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.CounsellorProfile
import com.example.data.model.ConversationStatus
import com.example.data.model.InternalNote
import com.example.data.remote.ConvoyRemoteDataSource
import com.example.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val remoteDataSource: RemoteDataSource = ConvoyRemoteDataSource()
) {
    fun getConversations(userId: String): Flow<List<ChatConversation>> =
        remoteDataSource.fetchConversations(userId)

    fun getAllConversationsForAdmin(): Flow<List<ChatConversation>> =
        remoteDataSource.fetchAllConversationsForAdmin()

    fun getConversationById(conversationId: String): Flow<ChatConversation?> =
        remoteDataSource.fetchConversationById(conversationId)

    fun getMessages(conversationId: String): Flow<List<ChatMessage>> =
        remoteDataSource.fetchMessages(conversationId)

    fun getInternalNotes(conversationId: String): Flow<List<InternalNote>> =
        remoteDataSource.fetchInternalNotes(conversationId)

    fun sendMessage(message: ChatMessage) {
        remoteDataSource.sendMessage(message)
    }

    fun markAsRead(conversationId: String, userId: String) {
        remoteDataSource.markConversationAsRead(conversationId, userId)
    }

    fun createCounsellorChat(userId: String, userName: String, userEmail: String): String {
        return remoteDataSource.createOrGetCounsellorConversation(userId, userName, userEmail)
    }

    fun createSupportChat(userId: String, userName: String, userEmail: String, topic: String = "General Support"): String {
        return remoteDataSource.createOrGetSupportConversation(userId, userName, userEmail, topic)
    }

    fun createApplicationChat(
        userId: String,
        userName: String,
        userEmail: String,
        applicationId: String,
        universityName: String,
        programName: String
    ): String {
        return remoteDataSource.createOrGetApplicationConversation(
            userId, userName, userEmail, applicationId, universityName, programName
        )
    }

    fun addInternalNote(note: InternalNote) {
        remoteDataSource.addInternalNote(note)
    }

    fun updateStatus(conversationId: String, status: ConversationStatus) {
        remoteDataSource.updateConversationStatus(conversationId, status)
    }

    fun assignCounsellor(conversationId: String, counsellorId: String, counsellorName: String) {
        remoteDataSource.assignConversationCounsellor(conversationId, counsellorId, counsellorName)
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        remoteDataSource.deleteChatMessage(conversationId, messageId)
    }

    fun reportOrBlock(conversationId: String, isReported: Boolean, isBlocked: Boolean) {
        remoteDataSource.reportOrBlockConversation(conversationId, isReported, isBlocked)
    }

    fun getCounsellors(): List<CounsellorProfile> {
        return remoteDataSource.getCounsellors()
    }
}
