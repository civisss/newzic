package com.newzic.domain.repository

import com.newzic.domain.entity.NotificationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface NotificationRepository : JpaRepository<NotificationEntity, UUID> {

    fun findByRecipientIdOrderByCreatedAtDesc(recipientId: UUID, pageable: Pageable): Page<NotificationEntity>

    fun countByRecipientIdAndIsReadFalse(recipientId: UUID): Long

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.recipient.id = :recipientId")
    fun markAllAsRead(recipientId: UUID)
}
