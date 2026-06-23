package com.newzic.service

import com.newzic.api.dto.*
import com.newzic.domain.entity.*
import com.newzic.domain.repository.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockitoExtension::class)
class WorkspaceServiceTest {

    @Mock private lateinit var workspaceRepository: WorkspaceRepository
    @Mock private lateinit var memberRepository: WorkspaceMemberRepository
    @Mock private lateinit var versionRepository: WorkspaceVersionRepository
    @Mock private lateinit var commentRepository: WorkspaceCommentRepository
    @Mock private lateinit var fileRepository: WorkspaceFileRepository
    @Mock private lateinit var chatRepository: WorkspaceChatRepository
    @Mock private lateinit var taskRepository: WorkspaceTaskRepository
    @Mock private lateinit var activityRepository: WorkspaceActivityRepository
    @Mock private lateinit var referenceRepository: WorkspaceReferenceRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var collaborationRepository: CollaborationRepository
    @Mock private lateinit var notificationService: NotificationService

    @InjectMocks
    private lateinit var workspaceService: WorkspaceService

    private lateinit var owner: UserEntity
    private lateinit var collaborator: UserEntity

    @BeforeEach
    fun setUp() {
        owner = createUser("Owner")
        collaborator = createUser("Collaborator")
    }

    // ═══════════════════════════════════════════
    // Workspace CRUD
    // ═══════════════════════════════════════════

    @Nested
    inner class CreateWorkspace {

        @Test
        fun `create workspace with title and description`() {
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.save(any<WorkspaceMemberEntity>())).thenAnswer { it.arguments[0] }
            stubWorkspaceCounts()

            val request = CreateWorkspaceRequest(title = "My Collab", description = "Beat project")
            val result = workspaceService.create(owner.id, request)

            assertEquals("My Collab", result.title)
            assertEquals("Beat project", result.description)
            assertEquals("draft", result.status)
            assertEquals(owner.id.toString(), result.ownerId)
            verify(memberRepository).save(argThat { role == WorkspaceMemberRole.OWNER })
        }

        @Test
        fun `create workspace with invited members`() {
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(userRepository.findById(collaborator.id)).thenReturn(Optional.of(collaborator))
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.save(any<WorkspaceMemberEntity>())).thenAnswer { it.arguments[0] }
            stubWorkspaceCounts()

            val request = CreateWorkspaceRequest(
                title = "Collab",
                inviteUserIds = listOf(collaborator.id.toString())
            )
            workspaceService.create(owner.id, request)

