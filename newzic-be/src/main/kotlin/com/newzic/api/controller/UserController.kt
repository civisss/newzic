package com.newzic.api.controller

import com.newzic.api.dto.UpdateProfileRequest
import com.newzic.api.dto.UserResponse
import com.newzic.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class UserController(private val userService: UserService) {

    @GetMapping("/users/me")
    fun getCurrentUser(auth: Authentication): ResponseEntity<UserResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(userService.getById(userId))
    }

    @PatchMapping("/users/me")
    fun updateProfile(
        auth: Authentication,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(userService.updateProfile(userId, request))
    }

    @GetMapping("/artists/{id}")
    fun getArtist(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getById(id))
    }

    @GetMapping("/artists/trending")
    fun getTrendingArtists(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.getTrending(PageRequest.of(page, size)))
    }

    @GetMapping("/artists/producers")
    fun getProducers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.getProducers(PageRequest.of(page, size)))
    }

    @GetMapping("/artists/community-picks")
    fun getCommunityPicks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.getCommunityPicks(PageRequest.of(page, size)))
    }

    @GetMapping("/artists/search")
    fun searchArtists(
        @RequestParam q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<UserResponse>> {
        return ResponseEntity.ok(userService.search(q, PageRequest.of(page, size)))
    }

    @PostMapping("/artists/{id}/follow")
    fun follow(auth: Authentication, @PathVariable id: UUID): ResponseEntity<Map<String, Boolean>> {
        val userId = auth.principal as UUID
        val followed = userService.follow(userId, id)
        return ResponseEntity.ok(mapOf("following" to followed))
    }

    @GetMapping("/artists/{id}/following")
    fun isFollowing(auth: Authentication, @PathVariable id: UUID): ResponseEntity<Map<String, Boolean>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(mapOf("following" to userService.isFollowing(userId, id)))
    }

    @GetMapping("/artists/recommended")
    fun getRecommended(
        auth: Authentication,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<UserResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(userService.getRecommended(userId, size))
    }
}
