package com.newzic.api.controller

import com.newzic.api.dto.ArtistStatsResponse
import com.newzic.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/stats")
class StatsController(private val statsService: StatsService) {

    @GetMapping("/me")
    fun getMyStats(auth: Authentication): ResponseEntity<ArtistStatsResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(statsService.getArtistStats(userId))
    }

    @GetMapping("/artist/{id}")
    fun getArtistStats(@PathVariable id: UUID): ResponseEntity<ArtistStatsResponse> {
        return ResponseEntity.ok(statsService.getArtistStats(id))
    }
}