            // Owner + collaborator = 2 member saves
            verify(memberRepository, times(2)).save(any<WorkspaceMemberEntity>())
            verify(notificationService).create(
                eq(collaborator.id), eq(owner.id), eq(NotificationType.WORKSPACE_INVITE),
                argThat { contains("invited") }, any(), anyOrNull()
            )
        }

        @Test
        fun `create workspace does not invite owner as collaborator`() {
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.save(any<WorkspaceMemberEntity>())).thenAnswer { it.arguments[0] }
            stubWorkspaceCounts()

            val request = CreateWorkspaceRequest(
                title = "Self Invite",
                inviteUserIds = listOf(owner.id.toString())
            )
            workspaceService.create(owner.id, request)

            // Only 1 save: the OWNER role
            verify(memberRepository, times(1)).save(any<WorkspaceMemberEntity>())
        }

        @Test
        fun `create workspace throws when user not found`() {
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.empty())

            assertThrows(NoSuchElementException::class.java) {
                workspaceService.create(owner.id, CreateWorkspaceRequest(title = "Test"))
            }
        }
    }

    @Nested
    inner class GetWorkspace {

        @Test
        fun `getById returns workspace for member`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            stubWorkspaceCounts(ws.id)

            val result = workspaceService.getById(ws.id, owner.id)

            assertEquals(ws.id.toString(), result.id)
        }

        @Test
        fun `getById throws for non-member`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, collaborator.id)).thenReturn(false)

            assertThrows(SecurityException::class.java) {
                workspaceService.getById(ws.id, collaborator.id)
            }
        }

        @Test
        fun `getById throws when workspace not found`() {
            val randomId = UUID.randomUUID()
            whenever(workspaceRepository.findById(randomId)).thenReturn(Optional.empty())

            assertThrows(NoSuchElementException::class.java) {
                workspaceService.getById(randomId, owner.id)
            }
        }

        @Test
        fun `getMyWorkspaces returns user workspaces`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findByMemberUserId(owner.id)).thenReturn(listOf(ws))
            stubWorkspaceCounts(ws.id)

            val result = workspaceService.getMyWorkspaces(owner.id)

            assertEquals(1, result.size)
        }
    }

    @Nested
    inner class UpdateWorkspace {

        @Test
        fun `update title and status`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }
            stubWorkspaceCounts(ws.id)

            val result = workspaceService.update(ws.id, owner.id,
                UpdateWorkspaceRequest(title = "New Title", status = "in_progress"))

            assertEquals("New Title", result.title)
            assertEquals("in_progress", result.status)
            verify(activityRepository).save(argThat { type == ActivityType.STATUS_CHANGED })
        }
    }

    // ═══════════════════════════════════════════
    // Members
    // ═══════════════════════════════════════════

    @Nested
    inner class Members {

        @Test
        fun `invite member adds collaborator role`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, collaborator.id)).thenReturn(false)
            whenever(userRepository.findById(collaborator.id)).thenReturn(Optional.of(collaborator))
            whenever(memberRepository.save(any<WorkspaceMemberEntity>())).thenAnswer { it.arguments[0] }
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.inviteMember(ws.id, owner.id, collaborator.id)

            assertEquals("collaborator", result.role)
            assertEquals(collaborator.displayName, result.displayName)
        }

        @Test
        fun `invite existing member throws`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, collaborator.id)).thenReturn(true)

            assertThrows(IllegalStateException::class.java) {
                workspaceService.inviteMember(ws.id, owner.id, collaborator.id)
            }
        }

        @Test
        fun `invite sends notification`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, collaborator.id)).thenReturn(false)
            whenever(userRepository.findById(collaborator.id)).thenReturn(Optional.of(collaborator))
            whenever(memberRepository.save(any<WorkspaceMemberEntity>())).thenAnswer { it.arguments[0] }
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            workspaceService.inviteMember(ws.id, owner.id, collaborator.id)

            verify(notificationService).create(
                eq(collaborator.id), eq(owner.id), eq(NotificationType.WORKSPACE_INVITE),
                any(), any(), anyOrNull()
            )
        }

        @Test
        fun `non-member cannot invite`() {
            val ws = createWorkspace(owner)
            val outsider = createUser("Outsider")
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, outsider.id)).thenReturn(false)

            assertThrows(SecurityException::class.java) {
                workspaceService.inviteMember(ws.id, outsider.id, collaborator.id)
            }
        }
    }

    // ═══════════════════════════════════════════
    // Versions
    // ═══════════════════════════════════════════

    @Nested
    inner class Versions {

        @Test
        fun `upload version increments version number`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(versionRepository.findMaxVersionNumber(ws.id)).thenReturn(2)
            whenever(versionRepository.save(any<WorkspaceVersionEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(emptyList())
            whenever(commentRepository.countByVersionId(any())).thenReturn(0)
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.uploadVersion(ws.id, owner.id,
                UploadVersionRequest(audioUrl = "https://audio.com/v3.mp3", notes = "Refined mix", duration = 200))

            assertEquals(3, result.versionNumber)
            assertEquals("https://audio.com/v3.mp3", result.audioUrl)
            assertEquals("Refined mix", result.notes)
        }

        @Test
        fun `first version sets status to in_progress`() {
            val ws = createWorkspace(owner)
            assertEquals(WorkspaceStatus.DRAFT, ws.status)

            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(versionRepository.findMaxVersionNumber(ws.id)).thenReturn(0)
            whenever(versionRepository.save(any<WorkspaceVersionEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(emptyList())
            whenever(commentRepository.countByVersionId(any())).thenReturn(0)
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            workspaceService.uploadVersion(ws.id, owner.id,
                UploadVersionRequest(audioUrl = "https://audio.com/v1.mp3"))

            assertEquals(WorkspaceStatus.IN_PROGRESS, ws.status)
        }

        @Test
        fun `upload version notifies other members`() {
            val ws = createWorkspace(owner)
            val ownerMember = WorkspaceMemberEntity(workspace = ws, user = owner, role = WorkspaceMemberRole.OWNER)
            val collabMember = WorkspaceMemberEntity(workspace = ws, user = collaborator, role = WorkspaceMemberRole.COLLABORATOR)

            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(versionRepository.findMaxVersionNumber(ws.id)).thenReturn(0)
            whenever(versionRepository.save(any<WorkspaceVersionEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(listOf(ownerMember, collabMember))
            whenever(commentRepository.countByVersionId(any())).thenReturn(0)
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            workspaceService.uploadVersion(ws.id, owner.id,
                UploadVersionRequest(audioUrl = "https://audio.com/v1.mp3"))

            // Only collaborator should be notified, not the owner who uploaded
            verify(notificationService).create(
                eq(collaborator.id), eq(owner.id), eq(NotificationType.WORKSPACE_VERSION),
                argThat { contains("v1") }, any(), anyOrNull()
            )
        }
    }

    // ═══════════════════════════════════════════
    // Timestamped Comments
    // ═══════════════════════════════════════════

    @Nested
    inner class Comments {

        @Test
        fun `add comment with timestamp`() {
            val ws = createWorkspace(owner)
            val version = createVersion(ws, owner, 1)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, collaborator.id)).thenReturn(true)
            whenever(versionRepository.findById(version.id)).thenReturn(Optional.of(version))
            whenever(userRepository.findById(collaborator.id)).thenReturn(Optional.of(collaborator))
            whenever(commentRepository.save(any<WorkspaceCommentEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(emptyList())
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.addComment(ws.id, version.id, collaborator.id,
                AddCommentRequest(content = "The beat is too loud here", timestampSeconds = 34.5))

            assertEquals("The beat is too loud here", result.content)
            assertEquals(34.5, result.timestampSeconds)
            assertEquals(collaborator.displayName, result.authorName)
        }

        @Test
        fun `comment on wrong workspace version throws`() {
            val ws1 = createWorkspace(owner)
            val ws2 = createWorkspace(owner)
            val versionOnWs2 = createVersion(ws2, owner, 1)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws1.id, owner.id)).thenReturn(true)
            whenever(versionRepository.findById(versionOnWs2.id)).thenReturn(Optional.of(versionOnWs2))

            assertThrows(IllegalArgumentException::class.java) {
                workspaceService.addComment(ws1.id, versionOnWs2.id, owner.id,
                    AddCommentRequest(content = "Test", timestampSeconds = 10.0))
            }
        }

        @Test
        fun `add reply to existing comment`() {
            val ws = createWorkspace(owner)
            val version = createVersion(ws, owner, 1)
            val parentComment = WorkspaceCommentEntity(
                version = version, author = owner, content = "Original", timestampSeconds = 10.0
            )

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, collaborator.id)).thenReturn(true)
            whenever(versionRepository.findById(version.id)).thenReturn(Optional.of(version))
            whenever(userRepository.findById(collaborator.id)).thenReturn(Optional.of(collaborator))
            whenever(commentRepository.findById(parentComment.id)).thenReturn(Optional.of(parentComment))
            whenever(commentRepository.save(any<WorkspaceCommentEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(emptyList())
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.addComment(ws.id, version.id, collaborator.id,
                AddCommentRequest(
                    content = "Agreed!",
                    timestampSeconds = 10.0,
                    parentId = parentComment.id.toString()
                ))

            assertEquals(parentComment.id.toString(), result.parentId)
        }

        @Test
        fun `comment notification includes formatted timestamp`() {
            val ws = createWorkspace(owner)
            val version = createVersion(ws, owner, 1)
            val ownerMember = WorkspaceMemberEntity(workspace = ws, user = owner, role = WorkspaceMemberRole.OWNER)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, collaborator.id)).thenReturn(true)
            whenever(versionRepository.findById(version.id)).thenReturn(Optional.of(version))
            whenever(userRepository.findById(collaborator.id)).thenReturn(Optional.of(collaborator))
            whenever(commentRepository.save(any<WorkspaceCommentEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(listOf(ownerMember))
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            workspaceService.addComment(ws.id, version.id, collaborator.id,
                AddCommentRequest(content = "Fix this", timestampSeconds = 125.0)) // 2:05

            verify(notificationService).create(
                eq(owner.id), eq(collaborator.id), eq(NotificationType.WORKSPACE_COMMENT),
                argThat { contains("2:05") }, any(), anyOrNull()
            )
        }

        @Test
        fun `getComments returns threaded comments`() {
            val ws = createWorkspace(owner)
            val version = createVersion(ws, owner, 1)
            val parentComment = WorkspaceCommentEntity(
                version = version, author = owner, content = "Parent", timestampSeconds = 10.0
            )
            val reply = WorkspaceCommentEntity(
                version = version, author = collaborator, content = "Reply",
                timestampSeconds = 10.0, parent = parentComment
            )

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(commentRepository.findByVersionIdAndParentIsNullOrderByTimestampSecondsAsc(version.id))
                .thenReturn(listOf(parentComment))
            whenever(commentRepository.findByParentIdOrderByCreatedAtAsc(parentComment.id))
                .thenReturn(listOf(reply))

            val result = workspaceService.getComments(ws.id, version.id, owner.id)

            assertEquals(1, result.size)
            assertEquals("Parent", result[0].content)
            assertEquals(1, result[0].replies.size)
            assertEquals("Reply", result[0].replies[0].content)
        }
    }

    // ═══════════════════════════════════════════
    // Files
    // ═══════════════════════════════════════════

    @Nested
    inner class Files {

        @Test
        fun `upload file with type`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(fileRepository.save(any<WorkspaceFileEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.uploadFile(ws.id, owner.id,
                UploadFileRequest(name = "vocals.wav", url = "https://files.com/vocals.wav",
                    fileType = "STEM", sizeBytes = 15000000))

            assertEquals("vocals.wav", result.name)
            assertEquals("stem", result.fileType)
            assertEquals(15000000, result.sizeBytes)
        }

        @Test
        fun `invalid file type defaults to OTHER`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(fileRepository.save(any<WorkspaceFileEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.uploadFile(ws.id, owner.id,
                UploadFileRequest(name = "notes.txt", url = "https://files.com/notes.txt",
                    fileType = "INVALID"))

            assertEquals("other", result.fileType)
        }

        @Test
        fun `delete file checks workspace ownership`() {
            val ws = createWorkspace(owner)
            val ws2 = createWorkspace(owner)
            val file = WorkspaceFileEntity(
                workspace = ws2, name = "test.wav", url = "url",
                fileType = WorkspaceFileType.STEM, uploadedBy = owner
            )

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(fileRepository.findById(file.id)).thenReturn(Optional.of(file))

            assertThrows(IllegalArgumentException::class.java) {
                workspaceService.deleteFile(ws.id, file.id, owner.id)
            }
        }
    }

    // ═══════════════════════════════════════════
    // Chat
    // ═══════════════════════════════════════════

    @Nested
    inner class Chat {

        @Test
        fun `send chat message`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(chatRepository.save(any<WorkspaceChatEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.sendChatMessage(ws.id, owner.id,
                SendChatMessageRequest(content = "Hey, check v3!"))

            assertEquals("Hey, check v3!", result.content)
            assertEquals(owner.displayName, result.senderName)
        }

        @Test
        fun `get chat messages returns oldest first`() {
            val ws = createWorkspace(owner)
            val msg1 = WorkspaceChatEntity(workspace = ws, sender = owner, content = "First")
            val msg2 = WorkspaceChatEntity(workspace = ws, sender = collaborator, content = "Second")

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(chatRepository.findByWorkspaceIdOrderByCreatedAtDesc(eq(ws.id), any()))
                .thenReturn(PageImpl(listOf(msg2, msg1))) // DESC from DB

            val result = workspaceService.getChatMessages(ws.id, owner.id, 0, 50)

            assertEquals(2, result.size)
            // Reversed → oldest first
            assertEquals("First", result[0].content)
            assertEquals("Second", result[1].content)
        }

        @Test
        fun `non-member cannot send chat message`() {
            val ws = createWorkspace(owner)
            val outsider = createUser("Outsider")
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, outsider.id)).thenReturn(false)

            assertThrows(SecurityException::class.java) {
                workspaceService.sendChatMessage(ws.id, outsider.id,
                    SendChatMessageRequest(content = "Sneaky"))
            }
        }
    }

    // ═══════════════════════════════════════════
    // Enhanced Comments (Range, Resolve)
    // ═══════════════════════════════════════════

    @Nested
    inner class EnhancedComments {

        @Test
        fun `add range comment with end timestamp`() {
            val ws = createWorkspace(owner)
            val version = createVersion(ws, owner, 1)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(versionRepository.findById(version.id)).thenReturn(Optional.of(version))
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(commentRepository.save(any<WorkspaceCommentEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(emptyList())
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.addComment(ws.id, version.id, owner.id,
                AddCommentRequest(content = "This section needs work", timestampSeconds = 30.0, endTimestampSeconds = 45.0))

            assertEquals(30.0, result.timestampSeconds)
            assertEquals(45.0, result.endTimestampSeconds)
        }

        @Test
        fun `resolve and unresolve comment`() {
            val ws = createWorkspace(owner)
            val version = createVersion(ws, owner, 1)
            val comment = WorkspaceCommentEntity(
                version = version, author = collaborator, content = "Fix this", timestampSeconds = 10.0
            )
            assertFalse(comment.resolved)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(commentRepository.findById(comment.id)).thenReturn(Optional.of(comment))
            whenever(commentRepository.save(any<WorkspaceCommentEntity>())).thenAnswer { it.arguments[0] }
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val resolved = workspaceService.resolveComment(ws.id, version.id, comment.id, owner.id, true)
            assertTrue(resolved.resolved)

            val reopened = workspaceService.resolveComment(ws.id, version.id, comment.id, owner.id, false)
            assertFalse(reopened.resolved)
        }
    }

    // ═══════════════════════════════════════════
    // Version Changelog
    // ═══════════════════════════════════════════

    @Nested
    inner class VersionChangelog {

        @Test
        fun `upload version with changelog`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(versionRepository.findMaxVersionNumber(ws.id)).thenReturn(1)
            whenever(versionRepository.save(any<WorkspaceVersionEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(memberRepository.findByWorkspaceId(ws.id)).thenReturn(emptyList())
            whenever(commentRepository.countByVersionId(any())).thenReturn(0)
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.uploadVersion(ws.id, owner.id,
                UploadVersionRequest(
                    audioUrl = "https://audio.com/v2.mp3",
                    notes = "Mix update",
                    changelog = listOf("Fixed bass", "Added reverb"),
                    duration = 180
                ))

            assertEquals(2, result.versionNumber)
            assertEquals(listOf("Fixed bass", "Added reverb"), result.changelog)
        }
    }

    // ═══════════════════════════════════════════
    // Tasks
    // ═══════════════════════════════════════════

    @Nested
    inner class Tasks {

        @Test
        fun `create task`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(taskRepository.save(any<WorkspaceTaskEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.createTask(ws.id, owner.id,
                CreateTaskRequest(title = "Record vocals", description = "Verse 1 and chorus"))

            assertEquals("Record vocals", result.title)
            assertEquals("Verse 1 and chorus", result.description)
            assertEquals("todo", result.status)
        }

        @Test
        fun `create task with assignee notifies`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(userRepository.findById(collaborator.id)).thenReturn(Optional.of(collaborator))
            whenever(taskRepository.save(any<WorkspaceTaskEntity>())).thenAnswer { it.arguments[0] }
            whenever(workspaceRepository.save(any<WorkspaceEntity>())).thenAnswer { it.arguments[0] }
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            workspaceService.createTask(ws.id, owner.id,
                CreateTaskRequest(title = "Fix mix", assignedToId = collaborator.id.toString()))

            verify(notificationService).create(
                eq(collaborator.id), eq(owner.id), eq(NotificationType.WORKSPACE_TASK),
                argThat { contains("Fix mix") }, any(), anyOrNull()
            )
        }

        @Test
        fun `update task status`() {
            val ws = createWorkspace(owner)
            val task = WorkspaceTaskEntity(workspace = ws, title = "Test", createdBy = owner)
            assertEquals(TaskStatus.TODO, task.status)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))
            whenever(taskRepository.save(any<WorkspaceTaskEntity>())).thenAnswer { it.arguments[0] }
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.updateTask(ws.id, task.id, owner.id,
                UpdateTaskRequest(status = "done"))

            assertEquals("done", result.status)
        }

        @Test
        fun `update task on wrong workspace throws`() {
            val ws1 = createWorkspace(owner)
            val ws2 = createWorkspace(owner)
            val task = WorkspaceTaskEntity(workspace = ws2, title = "Test", createdBy = owner)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws1.id, owner.id)).thenReturn(true)
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            assertThrows(IllegalArgumentException::class.java) {
                workspaceService.updateTask(ws1.id, task.id, owner.id,
                    UpdateTaskRequest(status = "done"))
            }
        }

        @Test
        fun `delete task`() {
            val ws = createWorkspace(owner)
            val task = WorkspaceTaskEntity(workspace = ws, title = "Test", createdBy = owner)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(taskRepository.findById(task.id)).thenReturn(Optional.of(task))

            workspaceService.deleteTask(ws.id, task.id, owner.id)

            verify(taskRepository).delete(task)
        }

        @Test
        fun `get tasks returns all tasks for workspace`() {
            val ws = createWorkspace(owner)
            val t1 = WorkspaceTaskEntity(workspace = ws, title = "Task 1", createdBy = owner)
            val t2 = WorkspaceTaskEntity(workspace = ws, title = "Task 2", createdBy = owner, status = TaskStatus.DONE)

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(taskRepository.findByWorkspaceIdOrderByCreatedAtAsc(ws.id)).thenReturn(listOf(t1, t2))

            val result = workspaceService.getTasks(ws.id, owner.id)

            assertEquals(2, result.size)
            assertEquals("Task 1", result[0].title)
            assertEquals("done", result[1].status)
        }
    }

    // ═══════════════════════════════════════════
    // Activities
    // ═══════════════════════════════════════════

    @Nested
    inner class Activities {

        @Test
        fun `get activities returns feed`() {
            val ws = createWorkspace(owner)
            val activity = WorkspaceActivityEntity(
                workspace = ws, user = owner,
                type = ActivityType.VERSION_UPLOADED, message = "uploaded v1"
            )

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(activityRepository.findByWorkspaceIdOrderByCreatedAtDesc(eq(ws.id), any()))
                .thenReturn(PageImpl(listOf(activity)))

            val result = workspaceService.getActivities(ws.id, owner.id, 0, 50)

            assertEquals(1, result.size)
            assertEquals("uploaded v1", result[0].message)
            assertEquals("version_uploaded", result[0].type)
        }
    }

    // ═══════════════════════════════════════════
    // References
    // ═══════════════════════════════════════════

    @Nested
    inner class References {

        @Test
        fun `add reference track`() {
            val ws = createWorkspace(owner)
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(userRepository.findById(owner.id)).thenReturn(Optional.of(owner))
            whenever(referenceRepository.save(any<WorkspaceReferenceEntity>())).thenAnswer { it.arguments[0] }
            whenever(activityRepository.save(any<WorkspaceActivityEntity>())).thenAnswer { it.arguments[0] }

            val result = workspaceService.addReference(ws.id, owner.id,
                AddReferenceRequest(title = "Blinding Lights", artist = "The Weeknd",
                    url = "https://spotify.com/track/123", notes = "This vibe", platform = "SPOTIFY"))

            assertEquals("Blinding Lights", result.title)
            assertEquals("The Weeknd", result.artist)
            assertEquals("spotify", result.platform)
        }

        @Test
        fun `get references returns list`() {
            val ws = createWorkspace(owner)
            val ref = WorkspaceReferenceEntity(
                workspace = ws, title = "Track", addedBy = owner, platform = ReferencePlatform.SPOTIFY
            )

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(referenceRepository.findByWorkspaceIdOrderByCreatedAtDesc(ws.id)).thenReturn(listOf(ref))

            val result = workspaceService.getReferences(ws.id, owner.id)

            assertEquals(1, result.size)
            assertEquals("Track", result[0].title)
        }

        @Test
        fun `delete reference on wrong workspace throws`() {
            val ws1 = createWorkspace(owner)
            val ws2 = createWorkspace(owner)
            val ref = WorkspaceReferenceEntity(
                workspace = ws2, title = "Track", addedBy = owner, platform = ReferencePlatform.OTHER
            )

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws1.id, owner.id)).thenReturn(true)
            whenever(referenceRepository.findById(ref.id)).thenReturn(Optional.of(ref))

            assertThrows(IllegalArgumentException::class.java) {
                workspaceService.deleteReference(ws1.id, ref.id, owner.id)
            }
        }

        @Test
        fun `delete reference`() {
            val ws = createWorkspace(owner)
            val ref = WorkspaceReferenceEntity(
                workspace = ws, title = "Track", addedBy = owner, platform = ReferencePlatform.OTHER
            )

            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, owner.id)).thenReturn(true)
            whenever(referenceRepository.findById(ref.id)).thenReturn(Optional.of(ref))

            workspaceService.deleteReference(ws.id, ref.id, owner.id)

            verify(referenceRepository).delete(ref)
        }

        @Test
        fun `non-member cannot add reference`() {
            val ws = createWorkspace(owner)
            val outsider = createUser("Outsider")
            whenever(workspaceRepository.findById(ws.id)).thenReturn(Optional.of(ws))
            whenever(memberRepository.existsByWorkspaceIdAndUserId(ws.id, outsider.id)).thenReturn(false)

            assertThrows(SecurityException::class.java) {
                workspaceService.addReference(ws.id, outsider.id,
                    AddReferenceRequest(title = "Test"))
            }
        }
    }

    // ═══════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════

    private fun createUser(name: String) = UserEntity(
        id = UUID.randomUUID(),
        displayName = name,
        username = name.lowercase(),
        email = "${name.lowercase()}@test.com",
        passwordHash = "hash",
        roles = mutableSetOf(ArtistRole.SINGER)
    )

    private fun createWorkspace(owner: UserEntity) = WorkspaceEntity(
        title = "Test Workspace",
        owner = owner
    )

    private fun createVersion(ws: WorkspaceEntity, uploader: UserEntity, num: Int) =
        WorkspaceVersionEntity(
            workspace = ws,
            versionNumber = num,
            audioUrl = "https://audio.com/v$num.mp3",
            uploadedBy = uploader,
            duration = 200
        )

    private fun stubWorkspaceCounts(wsId: UUID? = null) {
        whenever(memberRepository.findByWorkspaceId(wsId ?: any())).thenReturn(emptyList())
        whenever(versionRepository.findFirstByWorkspaceIdOrderByVersionNumberDesc(wsId ?: any())).thenReturn(null)
        whenever(versionRepository.countByWorkspaceId(wsId ?: any())).thenReturn(0)
        whenever(fileRepository.countByWorkspaceId(wsId ?: any())).thenReturn(0)
    }
}
