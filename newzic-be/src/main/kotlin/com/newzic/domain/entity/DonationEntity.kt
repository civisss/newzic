package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "donations")
class DonationEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    val fromUser: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_artist_id", nullable = false)
    val toArtist: UserEntity,

    @Column(nullable = false)
    val amountCents: Int,

    @Column(nullable = false)
    val artistCents: Int,

    @Column(nullable = false)
    val platformCents: Int,

    @Column(columnDefinition = "TEXT")
    val message: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
