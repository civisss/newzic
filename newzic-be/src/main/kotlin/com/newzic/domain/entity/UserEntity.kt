package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    var username: String,

    @Column(unique = true, nullable = false)
    var email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Column(nullable = false)
    var displayName: String,

    @Column(columnDefinition = "TEXT")
    var avatar: String? = null,

    @Column(columnDefinition = "TEXT")
    var cover: String? = null,

    @Column(columnDefinition = "TEXT")
    var bio: String? = null,

    @Column(columnDefinition = "TEXT")
    var longBio: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<ArtistRole> = mutableSetOf(),

    var followers: Int = 0,

    var following: Int = 0,

    var totalPlays: Long = 0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_genres", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "genre")
    var genres: MutableSet<String> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_tags", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = mutableSetOf(),

    var verified: Boolean = false,

    var premium: Boolean = false,

    var premiumSince: LocalDateTime? = null,

    @Column(unique = true)
    var customUrl: String? = null,

    var country: String? = null,

    var location: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_genres", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "genre")
    var preferredGenres: MutableSet<String> = mutableSetOf(),

    var lookingForCollab: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var collabDescription: String? = null,

    var weeklyGrowth: Double? = null,

    // Social links
    var spotifyUrl: String? = null,
    var youtubeMusicUrl: String? = null,
    var appleMusicUrl: String? = null,
    var soundcloudUrl: String? = null,
    var tiktokUrl: String? = null,
    var instagramUrl: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_photos", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "photo_url")
    var photos: MutableSet<String> = mutableSetOf(),

    var preferredLanguage: String? = "en",

    val joinedDate: LocalDate = LocalDate.now(),

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ArtistRole {
    SINGER, PRODUCER, BAND, MUSICIAN, BEATMAKER
}
