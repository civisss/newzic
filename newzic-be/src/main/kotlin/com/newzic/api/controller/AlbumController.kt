package com.newzic.api.controller

import com.newzic.api.dto.AlbumResponse
import com.newzic.domain.repository.AlbumRepository
import com.newzic.domain.repository.SongRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/albums")
class AlbumController(
    private val albumRepository: AlbumRepository,
    private val songRepository: SongRepository
) {

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<AlbumResponse> {
        val album = albumRepository.findById(id).orElseThrow { NoSuchElementException("Album not found") }
        val songs = songRepository.findByAlbumId(album.id)
        return ResponseEntity.ok(toResponse(album, songs))
    }

    @GetMapping("/artist/{artistId}")
    fun getByArtist(@PathVariable artistId: UUID): ResponseEntity<List<AlbumResponse>> {
        val albums = albumRepository.findByArtistId(artistId)
        return ResponseEntity.ok(albums.map { a ->
            val songs = songRepository.findByAlbumId(a.id)
            toResponse(a, songs)
        })
    }

    private fun toResponse(a: com.newzic.domain.entity.AlbumEntity, songs: List<com.newzic.domain.entity.SongEntity>): AlbumResponse {
        return AlbumResponse(
            id = a.id.toString(),
            title = a.title,
            artistId = a.artist.id.toString(),
            artistName = a.artist.displayName,
            cover = a.cover,
            type = a.type.name,
            releaseDate = a.releaseDate.toString(),
            genre = a.genre,
            trackIds = songs.map { it.id.toString() },
            totalPlays = songs.sumOf { it.plays },
            description = a.description
        )
    }
}
