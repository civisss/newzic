package com.newzic.api.controller

import com.newzic.domain.entity.MessageEntity
import com.newzic.domain.repository.MessageRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class SendMessageRequest(val content: String)

data class MessageResponse(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String?,
    val recipientId: String,
    val recipientName: String,
    val recipientAvatar: String?,
    val content: String,
    val isRead: Boolean,
    val createdAt: String
)

data class ConversationPreview(
    val userId: String,
    val displayName: String,
    val avatar: String?,
    val lastMessage: String,
    val lastMessageTime: String,
    val unread: Boolean
)

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {

    @GetMapping("/conversations")
    @Transactional(readOnly = true)
    fun getConversations(auth: Authentication): ResponseEntity<List<ConversationPreview>> {
        val userId = auth.principal as UUID
        val previews = messageRepository.findConversationPreviews(userId)
        val result = previews.map { m ->
            val otherId = if (m.sender.id == userId) m.recipient.id else m.sender.id
            val other = if (m.sender.id == userId) m.recipient else m.sender
            ConversationPreview(
                userId = otherId.toString(),
                displayName = other.displayName,
                avatar = other.avatar,
                lastMessage = m.content.take(100),
                lastMessageTime = m.createdAt.toString() + "Z",
                unread = !m.isRead && m.recipient.id == userId
            )
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping("/conversation/{otherId}")
    @Transactional(readOnly = true)
    fun getConversation(
        auth: Authentication,
        @PathVariable otherId: UUID
    ): ResponseEntity<List<MessageResponse>> {
        val userId = auth.principal as UUID
        val messages = messageRepository.findConversation(userId, otherId)
        return ResponseEntity.ok(messages.map { toResponse(it) })
    }

    @PostMapping("/send/{recipientId}")
    @Transactional
    fun sendMessage(
        auth: Authentication,
        @PathVariable recipientId: UUID,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<MessageResponse> {
        val senderId = auth.principal as UUID
        if (senderId == recipientId) {
            return ResponseEntity.badRequest().build()
        }
        val sender = userRepository.findById(senderId)
            .orElseThrow { NoSuchElementException("Sender not found") }
        val recipient = userRepository.findById(recipientId)
            .orElseThrow { NoSuchElementException("Recipient not found") }

        val message = messageRepository.save(
            MessageEntity(sender = sender, recipient = recipient, content = request.content.trim())
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(message))
    }

    @PostMapping("/read/{senderId}")
    @Transactional
    fun markAsRead(
        auth: Authentication,
        @PathVariable senderId: UUID
    ): ResponseEntity<Void> {
        val userId = auth.principal as UUID
        messageRepository.markConversationAsRead(userId, senderId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(auth: Authentication): ResponseEntity<Map<String, Long>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(mapOf("count" to messageRepository.countUnread(userId)))
    }

    private fun toResponse(m: MessageEntity) = MessageResponse(
        id = m.id.toString(),
        senderId = m.sender.id.toString(),
        senderName = m.sender.displayName,
        senderAvatar = m.sender.avatar,
        recipientId = m.recipient.id.toString(),
        recipientName = m.recipient.displayName,
        recipientAvatar = m.recipient.avatar,
        content = m.content,
        isRead = m.isRead,
        createdAt = m.createdAt.toString() + "Z"
    )
}
