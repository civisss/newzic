package com.newzic.service

import com.newzic.domain.entity.NotificationEntity
import com.newzic.domain.entity.NotificationType
import com.newzic.domain.repository.NotificationRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class NotificationResponse(
    val id: String,
    val type: String,
    val message: String,
    val avatar: String?,
    val fromUser: String?,
    val timestamp: String,
    val read: Boolean,
    val link: String?
)

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {

    fun getForUser(userId: UUID, pageable: Pageable): Page<NotificationResponse> {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
            .map { toResponse(it) }
    }

    fun getUnreadCount(userId: UUID): Long {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId)
    }

    @Transactional
    fun markAllAsRead(userId: UUID) {
        notificationRepository.markAllAsRead(userId)
    }

    @Transactional
    fun create(recipientId: UUID, fromUserId: UUID?, type: NotificationType, message: String, link: String? = null) {
        val recipient = userRepository.findById(recipientId).orElse(null) ?: return
        val fromUser = fromUserId?.let { userRepository.findById(it).orElse(null) }

        notificationRepository.save(
            NotificationEntity(
                type = type,
                message = message,
                recipient = recipient,
                fromUser = fromUser,
                link = link
            )
        )
    }

    private fun toResponse(n: NotificationEntity): NotificationResponse {
        return NotificationResponse(
            id = n.id.toString(),
            type = n.type.name.lowercase(),
            message = n.message,
            avatar = n.fromUser?.avatar,
            fromUser = n.fromUser?.displayName,
            timestamp = n.createdAt.toString(),
            read = n.isRead,
            link = n.link
        )
    }
}
