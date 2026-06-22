package com.newzic.api.controller

import com.newzic.api.dto.WorkspaceResponse
import com.newzic.service.CollaborationResponse
import com.newzic.service.CollaborationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/collaborations")
class CollaborationController(private val collaborationService: CollaborationService) {

    @GetMapping
    fun getAll(
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<CollaborationResponse>> {
        val pageable = PageRequest.of(page, size)
        val result = if (status != null) {
            collaborationService.getByStatus(status, pageable)
        } else {
            collaborationService.getAll(pageable)
        }
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/respond")
    fun respondToCollab(
        auth: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<WorkspaceResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(collaborationService.respondToCollab(id, userId))
    }
}
