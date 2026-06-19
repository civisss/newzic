package com.newzic.api.controller

import com.newzic.api.dto.CreateFeedPostRequest
import com.newzic.api.dto.FeedPostResponse
import com.newzic.service.FeedService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/feed")
class FeedController(private val feedService: FeedService) {

    @GetMapping
    fun getFeed(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FeedPostResponse>> {
        return ResponseEntity.ok(feedService.getFeed(PageRequest.of(page, size)))
    }

    @GetMapping("/user/{userId}")
    fun getByUser(
        @PathVariable userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FeedPostResponse>> {
        return ResponseEntity.ok(feedService.getByAuthor(userId, PageRequest.of(page, size)))
    }

    @PostMapping
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateFeedPostRequest
    ): ResponseEntity<FeedPostResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED).body(feedService.create(userId, request))
    }
}
