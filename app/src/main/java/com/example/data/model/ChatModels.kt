package com.example.data.model

import java.util.UUID

enum class ConversationType(val label: String) {
    COUNSELLOR("Study Abroad Counsellor"),
    SUPPORT("Convoy Support Desk"),
    APPLICATION("Application Support"),
    GENERAL("Direct Inquiry")
}

enum class ConversationStatus(val label: String) {
    ACTIVE("Active"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class ChatMessageType {
    TEXT,
    IMAGE,
    DOCUMENT,
    VOICE,
    SYSTEM
}

data class ChatAttachment(
    val attachmentId: String = UUID.randomUUID().toString(),
    val fileName: String,
    val fileType: String, // e.g. "application/pdf", "image/jpeg"
    val fileSizeFormatted: String, // e.g. "1.8 MB"
    val fileUrl: String,
    val isSensitive: Boolean = true,
    val categoryLabel: String = "Application Document"
)

data class ChatMessage(
    val messageId: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole = UserRole.STUDENT,
    val senderAvatarUrl: String? = null,
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val text: String = "",
    val attachment: ChatAttachment? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val isDelivered: Boolean = true,
    val isRead: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

data class InternalNote(
    val noteId: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

data class CounsellorProfile(
    val id: String,
    val name: String,
    val roleTitle: String = "Senior Study Abroad Counsellor",
    val avatarUrl: String,
    val specialization: String,
    val yearsExperience: String = "6+ Years Experience",
    val isOnline: Boolean = true
)

data class ChatConversation(
    val conversationId: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val studentEmail: String,
    val studentAvatarUrl: String? = null,
    val conversationType: ConversationType,
    val title: String,
    val subTitle: String = "",
    val counsellorId: String? = "counsellor_elena",
    val counsellorName: String? = "Elena Vance",
    val counsellorAvatarUrl: String? = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&q=80",
    val counsellorRole: String? = "Senior Study Abroad Counsellor",
    val isCounsellorOnline: Boolean = true,
    val applicationId: String? = null,
    val applicationStatus: String? = null,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCountStudent: Int = 0,
    val unreadCountStaff: Int = 0,
    val status: ConversationStatus = ConversationStatus.ACTIVE,
    val isReported: Boolean = false,
    val isBlocked: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
