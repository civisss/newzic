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
    private val taskRepository: WorkspaceTaskRepository,
    private val activityRepository: WorkspaceActivityRepository,
    private val referenceRepository: WorkspaceReferenceRepository,
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
        request.status?.let {
            val newStatus = WorkspaceStatus.valueOf(it.uppercase())
            if (newStatus != workspace.status) {
                workspace.status = newStatus
                val user = userRepository.findById(userId).orElseThrow()
                logActivity(workspace, user, ActivityType.STATUS_CHANGED,
                    "changed status to ${newStatus.name.lowercase()}")
            }
        }
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

        val invitee = userRepository.findById(inviteeId).orElseThrow()
        logActivity(workspace, invitee, ActivityType.MEMBER_JOINED,
            "joined the workspace")

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
                changelog = request.changelog.toMutableList(),
                uploadedBy = uploader,
                duration = request.duration
            )
        )

        // Resolve linked comments
        request.resolveCommentIds.forEach { commentIdStr ->
            try {
                val comment = commentRepository.findById(UUID.fromString(commentIdStr)).orElse(null)
                if (comment != null && !comment.resolved) {
                    comment.resolved = true
                    comment.resolvedByVersion = version
                    commentRepository.save(comment)
                }
            } catch (_: Exception) {}
        }

        // Resolve linked tasks
        request.resolveTaskIds.forEach { taskIdStr ->
            try {
                val task = taskRepository.findById(UUID.fromString(taskIdStr)).orElse(null)
                if (task != null && task.status != TaskStatus.DONE) {
                    task.status = TaskStatus.DONE
                    task.resolvedByVersion = version
                    task.updatedAt = LocalDateTime.now()
                    taskRepository.save(task)
                }
            } catch (_: Exception) {}
        }

        // Update workspace status and timestamp
        if (workspace.status == WorkspaceStatus.DRAFT) {
            workspace.status = WorkspaceStatus.IN_PROGRESS
        }
        workspace.updatedAt = LocalDateTime.now()
        workspaceRepository.save(workspace)

        // Log activity
        val changelogText = if (request.changelog.isNotEmpty()) ": ${request.changelog.joinToString(", ")}" else ""
        logActivity(workspace, uploader, ActivityType.VERSION_UPLOADED,
            "uploaded v$nextVersion$changelogText")

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
                endTimestampSeconds = request.endTimestampSeconds,
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
        val rangeStr = if (request.endTimestampSeconds != null) {
            val endMins = (request.endTimestampSeconds / 60).toInt()
            val endSecs = (request.endTimestampSeconds % 60).toInt()
            "$timeStr-${endMins}:${endSecs.toString().padStart(2, '0')}"
        } else timeStr

        // Log activity
        logActivity(workspace, author, ActivityType.COMMENT_ADDED,
            "commented at $rangeStr")

        notifyOtherMembers(workspaceId, userId, NotificationType.WORKSPACE_COMMENT,
            "${author.displayName} commented at $rangeStr on \"${workspace.title}\"",
            "/workspace/$workspaceId")

        return toCommentResponse(comment)
    }

    @Transactional
    fun resolveComment(workspaceId: UUID, versionId: UUID, commentId: UUID, userId: UUID, resolved: Boolean): WorkspaceCommentResponse {
        requireMember(workspaceId, userId)
        val comment = commentRepository.findById(commentId)
            .orElseThrow { NoSuchElementException("Comment not found") }
        comment.resolved = resolved
        if (!resolved) comment.resolvedByVersion = null
        commentRepository.save(comment)

        if (resolved) {
            val workspace = comment.version.workspace
            val user = userRepository.findById(userId).orElseThrow()
            logActivity(workspace, user, ActivityType.COMMENT_RESOLVED,
                "resolved a comment at ${formatTimeStr(comment.timestampSeconds)}")
        }

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
    // Tasks
    // ═══════════════════════════════════════════

    @Transactional
    fun createTask(workspaceId: UUID, userId: UUID, request: CreateTaskRequest): WorkspaceTaskResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, userId)

        val creator = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val assignee = request.assignedToId?.let {
            userRepository.findById(UUID.fromString(it)).orElse(null)
        }

        val task = taskRepository.save(
            WorkspaceTaskEntity(
                workspace = workspace,
                title = request.title,
                description = request.description,
                assignedTo = assignee,
                createdBy = creator,
                timestampSeconds = request.timestampSeconds
            )
        )

        workspace.updatedAt = LocalDateTime.now()
        workspaceRepository.save(workspace)

        logActivity(workspace, creator, ActivityType.TASK_CREATED,
            "created task \"${request.title}\"")

        // Notify assigned user
        if (assignee != null && assignee.id != userId) {
            notificationService.create(
                recipientId = assignee.id,
                fromUserId = userId,
                type = NotificationType.WORKSPACE_TASK,
                message = "${creator.displayName} assigned you \"${request.title}\" in \"${workspace.title}\"",
                link = "/workspace/$workspaceId"
            )
        }

        return toTaskResponse(task)
    }

    @Transactional(readOnly = true)
    fun getTasks(workspaceId: UUID, userId: UUID): List<WorkspaceTaskResponse> {
        requireMember(workspaceId, userId)
        return taskRepository.findByWorkspaceIdOrderByCreatedAtAsc(workspaceId)
            .map { toTaskResponse(it) }
    }

    @Transactional
    fun updateTask(workspaceId: UUID, taskId: UUID, userId: UUID, request: UpdateTaskRequest): WorkspaceTaskResponse {
        requireMember(workspaceId, userId)
        val task = taskRepository.findById(taskId)
            .orElseThrow { NoSuchElementException("Task not found") }
        if (task.workspace.id != workspaceId) {
            throw IllegalArgumentException("Task does not belong to this workspace")
        }

        val user = userRepository.findById(userId).orElseThrow()
        val workspace = task.workspace

        request.title?.let { task.title = it }
        request.description?.let { task.description = it }
        request.status?.let {
            val newStatus = TaskStatus.valueOf(it.uppercase())
            if (newStatus == TaskStatus.DONE && task.status != TaskStatus.DONE) {
                logActivity(workspace, user, ActivityType.TASK_COMPLETED,
                    "completed task \"${task.title}\"")
            }
            task.status = newStatus
        }
        request.assignedToId?.let { assignedIdStr ->
            val assignee = userRepository.findById(UUID.fromString(assignedIdStr)).orElse(null)
            task.assignedTo = assignee
            if (assignee != null && assignee.id != userId) {
                logActivity(workspace, user, ActivityType.TASK_ASSIGNED,
                    "assigned \"${task.title}\" to ${assignee.displayName}")
                notificationService.create(
                    recipientId = assignee.id,
                    fromUserId = userId,
                    type = NotificationType.WORKSPACE_TASK,
                    message = "${user.displayName} assigned you \"${task.title}\" in \"${workspace.title}\"",
                    link = "/workspace/$workspaceId"
                )
            }
        }
        task.updatedAt = LocalDateTime.now()
        taskRepository.save(task)

        return toTaskResponse(task)
    }

    @Transactional
    fun deleteTask(workspaceId: UUID, taskId: UUID, userId: UUID) {
        requireMember(workspaceId, userId)
        val task = taskRepository.findById(taskId)
            .orElseThrow { NoSuchElementException("Task not found") }
        if (task.workspace.id != workspaceId) {
            throw IllegalArgumentException("Task does not belong to this workspace")
        }
        taskRepository.delete(task)
    }

    // ═══════════════════════════════════════════
    // Activity Feed
    // ═══════════════════════════════════════════

    @Transactional(readOnly = true)
    fun getActivities(workspaceId: UUID, userId: UUID, page: Int, size: Int): List<WorkspaceActivityResponse> {
        requireMember(workspaceId, userId)
        return activityRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, PageRequest.of(page, size))
            .content
            .map { toActivityResponse(it) }
    }

    // ═══════════════════════════════════════════
    // Reference Tracks
    // ═══════════════════════════════════════════

    @Transactional
    fun addReference(workspaceId: UUID, userId: UUID, request: AddReferenceRequest): WorkspaceReferenceResponse {
        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
        requireMember(workspaceId, userId)

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val platform = try {
            ReferencePlatform.valueOf(request.platform.uppercase())
        } catch (e: Exception) {
            ReferencePlatform.OTHER
        }

        val ref = referenceRepository.save(
            WorkspaceReferenceEntity(
                workspace = workspace,
                title = request.title,
                artist = request.artist,
                url = request.url,
                notes = request.notes,
                platform = platform,
                addedBy = user
            )
        )

        workspace.updatedAt = LocalDateTime.now()
        workspaceRepository.save(workspace)

        val artistStr = request.artist?.let { " - $it" } ?: ""
        logActivity(workspace, user, ActivityType.REFERENCE_ADDED,
            "added reference \"${request.title}$artistStr\"")

        return toReferenceResponse(ref)
    }

    @Transactional(readOnly = true)
    fun getReferences(workspaceId: UUID, userId: UUID): List<WorkspaceReferenceResponse> {
        requireMember(workspaceId, userId)
        return referenceRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
            .map { toReferenceResponse(it) }
    }

    @Transactional
    fun deleteReference(workspaceId: UUID, refId: UUID, userId: UUID) {
        requireMember(workspaceId, userId)
        val ref = referenceRepository.findById(refId)
            .orElseThrow { NoSuchElementException("Reference not found") }
        if (ref.workspace.id != workspaceId) {
            throw IllegalArgumentException("Reference does not belong to this workspace")
        }
        referenceRepository.delete(ref)
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

    private fun logActivity(workspace: WorkspaceEntity, user: UserEntity, type: ActivityType, message: String) {
        activityRepository.save(
            WorkspaceActivityEntity(
                workspace = workspace,
                user = user,
                type = type,
                message = message
            )
        )
    }

    private fun formatTimeStr(seconds: Double): String {
        val mins = (seconds / 60).toInt()
        val secs = (seconds % 60).toInt()
        return "${mins}:${secs.toString().padStart(2, '0')}"
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
            createdAt = ws.createdAt.toString() + "Z",
            updatedAt = ws.updatedAt.toString() + "Z"
        )
    }

    private fun toMemberResponse(m: WorkspaceMemberEntity) = WorkspaceMemberResponse(
        id = m.id.toString(),
        userId = m.user.id.toString(),
        displayName = m.user.displayName,
        avatar = m.user.avatar,
        role = m.role.name.lowercase(),
        joinedAt = m.joinedAt.toString() + "Z"
    )

    private fun toVersionResponse(v: WorkspaceVersionEntity): WorkspaceVersionResponse {
        val commentCount = commentRepository.countByVersionId(v.id).toInt()
        return WorkspaceVersionResponse(
            id = v.id.toString(),
            versionNumber = v.versionNumber,
            audioUrl = v.audioUrl,
            notes = v.notes,
            changelog = v.changelog.toList(),
            uploadedById = v.uploadedBy.id.toString(),
            uploadedByName = v.uploadedBy.displayName,
            uploadedByAvatar = v.uploadedBy.avatar,
            duration = v.duration,
            commentCount = commentCount,
            createdAt = v.createdAt.toString() + "Z"
        )
    }

    private fun toCommentResponse(c: WorkspaceCommentEntity) = WorkspaceCommentResponse(
        id = c.id.toString(),
        authorId = c.author.id.toString(),
        authorName = c.author.displayName,
        authorAvatar = c.author.avatar,
        content = c.content,
        timestampSeconds = c.timestampSeconds,
        endTimestampSeconds = c.endTimestampSeconds,
        resolved = c.resolved,
        resolvedByVersionNumber = c.resolvedByVersion?.versionNumber,
        parentId = c.parent?.id?.toString(),
        replies = emptyList(),
        createdAt = c.createdAt.toString() + "Z"
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
            endTimestampSeconds = c.endTimestampSeconds,
            resolved = c.resolved,
            resolvedByVersionNumber = c.resolvedByVersion?.versionNumber,
            parentId = null,
            replies = replies.map { toCommentResponse(it) },
            createdAt = c.createdAt.toString() + "Z"
        )
    }

    private fun toTaskResponse(t: WorkspaceTaskEntity) = WorkspaceTaskResponse(
        id = t.id.toString(),
        title = t.title,
        description = t.description,
        status = t.status.name.lowercase(),
        assignedToId = t.assignedTo?.id?.toString(),
        assignedToName = t.assignedTo?.displayName,
        assignedToAvatar = t.assignedTo?.avatar,
        createdById = t.createdBy.id.toString(),
        createdByName = t.createdBy.displayName,
        timestampSeconds = t.timestampSeconds,
        resolvedByVersionNumber = t.resolvedByVersion?.versionNumber,
        createdAt = t.createdAt.toString() + "Z",
        updatedAt = t.updatedAt.toString() + "Z"
    )

    private fun toActivityResponse(a: WorkspaceActivityEntity) = WorkspaceActivityResponse(
        id = a.id.toString(),
        userId = a.user.id.toString(),
        userName = a.user.displayName,
        userAvatar = a.user.avatar,
        type = a.type.name.lowercase(),
        message = a.message,
        createdAt = a.createdAt.toString() + "Z"
    )

    private fun toReferenceResponse(r: WorkspaceReferenceEntity) = WorkspaceReferenceResponse(
        id = r.id.toString(),
        title = r.title,
        artist = r.artist,
        url = r.url,
        notes = r.notes,
        platform = r.platform.name.lowercase(),
        addedById = r.addedBy.id.toString(),
        addedByName = r.addedBy.displayName,
        createdAt = r.createdAt.toString() + "Z"
    )

    private fun toFileResponse(f: WorkspaceFileEntity) = WorkspaceFileResponse(
        id = f.id.toString(),
        name = f.name,
        url = f.url,
        fileType = f.fileType.name.lowercase(),
        sizeBytes = f.sizeBytes,
        uploadedById = f.uploadedBy.id.toString(),
        uploadedByName = f.uploadedBy.displayName,
        createdAt = f.createdAt.toString() + "Z"
    )

    private fun toChatResponse(m: WorkspaceChatEntity) = WorkspaceChatMessageResponse(
        id = m.id.toString(),
        senderId = m.sender.id.toString(),
        senderName = m.sender.displayName,
        senderAvatar = m.sender.avatar,
        content = m.content,
        createdAt = m.createdAt.toString() + "Z"
    )
}
