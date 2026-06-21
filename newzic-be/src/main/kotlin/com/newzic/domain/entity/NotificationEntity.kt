package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notifications")
class NotificationEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: NotificationType,

    @Column(nullable = false)
    var message: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    var recipient: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    var fromUser: UserEntity? = null,

    var link: String? = null,

    var songId: UUID? = null,

    var isRead: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    FOLLOW, REACTION, LIKE, COMMENT, RELEASE, MILESTONE
}
