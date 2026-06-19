package com.newzic.api.controller

import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.SpotlightRepository
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/spotlight")
class SpotlightController(
    private val spotlightRepository: SpotlightRepository,
    private val songRepository: SongRepository
) {

    @Transactional(readOnly = true)
    @GetMapping("/current")
    fun getCurrent(): ResponseEntity<Map<String, Any?>> {
        val spotlight = spotlightRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
            ?: return ResponseEntity.noContent().build()

        val artist = spotlight.artist
        val totalSongs = songRepository.countByArtistId(artist.id)

        val response = mapOf(
            "id" to spotlight.id.toString(),
            "artistId" to artist.id.toString(),
            "artistName" to artist.displayName,
            "artistAvatar" to artist.avatar,
            "artistCover" to artist.cover,
            "quote" to spotlight.quote,
            "featuredSongId" to spotlight.featuredSong?.id?.toString(),
            "featuredSongTitle" to spotlight.featuredSong?.title,
            "featuredSongCover" to spotlight.featuredSong?.cover,
            "editorNote" to spotlight.editorNote,
            "weekLabel" to spotlight.weekLabel,
            "artistFollowers" to artist.followers,
            "artistTotalPlays" to artist.totalPlays,
            "artistGenres" to artist.genres.toList(),
            "artistTotalSongs" to totalSongs,
            "artistVerified" to artist.verified,
            "artistLocation" to artist.location
        )

        return ResponseEntity.ok(response)
    }
}
