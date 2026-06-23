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
import com.newzic.domain.repository.SongLikeRepository
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import com.newzic.domain.entity.SongLikeEntity
import com.newzic.service.SongMapper

@RestController
@RequestMapping("/api/songs")
class SongController(
    private val songService: SongService,
    private val songLikeRepository: SongLikeRepository,
    private val songRepository: SongRepository,
    private val userRepository: UserRepository,
    private val songMapper: SongMapper
) {

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

    @PostMapping("/{id}/like")
    @org.springframework.transaction.annotation.Transactional
    fun toggleLike(
        auth: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<Map<String, Boolean>> {
        val userId = auth.principal as UUID
        val existing = songLikeRepository.findByUserIdAndSongId(userId, id)
        if (existing != null) {
            songLikeRepository.delete(existing)
            val song = songRepository.findById(id).orElse(null)
            song?.let { it.likes = maxOf(0, it.likes - 1); songRepository.save(it) }
            return ResponseEntity.ok(mapOf("liked" to false))
        } else {
            val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
            val song = songRepository.findById(id).orElseThrow { NoSuchElementException("Song not found") }
            songLikeRepository.save(SongLikeEntity(user = user, song = song))
            song.likes += 1
            songRepository.save(song)
            return ResponseEntity.ok(mapOf("liked" to true))
        }
    }

    @GetMapping("/{id}/liked")
    fun isLiked(
        auth: Authentication,
        @PathVariable id: UUID
    ): ResponseEntity<Map<String, Boolean>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(mapOf("liked" to songLikeRepository.existsByUserIdAndSongId(userId, id)))
    }

    @GetMapping("/liked")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    fun getLikedSongs(auth: Authentication): ResponseEntity<List<SongResponse>> {
        val userId = auth.principal as UUID
        val likes = songLikeRepository.findByUserIdOrderByCreatedAtDesc(userId)
        val songs = likes.map { songMapper.toListResponse(it.song) }
        return ResponseEntity.ok(songs)
    }
}
