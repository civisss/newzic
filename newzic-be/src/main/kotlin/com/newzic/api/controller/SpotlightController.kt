package com.newzic.api.controller

import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/spotlight")
class SpotlightController(
    private val userRepository: UserRepository,
    private val songRepository: SongRepository
) {

    @Transactional(readOnly = true)
    @GetMapping("/weekly-top")
    fun getWeeklyTop(): ResponseEntity<List<Map<String, Any?>>> {
        val top1 = PageRequest.of(0, 1)
        val slides = mutableListOf<Map<String, Any?>>()

        // 1. Top artist by plays
        val topPlays = userRepository.findTrending(top1).content.firstOrNull()
        if (topPlays != null) {
            slides.add(artistSlide(topPlays, "top_plays", "🔥 Most Played Artist", "Top artist by total plays this week"))
        }

        // 2. Top artist by reactions (optimized DB query)
        val topReactionRow = songRepository.findTopArtistByReactions(top1).content.firstOrNull()
        val topReactions = if (topReactionRow != null) {
            val artistId = topReactionRow[0] as UUID
            userRepository.findById(artistId).orElse(null)
        } else null
        if (topReactions != null) {
            val totalReactions = (topReactionRow!![1] as Long)
            slides.add(artistSlide(topReactions, "top_reactions", "💎 Most Loved Artist", "Top artist by total reactions this week", totalReactions))
        }

        // 3. Top artist by followers (deduplicated from previous slides)
        val followerPage = userRepository.findTrending(PageRequest.of(0, 3))
        val topFollower = followerPage.content.firstOrNull { it.id != topPlays?.id && it.id != topReactions?.id }
            ?: followerPage.content.firstOrNull { it.id != topPlays?.id }
            ?: followerPage.content.firstOrNull()
        if (topFollower != null) {
            slides.add(artistSlide(topFollower, "top_followers", "⭐ Most Followed Artist", "Top artist by follower count this week"))
        }

        if (slides.isEmpty()) return ResponseEntity.noContent().build()
        return ResponseEntity.ok(slides)
    }

    private fun artistSlide(
        artist: UserEntity,
        category: String,
        categoryLabel: String,
        description: String,
        extraStat: Long? = null
    ): Map<String, Any?> {
        val totalSongs = songRepository.countByArtistId(artist.id)
        return mapOf(
            "artistId" to artist.id.toString(),
            "artistName" to artist.displayName,
            "artistUsername" to artist.username,
            "artistAvatar" to artist.avatar,
            "artistCover" to artist.cover,
            "artistFollowers" to artist.followers,
            "artistTotalPlays" to artist.totalPlays,
            "artistGenres" to artist.genres.toList(),
            "artistTotalSongs" to totalSongs,
            "artistVerified" to artist.verified,
            "artistLocation" to artist.location,
            "category" to category,
            "categoryLabel" to categoryLabel,
            "description" to description,
            "extraStat" to extraStat
        )
    }
}
