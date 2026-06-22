package com.newzic.api.controller

import com.newzic.api.dto.*
import com.newzic.service.WorkspaceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/workspaces")
class WorkspaceController(private val workspaceService: WorkspaceService) {

    // ── Workspace CRUD ──

    @PostMapping
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateWorkspaceRequest
    ): ResponseEntity<WorkspaceResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.create(userId, request))
    }

    @GetMapping
    fun getMyWorkspaces(
        auth: Authentication,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<List<WorkspaceResponse>> {
        val userId = auth.principal as UUID
        val workspaces = if (status != null) {
            workspaceService.getMyWorkspacesByStatus(userId, status)
        } else {
            workspaceService.getMyWorkspaces(userId)
        }
        return ResponseEntity.ok(workspaces)
    }

    @GetMapping("/{id}")
    fun getById(auth: Authentication, @PathVariable id: UUID): ResponseEntity<WorkspaceResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(workspaceService.getById(id, userId))
    }

    @PatchMapping("/{id}")
    fun update(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateWorkspaceRequest
    ): ResponseEntity<WorkspaceResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(workspaceService.update(id, userId, request))
    }

    // ── Members ──

    @PostMapping("/{id}/members")
    fun inviteMember(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: InviteMemberRequest
    ): ResponseEntity<WorkspaceMemberResponse> {
        val userId = auth.principal as UUID
        val inviteeId = UUID.fromString(request.userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.inviteMember(id, userId, inviteeId))
    }

    @GetMapping("/{id}/members")
    fun getMembers(auth: Authentication, @PathVariable id: UUID): ResponseEntity<List<WorkspaceMemberResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(workspaceService.getMembers(id, userId))
    }

    // ── Versions ──

    @PostMapping("/{id}/versions")
    fun uploadVersion(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UploadVersionRequest
    ): ResponseEntity<WorkspaceVersionResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.uploadVersion(id, userId, request))
    }

    @GetMapping("/{id}/versions")
    fun getVersions(auth: Authentication, @PathVariable id: UUID): ResponseEntity<List<WorkspaceVersionResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(workspaceService.getVersions(id, userId))
    }

    // ── Comments ──

    @PostMapping("/{id}/versions/{versionId}/comments")
    fun addComment(
        auth: Authentication,
        @PathVariable id: UUID,
        @PathVariable versionId: UUID,
        @Valid @RequestBody request: AddCommentRequest
    ): ResponseEntity<WorkspaceCommentResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.addComment(id, versionId, userId, request))
    }

    @GetMapping("/{id}/versions/{versionId}/comments")
    fun getComments(
        auth: Authentication,
        @PathVariable id: UUID,
        @PathVariable versionId: UUID
    ): ResponseEntity<List<WorkspaceCommentResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(workspaceService.getComments(id, versionId, userId))
    }

    // ── Files ──

    @PostMapping("/{id}/files")
    fun uploadFile(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UploadFileRequest
    ): ResponseEntity<WorkspaceFileResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.uploadFile(id, userId, request))
    }

    @GetMapping("/{id}/files")
    fun getFiles(auth: Authentication, @PathVariable id: UUID): ResponseEntity<List<WorkspaceFileResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(workspaceService.getFiles(id, userId))
    }

    @DeleteMapping("/{id}/files/{fileId}")
    fun deleteFile(
        auth: Authentication,
        @PathVariable id: UUID,
        @PathVariable fileId: UUID
    ): ResponseEntity<Void> {
        val userId = auth.principal as UUID
        workspaceService.deleteFile(id, fileId, userId)
        return ResponseEntity.noContent().build()
    }

    // ── Chat ──

    @PostMapping("/{id}/chat")
    fun sendChatMessage(
        auth: Authentication,
        @PathVariable id: UUID,
        @Valid @RequestBody request: SendChatMessageRequest
    ): ResponseEntity<WorkspaceChatMessageResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.sendChatMessage(id, userId, request))
    }

    @GetMapping("/{id}/chat")
    fun getChatMessages(
        auth: Authentication,
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<List<WorkspaceChatMessageResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(workspaceService.getChatMessages(id, userId, page, size))
    }
}
