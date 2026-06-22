package com.newzic.api.dto

import jakarta.validation.constraints.NotBlank

// ── Responses ──

data class WorkspaceResponse(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val ownerId: String,
    val ownerName: String,
    val ownerAvatar: String?,
    val collaborationId: String?,
    val publishedSongId: String?,
    val members: List<WorkspaceMemberResponse>,
    val versionCount: Int,
    val commentCount: Int,
    val fileCount: Int,
    val createdAt: String,
    val updatedAt: String
)

data class WorkspaceMemberResponse(
    val id: String,
    val userId: String,
    val displayName: String,
    val avatar: String?,
    val role: String,
    val joinedAt: String
)

data class WorkspaceVersionResponse(
    val id: String,
    val versionNumber: Int,
    val audioUrl: String,
    val notes: String?,
    val uploadedById: String,
    val uploadedByName: String,
    val uploadedByAvatar: String?,
    val duration: Int,
    val commentCount: Int,
    val createdAt: String
)

data class WorkspaceCommentResponse(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val content: String,
    val timestampSeconds: Double,
    val parentId: String?,
    val replies: List<WorkspaceCommentResponse>,
    val createdAt: String
)

data class WorkspaceFileResponse(
    val id: String,
    val name: String,
    val url: String,
    val fileType: String,
    val sizeBytes: Long,
    val uploadedById: String,
    val uploadedByName: String,
    val createdAt: String
)

data class WorkspaceChatMessageResponse(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String?,
    val content: String,
    val createdAt: String
)

// ── Requests ──

data class CreateWorkspaceRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    val collaborationId: String? = null,
    val inviteUserIds: List<String> = emptyList()
)

data class UpdateWorkspaceRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null
)

data class UploadVersionRequest(
    @field:NotBlank val audioUrl: String,
    val notes: String? = null,
    val duration: Int = 0
)

data class AddCommentRequest(
    @field:NotBlank val content: String,
    val timestampSeconds: Double,
    val parentId: String? = null
)

data class UploadFileRequest(
    @field:NotBlank val name: String,
    @field:NotBlank val url: String,
    val fileType: String = "OTHER",
    val sizeBytes: Long = 0
)

data class SendChatMessageRequest(
    @field:NotBlank val content: String
)

data class InviteMemberRequest(
    @field:NotBlank val userId: String
)
