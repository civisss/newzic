package com.newzic.api.dto

data class CreateJournalPostRequest(
    val content: String,
    val imageUrl: String? = null,
    val category: String = "update",
    val hashtags: List<String> = emptyList(),
    val taggedUserIds: List<String> = emptyList()
)

data class UpdateJournalPostRequest(
    val content: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val hashtags: List<String>? = null,
    val taggedUserIds: List<String>? = null
)

data class JournalPostResponse(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String?,
    val authorRole: String,
    val authorPremium: Boolean,
    val authorVerified: Boolean,
    val content: String,
    val imageUrl: String?,
    val category: String,
    val hashtags: List<String>,
    val taggedUsers: List<TaggedUserResponse>,
    val reactions: ReactionSummaryResponse,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String
)

data class TaggedUserResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val avatar: String?
)

data class ReactionSummaryResponse(
    val total: Int,
    val like: Int,
    val fire: Int,
    val music: Int,
    val hype: Int,
    val userReactions: List<String>
)

data class JournalCommentResponse(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String?,
    val content: String,
    val createdAt: String
)

data class CreateJournalCommentRequest(
    val content: String
)

data class ToggleReactionRequest(
    val type: String
)
