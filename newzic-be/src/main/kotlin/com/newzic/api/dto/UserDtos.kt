package com.newzic.api.dto

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val avatar: String?,
    val cover: String?,
    val bio: String?,
    val longBio: String?,
    val roles: List<String>,
    val followers: Int,
    val following: Int,
    val totalPlays: Long,
    val genres: List<String>,
    val tags: List<String>,
    val verified: Boolean,
    val premium: Boolean,
    val country: String?,
    val location: String?,
    val preferredGenres: List<String>,
    val lookingForCollab: Boolean,
    val collabDescription: String?,
    val weeklyGrowth: Double?,
    val socialLinks: SocialLinksDto,
    val photos: List<String>,
    val joinedDate: String
)

data class SocialLinksDto(
    val spotify: String? = null,
    val youtubeMusic: String? = null,
    val appleMusic: String? = null,
    val soundcloud: String? = null,
    val tiktok: String? = null,
    val instagram: String? = null
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val longBio: String? = null,
    val country: String? = null,
    val location: String? = null,
    val genres: List<String>? = null,
    val tags: List<String>? = null,
    val preferredGenres: List<String>? = null,
    val lookingForCollab: Boolean? = null,
    val collabDescription: String? = null,
    val socialLinks: SocialLinksDto? = null
)
