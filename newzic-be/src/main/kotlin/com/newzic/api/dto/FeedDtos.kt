package com.newzic.api.dto

import jakarta.validation.constraints.NotBlank

data class FeedPostResponse(
    val id: String,
    val type: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val authorRole: String,
    val content: String,
    val image: String?,
    val songId: String?,
    val songTitle: String?,
    val songCover: String?,
    val timestamp: String,
    val likes: Long,
    val comments: Long,
    val reactions: ReactionsDto
)

data class CreateFeedPostRequest(
    @field:NotBlank val type: String,
    @field:NotBlank val content: String,
    val image: String? = null,
    val songId: String? = null
)
