package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "messages")
class MessageEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    val recipient: UserEntity,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    var isRead: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
