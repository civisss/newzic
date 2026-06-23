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
    val changelog: List<String>,
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
    val endTimestampSeconds: Double?,
    val resolved: Boolean,
    val resolvedByVersionNumber: Int?,
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
    val changelog: List<String> = emptyList(),
    val duration: Int = 0,
    val resolveCommentIds: List<String> = emptyList(),
    val resolveTaskIds: List<String> = emptyList()
)

data class AddCommentRequest(
    @field:NotBlank val content: String,
    val timestampSeconds: Double,
    val endTimestampSeconds: Double? = null,
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

// ── Tasks ──

data class WorkspaceTaskResponse(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val assignedToId: String?,
    val assignedToName: String?,
    val assignedToAvatar: String?,
    val createdById: String,
    val createdByName: String,
    val timestampSeconds: Double?,
    val resolvedByVersionNumber: Int?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateTaskRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    val assignedToId: String? = null,
    val timestampSeconds: Double? = null
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val assignedToId: String? = null
)

// ── Activity Feed ──

data class WorkspaceActivityResponse(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val type: String,
    val message: String,
    val createdAt: String
)

// ── Reference Tracks ──

data class WorkspaceReferenceResponse(
    val id: String,
    val title: String,
    val artist: String?,
    val url: String?,
    val notes: String?,
    val platform: String,
    val addedById: String,
    val addedByName: String,
    val createdAt: String
)

data class AddReferenceRequest(
    @field:NotBlank val title: String,
    val artist: String? = null,
    val url: String? = null,
    val notes: String? = null,
    val platform: String = "OTHER"
)

// ── Comment actions ──

data class ResolveCommentRequest(
    val resolved: Boolean = true
)
