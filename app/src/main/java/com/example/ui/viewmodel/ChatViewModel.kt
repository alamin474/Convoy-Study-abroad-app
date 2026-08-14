package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatAttachment
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMessageType
import com.example.data.model.ConversationType
import com.example.data.model.UserRole
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val filteredConversations: List<ChatConversation> = emptyList(),
    val activeConversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val unreadTotalCount: Int = 0,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val activeCategoryFilter: String = "ALL", // "ALL", "COUNSELLOR", "SUPPORT", "APPLICATION"
    val replyingToMessage: ChatMessage? = null,
    val isStaffTyping: Boolean = false,
    val pendingAttachment: ChatAttachment? = null,
    val showSecurityDialog: Boolean = false,
    val inAppNotification: String? = null
)

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentUserId: String = "user_1"

    init {
        loadConversations("user_1")
    }

    fun loadConversations(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            repository.getConversations(userId).collectLatest { list ->
                val totalUnread = list.sumOf { it.unreadCountStudent }
                _uiState.update { state ->
                    val filtered = applyFilterAndSearch(list, state.searchQuery, state.activeCategoryFilter)
                    state.copy(
                        conversations = list,
                        filteredConversations = filtered,
                        unreadTotalCount = totalUnread
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSearch(state.conversations, query, state.activeCategoryFilter)
            state.copy(searchQuery = query, filteredConversations = filtered)
        }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSearch(state.conversations, state.searchQuery, category)
            state.copy(activeCategoryFilter = category, filteredConversations = filtered)
        }
    }

    private fun applyFilterAndSearch(
        list: List<ChatConversation>,
        query: String,
        category: String
    ): List<ChatConversation> {
        return list.filter { conv ->
            val matchesCategory = when (category) {
                "COUNSELLOR" -> conv.conversationType == ConversationType.COUNSELLOR
                "SUPPORT" -> conv.conversationType == ConversationType.SUPPORT
                "APPLICATION" -> conv.conversationType == ConversationType.APPLICATION
                else -> true
            }
            val matchesSearch = query.isBlank() ||
                    conv.title.contains(query, ignoreCase = true) ||
                    conv.subTitle.contains(query, ignoreCase = true) ||
                    conv.lastMessageText.contains(query, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    fun selectConversation(conversationId: String, userId: String = currentUserId) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.markAsRead(conversationId, userId)

            launch {
                repository.getConversationById(conversationId).collectLatest { conv ->
                    _uiState.update { it.copy(activeConversation = conv) }
                }
            }

            launch {
                repository.getMessages(conversationId).collectLatest { msgs ->
                    _uiState.update { it.copy(messages = msgs, isLoading = false) }
                }
            }
        }
    }

    fun sendTextMessage(
        text: String,
        userId: String = currentUserId,
        userName: String = "Alex Rivera"
    ) {
        val activeConv = _uiState.value.activeConversation ?: return
        if (text.isBlank()) return

        val replyMessage = _uiState.value.replyingToMessage
        val newMessage = ChatMessage(
            conversationId = activeConv.conversationId,
            senderId = userId,
            senderName = userName,
            senderRole = UserRole.STUDENT,
            messageType = ChatMessageType.TEXT,
            text = text.trim(),
            replyToMessageId = replyMessage?.messageId,
            replyToText = replyMessage?.text,
            replyToSenderName = replyMessage?.senderName,
            isDelivered = true,
            isRead = false,
            createdAtTimestamp = System.currentTimeMillis()
        )

        repository.sendMessage(newMessage)
        _uiState.update { it.copy(replyingToMessage = null) }

        // Simulate intelligent staff response delay
        simulateStaffAutoReply(activeConv)
    }

    fun prepareDocumentAttachment(attachment: ChatAttachment) {
        _uiState.update { it.copy(pendingAttachment = attachment, showSecurityDialog = true) }
    }

    fun dismissSecurityDialog() {
        _uiState.update { it.copy(pendingAttachment = null, showSecurityDialog = false) }
    }

    fun confirmSendDocumentAttachment(
        userId: String = currentUserId,
        userName: String = "Alex Rivera"
    ) {
        val activeConv = _uiState.value.activeConversation ?: return
        val attachment = _uiState.value.pendingAttachment ?: return

        val newMessage = ChatMessage(
            conversationId = activeConv.conversationId,
            senderId = userId,
            senderName = userName,
            senderRole = UserRole.STUDENT,
            messageType = ChatMessageType.DOCUMENT,
            text = "Uploaded ${attachment.fileName}",
            attachment = attachment,
            isDelivered = true,
            isRead = false,
            createdAtTimestamp = System.currentTimeMillis()
        )

        repository.sendMessage(newMessage)
        _uiState.update {
            it.copy(
                pendingAttachment = null,
                showSecurityDialog = false,
                inAppNotification = "Document transmitted securely with enterprise encryption"
            )
        }

        simulateStaffAutoReply(activeConv, isDocument = true)
    }

    fun sendVoiceMessage(
        durationSec: Int,
        userId: String = currentUserId,
        userName: String = "Alex Rivera"
    ) {
        val activeConv = _uiState.value.activeConversation ?: return
        val newMessage = ChatMessage(
            conversationId = activeConv.conversationId,
            senderId = userId,
            senderName = userName,
            senderRole = UserRole.STUDENT,
            messageType = ChatMessageType.VOICE,
            text = "Voice note (${durationSec}s)",
            isDelivered = true,
            isRead = false,
            createdAtTimestamp = System.currentTimeMillis()
        )

        repository.sendMessage(newMessage)
        simulateStaffAutoReply(activeConv)
    }

    private fun simulateStaffAutoReply(conv: ChatConversation, isDocument: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isStaffTyping = true) }
            delay(2200)
            _uiState.update { it.copy(isStaffTyping = false) }

            val responseText = if (isDocument) {
                "Thank you! Your document has been safely received in our secure portal. Our admissions team will verify it shortly."
            } else {
                when (conv.conversationType) {
                    ConversationType.COUNSELLOR -> "Thanks for reaching out! I'm reviewing your request and will provide tailored guidance for your study abroad application."
                    ConversationType.SUPPORT -> "Thank you for contacting Convoy Support. A support representative is looking into your inquiry."
                    ConversationType.APPLICATION -> "Message received regarding your application. Our admissions coordinator is reviewing the update."
                    else -> "Message received! We will get back to you shortly."
                }
            }

            val replyMsg = ChatMessage(
                conversationId = conv.conversationId,
                senderId = conv.counsellorId ?: "staff_support",
                senderName = conv.counsellorName ?: "Convoy Counsellor",
                senderRole = UserRole.COUNSELOR,
                senderAvatarUrl = conv.counsellorAvatarUrl,
                messageType = ChatMessageType.TEXT,
                text = responseText,
                isDelivered = true,
                isRead = false,
                createdAtTimestamp = System.currentTimeMillis()
            )

            repository.sendMessage(replyMsg)
            _uiState.update {
                it.copy(
                    inAppNotification = "Convoy: You have a new message from ${conv.counsellorName ?: "your counsellor"}"
                )
            }
        }
    }

    fun setReplyMessage(message: ChatMessage?) {
        _uiState.update { it.copy(replyingToMessage = message) }
    }

    fun deleteMessage(messageId: String) {
        val activeConv = _uiState.value.activeConversation ?: return
        repository.deleteMessage(activeConv.conversationId, messageId)
    }

    fun reportOrBlock(isReported: Boolean, isBlocked: Boolean) {
        val activeConv = _uiState.value.activeConversation ?: return
        repository.reportOrBlock(activeConv.conversationId, isReported, isBlocked)
        _uiState.update {
            it.copy(
                inAppNotification = if (isBlocked) "Conversation blocked" else "Conversation reported to Convoy Safety Team"
            )
        }
    }

    fun createOrOpenCounsellorChat(
        userId: String = currentUserId,
        userName: String = "Alex Rivera",
        userEmail: String = "alex.rivera@example.com",
        onOpened: (String) -> Unit
    ) {
        val id = repository.createCounsellorChat(userId, userName, userEmail)
        selectConversation(id, userId)
        onOpened(id)
    }

    fun createOrOpenSupportChat(
        userId: String = currentUserId,
        userName: String = "Alex Rivera",
        userEmail: String = "alex.rivera@example.com",
        topic: String = "General Inquiry",
        onOpened: (String) -> Unit
    ) {
        val id = repository.createSupportChat(userId, userName, userEmail, topic)
        selectConversation(id, userId)
        onOpened(id)
    }

    fun createOrOpenApplicationChat(
        userId: String = currentUserId,
        userName: String = "Alex Rivera",
        userEmail: String = "alex.rivera@example.com",
        applicationId: String,
        universityName: String,
        programName: String,
        onOpened: (String) -> Unit
    ) {
        val id = repository.createApplicationChat(userId, userName, userEmail, applicationId, universityName, programName)
        selectConversation(id, userId)
        onOpened(id)
    }

    fun clearNotification() {
        _uiState.update { it.copy(inAppNotification = null) }
    }
}
