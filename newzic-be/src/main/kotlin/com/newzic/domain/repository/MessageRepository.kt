package com.newzic.domain.repository

import com.newzic.domain.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface MessageRepository : JpaRepository<MessageEntity, UUID> {

    @Query("""
        SELECT m FROM MessageEntity m
        WHERE (m.sender.id = :userId AND m.recipient.id = :otherId)
           OR (m.sender.id = :otherId AND m.recipient.id = :userId)
        ORDER BY m.createdAt ASC
    """)
    fun findConversation(userId: UUID, otherId: UUID): List<MessageEntity>

    @Query("""
        SELECT m FROM MessageEntity m
        WHERE m.createdAt IN (
            SELECT MAX(m2.createdAt) FROM MessageEntity m2
            WHERE m2.sender.id = :userId OR m2.recipient.id = :userId
            GROUP BY CASE
                WHEN m2.sender.id = :userId THEN m2.recipient.id
                ELSE m2.sender.id
            END
        )
        AND (m.sender.id = :userId OR m.recipient.id = :userId)
        ORDER BY m.createdAt DESC
    """)
    fun findConversationPreviews(userId: UUID): List<MessageEntity>

    @Modifying
    @Query("UPDATE MessageEntity m SET m.isRead = true WHERE m.recipient.id = :userId AND m.sender.id = :senderId AND m.isRead = false")
    fun markConversationAsRead(userId: UUID, senderId: UUID): Int

    @Query("SELECT COUNT(m) FROM MessageEntity m WHERE m.recipient.id = :userId AND m.isRead = false")
    fun countUnread(userId: UUID): Long
}
