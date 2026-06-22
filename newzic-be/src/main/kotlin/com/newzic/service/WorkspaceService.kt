package com.newzic.service

import com.newzic.api.dto.*
import com.newzic.domain.entity.*
import com.newzic.domain.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository,
    private val memberRepository: WorkspaceMemberRepository,
    private val versionRepository: WorkspaceVersionRepository,
    private val commentRepository: WorkspaceCommentRepository,
    private val fileRepository: WorkspaceFileRepository,
    private val chatRepository: WorkspaceChatRepository,
    private val userRepository: UserRepository,
    private val collaborationRepository: CollaborationRepository,
    private val notificationService: NotificationService
) {

    // ═══════════════════════════════════════════
    // Workspace CRUD
    // ═══════════════════════════════════════════

    @Transactional
    fun create(userId: UUID, request: CreateWorkspaceRequest): WorkspaceResponse {
        val owner = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val collab = request.collaborationId?.let {
            collaborationRepository.findById(UUID.fromString(it)).orElse(null)
        }

        val workspace = workspaceRepository.save(
            WorkspaceEntity(
                title = request.title,
                description = request.description,
                owner = owner,
                collaboration = collab
            )
        )

        // Add owner as OWNER member
        memberRepository.save(
            WorkspaceMemberEntity(
                workspace = workspace,
                user = owner,
                role = WorkspaceMemberRole.OWNER
            )
        )

        // Invite additional members
        request.inviteUserIds.forEach { inviteeId ->
            val inviteeUUID = UUID.fromString(inviteeId)
            if (inviteeUUID != userId) {
                addMember(workspace, inviteeUUID, userId)
            }
        }

        return toWorkspaceResponse(workspace)
    }

    @Transactional(readOnly = true)
    fun getById(workspaceId: UUID, userId: UUID): WorkspaceResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, userId)
        return toWorkspaceResponse(workspace)
    }

    @Transactional(readOnly = true)
    fun getMyWorkspaces(userId: UUID): List<WorkspaceResponse> {
        return workspaceRepository.findByMemberUserId(userId)
            .map { toWorkspaceResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getMyWorkspacesByStatus(userId: UUID, status: String): List<WorkspaceResponse> {
        val wsStatus = WorkspaceStatus.valueOf(status.uppercase())
        return workspaceRepository.findByMemberUserIdAndStatus(userId, wsStatus)
            .map { toWorkspaceResponse(it) }
    }

    @Transactional
    fun update(workspaceId: UUID, userId: UUID, request: UpdateWorkspaceRequest): WorkspaceResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, userId)

        request.title?.let { workspace.title = it }
        request.description?.let { workspace.description = it }
        request.status?.let { workspace.status = WorkspaceStatus.valueOf(it.uppercase()) }
        workspace.updatedAt = LocalDateTime.now()

        return toWorkspaceResponse(workspaceRepository.save(workspace))
    }

    // ═══════════════════════════════════════════
    // Members
    // ═══════════════════════════════════════════

    @Transactional
    fun inviteMember(workspaceId: UUID, inviterId: UUID, inviteeId: UUID): WorkspaceMemberResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, inviterId)

        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, inviteeId)) {
            throw IllegalStateException("User is already a member")
        }

        val member = addMember(workspace, inviteeId, inviterId)
        return toMemberResponse(member)
    }

    @Transactional(readOnly = true)
    fun getMembers(workspaceId: UUID, userId: UUID): List<WorkspaceMemberResponse> {
        requireMember(workspaceId, userId)
        return memberRepository.findByWorkspaceId(workspaceId).map { toMemberResponse(it) }
    }

    // ═══════════════════════════════════════════
    // Versions
    // ═══════════════════════════════════════════

    @Transactional
    fun uploadVersion(workspaceId: UUID, userId: UUID, request: UploadVersionRequest): WorkspaceVersionResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, userId)

        val uploader = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val nextVersion = versionRepository.findMaxVersionNumber(workspaceId) + 1

        val version = versionRepository.save(
            WorkspaceVersionEntity(
                workspace = workspace,
                versionNumber = nextVersion,
                audioUrl = request.audioUrl,
                notes = request.notes,
                uploadedBy = uploader,
                duration = request.duration
            )
        )

        // Update workspace status and timestamp
        if (workspace.status == WorkspaceStatus.DRAFT) {
            workspace.status = WorkspaceStatus.IN_PROGRESS
        }
        workspace.updatedAt = LocalDateTime.now()
        workspaceRepository.save(workspace)

        // Notify other members
        notifyOtherMembers(workspaceId, userId, NotificationType.WORKSPACE_VERSION,
            "${uploader.displayName} uploaded v$nextVersion to \"${workspace.title}\"",
            "/workspace/$workspaceId")

        return toVersionResponse(version)
    }

    @Transactional(readOnly = true)
    fun getVersions(workspaceId: UUID, userId: UUID): List<WorkspaceVersionResponse> {
        requireMember(workspaceId, userId)
        return versionRepository.findByWorkspaceIdOrderByVersionNumberDesc(workspaceId)
            .map { toVersionResponse(it) }
    }

    // ═══════════════════════════════════════════
    // Timestamped Comments
    // ═══════════════════════════════════════════

    @Transactional
    fun addComment(workspaceId: UUID, versionId: UUID, userId: UUID, request: AddCommentRequest): WorkspaceCommentResponse {
        requireMember(workspaceId, userId)

        val version = versionRepository.findById(versionId)
            .orElseThrow { NoSuchElementException("Version not found") }

        if (version.workspace.id != workspaceId) {
            throw IllegalArgumentException("Version does not belong to this workspace")
        }

        val author = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val parent = request.parentId?.let {
            commentRepository.findById(UUID.fromString(it)).orElse(null)
        }

        val comment = commentRepository.save(
            WorkspaceCommentEntity(
                version = version,
                author = author,
                content = request.content,
                timestampSeconds = request.timestampSeconds,
                parent = parent
            )
        )

        // Update workspace timestamp
        val workspace = version.workspace
        workspace.updatedAt = LocalDateTime.now()
        workspaceRepository.save(workspace)

        // Format timestamp for notification
        val mins = (request.timestampSeconds / 60).toInt()
        val secs = (request.timestampSeconds % 60).toInt()
        val timeStr = "${mins}:${secs.toString().padStart(2, '0')}"

        notifyOtherMembers(workspaceId, userId, NotificationType.WORKSPACE_COMMENT,
            "${author.displayName} commented at $timeStr on \"${workspace.title}\"",
            "/workspace/$workspaceId")

        return toCommentResponse(comment)
    }

    @Transactional(readOnly = true)
    fun getComments(workspaceId: UUID, versionId: UUID, userId: UUID): List<WorkspaceCommentResponse> {
        requireMember(workspaceId, userId)
        // Get top-level comments with replies
        val topLevel = commentRepository.findByVersionIdAndParentIsNullOrderByTimestampSecondsAsc(versionId)
        return topLevel.map { toCommentResponseWithReplies(it) }
    }

    // ═══════════════════════════════════════════
    // Files
    // ═══════════════════════════════════════════

    @Transactional
    fun uploadFile(workspaceId: UUID, userId: UUID, request: UploadFileRequest): WorkspaceFileResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, userId)

        val uploader = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val fileType = try {
            WorkspaceFileType.valueOf(request.fileType.uppercase())
        } catch (e: Exception) {
            WorkspaceFileType.OTHER
        }

        val file = fileRepository.save(
            WorkspaceFileEntity(
                workspace = workspace,
                name = request.name,
                url = request.url,
                fileType = fileType,
                sizeBytes = request.sizeBytes,
                uploadedBy = uploader
            )
        )

        workspace.updatedAt = LocalDateTime.now()
        workspaceRepository.save(workspace)

        return toFileResponse(file)
    }

    @Transactional(readOnly = true)
    fun getFiles(workspaceId: UUID, userId: UUID): List<WorkspaceFileResponse> {
        requireMember(workspaceId, userId)
        return fileRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
            .map { toFileResponse(it) }
    }

    @Transactional
    fun deleteFile(workspaceId: UUID, fileId: UUID, userId: UUID) {
        requireMember(workspaceId, userId)
        val file = fileRepository.findById(fileId)
            .orElseThrow { NoSuchElementException("File not found") }
        if (file.workspace.id != workspaceId) {
            throw IllegalArgumentException("File does not belong to this workspace")
        }
        fileRepository.delete(file)
    }

    // ═══════════════════════════════════════════
    // Chat
    // ═══════════════════════════════════════════

    @Transactional
    fun sendChatMessage(workspaceId: UUID, userId: UUID, request: SendChatMessageRequest): WorkspaceChatMessageResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, userId)

        val sender = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val msg = chatRepository.save(
            WorkspaceChatEntity(
                workspace = workspace,
                sender = sender,
                content = request.content
            )
        )

        return toChatResponse(msg)
    }

    @Transactional(readOnly = true)
    fun getChatMessages(workspaceId: UUID, userId: UUID, page: Int, size: Int): List<WorkspaceChatMessageResponse> {
        requireMember(workspaceId, userId)
        return chatRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, PageRequest.of(page, size))
            .content
            .reversed() // Oldest first in the returned page
            .map { toChatResponse(it) }
    }

    // ═══════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════

    private fun requireMember(workspaceId: UUID, userId: UUID) {
        if (!memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw SecurityException("You are not a member of this workspace")
        }
    }

    private fun addMember(workspace: WorkspaceEntity, inviteeId: UUID, inviterId: UUID): WorkspaceMemberEntity {
        val invitee = userRepository.findById(inviteeId)
            .orElseThrow { NoSuchElementException("User not found: $inviteeId") }

        val member = memberRepository.save(
            WorkspaceMemberEntity(
                workspace = workspace,
                user = invitee,
                role = WorkspaceMemberRole.COLLABORATOR
            )
        )

        notificationService.create(
            recipientId = inviteeId,
            fromUserId = inviterId,
            type = NotificationType.WORKSPACE_INVITE,
            message = "You've been invited to workspace \"${workspace.title}\"",
            link = "/workspace/${workspace.id}"
        )

        return member
    }

    private fun notifyOtherMembers(workspaceId: UUID, excludeUserId: UUID, type: NotificationType, message: String, link: String) {
        val members = memberRepository.findByWorkspaceId(workspaceId)
        members.filter { it.user.id != excludeUserId }.forEach { member ->
            notificationService.create(
                recipientId = member.user.id,
                fromUserId = excludeUserId,
                type = type,
                message = message,
                link = link
            )
        }
    }

    // ═══════════════════════════════════════════
    // Mappers
    // ═══════════════════════════════════════════

    private fun toWorkspaceResponse(ws: WorkspaceEntity): WorkspaceResponse {
        val members = memberRepository.findByWorkspaceId(ws.id)
        val latestVersion = versionRepository.findFirstByWorkspaceIdOrderByVersionNumberDesc(ws.id)
        val versionCount = versionRepository.countByWorkspaceId(ws.id).toInt()
        val commentCount = latestVersion?.let { commentRepository.countByVersionId(it.id).toInt() } ?: 0
        val fileCount = fileRepository.countByWorkspaceId(ws.id).toInt()

        return WorkspaceResponse(
            id = ws.id.toString(),
            title = ws.title,
            description = ws.description,
            status = ws.status.name.lowercase(),
            ownerId = ws.owner.id.toString(),
            ownerName = ws.owner.displayName,
            ownerAvatar = ws.owner.avatar,
            collaborationId = ws.collaboration?.id?.toString(),
            publishedSongId = ws.publishedSong?.id?.toString(),
            members = members.map { toMemberResponse(it) },
            versionCount = versionCount,
            commentCount = commentCount,
            fileCount = fileCount,
            createdAt = ws.createdAt.toString(),
            updatedAt = ws.updatedAt.toString()
        )
    }

    private fun toMemberResponse(m: WorkspaceMemberEntity) = WorkspaceMemberResponse(
        id = m.id.toString(),
        userId = m.user.id.toString(),
        displayName = m.user.displayName,
        avatar = m.user.avatar,
        role = m.role.name.lowercase(),
        joinedAt = m.joinedAt.toString()
    )

    private fun toVersionResponse(v: WorkspaceVersionEntity): WorkspaceVersionResponse {
        val commentCount = commentRepository.countByVersionId(v.id).toInt()
        return WorkspaceVersionResponse(
            id = v.id.toString(),
            versionNumber = v.versionNumber,
            audioUrl = v.audioUrl,
            notes = v.notes,
            uploadedById = v.uploadedBy.id.toString(),
            uploadedByName = v.uploadedBy.displayName,
            uploadedByAvatar = v.uploadedBy.avatar,
            duration = v.duration,
            commentCount = commentCount,
            createdAt = v.createdAt.toString()
        )
    }

    private fun toCommentResponse(c: WorkspaceCommentEntity) = WorkspaceCommentResponse(
        id = c.id.toString(),
        authorId = c.author.id.toString(),
        authorName = c.author.displayName,
        authorAvatar = c.author.avatar,
        content = c.content,
        timestampSeconds = c.timestampSeconds,
        parentId = c.parent?.id?.toString(),
        replies = emptyList(),
        createdAt = c.createdAt.toString()
    )

    private fun toCommentResponseWithReplies(c: WorkspaceCommentEntity): WorkspaceCommentResponse {
        val replies = commentRepository.findByParentIdOrderByCreatedAtAsc(c.id)
        return WorkspaceCommentResponse(
            id = c.id.toString(),
            authorId = c.author.id.toString(),
            authorName = c.author.displayName,
            authorAvatar = c.author.avatar,
            content = c.content,
            timestampSeconds = c.timestampSeconds,
            parentId = null,
            replies = replies.map { toCommentResponse(it) },
            createdAt = c.createdAt.toString()
        )
    }

    private fun toFileResponse(f: WorkspaceFileEntity) = WorkspaceFileResponse(
        id = f.id.toString(),
        name = f.name,
        url = f.url,
        fileType = f.fileType.name.lowercase(),
        sizeBytes = f.sizeBytes,
        uploadedById = f.uploadedBy.id.toString(),
        uploadedByName = f.uploadedBy.displayName,
        createdAt = f.createdAt.toString()
    )

    private fun toChatResponse(m: WorkspaceChatEntity) = WorkspaceChatMessageResponse(
        id = m.id.toString(),
        senderId = m.sender.id.toString(),
        senderName = m.sender.displayName,
        senderAvatar = m.sender.avatar,
        content = m.content,
        createdAt = m.createdAt.toString()
    )
}
