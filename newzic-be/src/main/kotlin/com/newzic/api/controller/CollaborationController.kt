package com.newzic.api.controller

import com.newzic.service.CollaborationResponse
import com.newzic.service.CollaborationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
}
