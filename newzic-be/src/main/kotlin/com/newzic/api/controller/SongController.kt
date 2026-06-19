package com.newzic.api.controller

import com.newzic.api.dto.CreateSongRequest
import com.newzic.api.dto.SongResponse
import com.newzic.service.SongService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/songs")
class SongController(private val songService: SongService) {

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<SongResponse> {
        return ResponseEntity.ok(songService.getById(id))
    }

    @GetMapping("/trending")
    fun getTrending(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<SongResponse>> {
        return ResponseEntity.ok(songService.getTrending(PageRequest.of(page, size)))
    }

    @GetMapping("/new-releases")
    fun getNewReleases(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<SongResponse>> {
        return ResponseEntity.ok(songService.getNewReleases(PageRequest.of(page, size)))
    }

    @GetMapping("/artist/{artistId}")
    fun getByArtist(
        @PathVariable artistId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<Page<SongResponse>> {
        return ResponseEntity.ok(songService.getByArtist(artistId, PageRequest.of(page, size)))
    }

    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<SongResponse>> {
        return ResponseEntity.ok(songService.search(q, PageRequest.of(page, size)))
    }

    @GetMapping("/recommended")
    fun getRecommended(
        auth: Authentication,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<SongResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(songService.getRecommended(userId, size))
    }

    @PostMapping
    fun create(
        auth: Authentication,
        @Valid @RequestBody request: CreateSongRequest
    ): ResponseEntity<SongResponse> {
        val userId = auth.principal as UUID
        return ResponseEntity.status(HttpStatus.CREATED).body(songService.create(userId, request))
    }

    @PostMapping("/{id}/react")
    fun react(
        auth: Authentication,
        @PathVariable id: UUID,
        @RequestParam type: String
    ): ResponseEntity<Map<String, Boolean>> {
        val userId = auth.principal as UUID
        val added = songService.react(userId, id, type)
        return ResponseEntity.ok(mapOf("added" to added))
    }

    @PostMapping("/{id}/play")
    fun recordPlay(
        @PathVariable id: UUID,
        auth: Authentication?
    ): ResponseEntity<Void> {
        val userId = auth?.principal as? UUID
        songService.recordPlay(id, userId)
        return ResponseEntity.noContent().build()
    }
}
