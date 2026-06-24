package com.newzic.api.controller

import com.newzic.api.dto.*
import com.newzic.service.JournalService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/journal")
class JournalController(
    private val journalService: JournalService
) {

    // ── Posts ──

    @PostMapping
    fun createPost(auth: Authentication, @RequestBody request: CreateJournalPostRequest): JournalPostResponse {
        val userId = auth.principal as UUID
        return journalService.createPost(userId, request)
    }

    @PutMapping("/{postId}")
    fun updatePost(auth: Authentication, @PathVariable postId: UUID, @RequestBody request: UpdateJournalPostRequest): JournalPostResponse {
        val userId = auth.principal as UUID
        return journalService.updatePost(userId, postId, request)
    }

    @DeleteMapping("/{postId}")
    fun deletePost(auth: Authentication, @PathVariable postId: UUID) {
        val userId = auth.principal as UUID
        journalService.deletePost(userId, postId)
    }

    @GetMapping("/{postId}")
    fun getPost(auth: Authentication?, @PathVariable postId: UUID): JournalPostResponse {
        val userId = auth?.let { it.principal as UUID }
        return journalService.getPost(postId, userId)
    }

    @GetMapping("/user/{authorId}")
    fun getPostsByAuthor(
        auth: Authentication?,
        @PathVariable authorId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<JournalPostResponse> {
        val userId = auth?.let { it.principal as UUID }
        return journalService.getPostsByAuthor(authorId, userId, PageRequest.of(page, size))
    }

    // ── Feed ──

    @GetMapping("/feed")
    fun getFeed(
        auth: Authentication?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<JournalPostResponse> {
        val userId = auth?.let { it.principal as UUID }
        return journalService.getFeed(userId, PageRequest.of(page, size))
    }

    // ── Search ──

    @GetMapping("/search")
    fun search(
        auth: Authentication?,
        @RequestParam q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<JournalPostResponse> {
        val userId = auth?.let { it.principal as UUID }
        return journalService.search(q, userId, PageRequest.of(page, size))
    }

    // ── Reactions ──

    @PostMapping("/{postId}/reactions")
    fun toggleReaction(auth: Authentication, @PathVariable postId: UUID, @RequestBody request: ToggleReactionRequest): ReactionSummaryResponse {
        val userId = auth.principal as UUID
        return journalService.toggleReaction(userId, postId, request.type)
    }

    // ── Comments ──

    @GetMapping("/{postId}/comments")
    fun getComments(
        @PathVariable postId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): Page<JournalCommentResponse> {
        return journalService.getComments(postId, PageRequest.of(page, size))
    }

    @PostMapping("/{postId}/comments")
    fun addComment(auth: Authentication, @PathVariable postId: UUID, @RequestBody request: CreateJournalCommentRequest): JournalCommentResponse {
        val userId = auth.principal as UUID
        return journalService.addComment(userId, postId, request)
    }

    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(auth: Authentication, @PathVariable commentId: UUID) {
        val userId = auth.principal as UUID
        journalService.deleteComment(userId, commentId)
    }
}
