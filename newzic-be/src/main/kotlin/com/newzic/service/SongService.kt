package com.newzic.service

import com.newzic.api.dto.CreateSongRequest
import com.newzic.api.dto.SongResponse
import com.newzic.domain.entity.NotificationType
import com.newzic.domain.entity.ReactionEntity
import com.newzic.domain.entity.ReactionType
import com.newzic.domain.entity.SongEntity
import com.newzic.domain.repository.AlbumRepository
import com.newzic.domain.repository.ReactionRepository
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class SongService(
    private val songRepository: SongRepository,
    private val userRepository: UserRepository,
    private val albumRepository: AlbumRepository,
    private val reactionRepository: ReactionRepository,
    private val songMapper: SongMapper,
    private val notificationService: NotificationService,
    private val recommendationService: RecommendationService
) {

    fun getById(id: UUID): SongResponse {
        val song = songRepository.findById(id)
            .orElseThrow { NoSuchElementException("Song not found") }
        return songMapper.toResponse(song)
    }

    fun getByArtist(artistId: UUID, pageable: Pageable): Page<SongResponse> {
        return songRepository.findByArtistId(artistId, pageable).map { songMapper.toResponse(it) }
    }

    fun getTrending(pageable: Pageable): Page<SongResponse> {
        return songRepository.findTrending(pageable).map { songMapper.toResponse(it) }
    }

    fun getNewReleases(pageable: Pageable): Page<SongResponse> {
        return songRepository.findNewReleases(pageable).map { songMapper.toResponse(it) }
    }

    fun search(query: String, pageable: Pageable): Page<SongResponse> {
        return songRepository.search(query, pageable).map { songMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getRecommended(userId: UUID, limit: Int = 10): List<SongResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val profile = recommendationService.buildTasteProfile(user)
        val topGenres = profile.topGenres
        val userCountry = user.country

        // ── Fetch candidates from DB (filtered, not findAll) ──
        val candidateLimit = limit * 5 // fetch more than needed for scoring
        val candidates: List<SongEntity> = if (topGenres.isNotEmpty() && userCountry != null) {
            songRepository.findRecommendationCandidates(
                userId, topGenres, userCountry, PageRequest.of(0, candidateLimit)
            ).content
        } else if (topGenres.isNotEmpty()) {
            songRepository.findByGenresExcludingUser(
                userId, topGenres, PageRequest.of(0, candidateLimit)
            ).content
        } else if (userCountry != null) {
            songRepository.findByArtistCountryExcludingUser(
                userId, userCountry, PageRequest.of(0, candidateLimit)
            ).content
        } else {
            songRepository.findTrending(PageRequest.of(0, candidateLimit)).content
                .filter { it.artist.id != userId }
        }

        // ── Score each candidate using the full taste profile ──
        data class ScoredSong(val entity: SongEntity, val score: Double)

        val scored = candidates.map { song ->
            var score = 0.0

            // Genre affinity from taste profile (weighted by interaction history)
            if (song.genre != null) {
                score += profile.genreScores[song.genre] ?: 0.0
            }

            // Tag overlap with top genres
            if (topGenres.isNotEmpty() && song.tags.isNotEmpty()) {
                val tagOverlap = topGenres.intersect(song.tags).size
                score += tagOverlap * 10.0
            }

            // Country affinity from taste profile
            song.artist.country?.let { artistCountry ->
                score += profile.countryScores[artistCountry] ?: 0.0
            }

            // Boost songs from followed artists
            if (song.artist.id in profile.followedArtistIds) {
                score += 25.0
            }

            // Popularity signal (capped)
            score += (song.plays / 100000.0).coerceAtMost(10.0)

            // Freshness: recent songs get a boost
            val daysSinceRelease = java.time.temporal.ChronoUnit.DAYS.between(
                song.releaseDate,
                java.time.LocalDate.now()
            )
            if (daysSinceRelease < 30) score += 8.0
            else if (daysSinceRelease < 90) score += 4.0

            ScoredSong(song, score)
        }

        return scored
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(limit)
            .map { songMapper.toResponse(it.entity) }
    }

    @Transactional
    fun create(userId: UUID, request: CreateSongRequest): SongResponse {
        val artist = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val album = request.albumId?.let {
            albumRepository.findById(UUID.fromString(it)).orElse(null)
        }

        val song = SongEntity(
            title = request.title,
            artist = artist,
            album = album,
            cover = request.cover,
            genre = request.genre,
            tags = request.tags.toMutableSet(),
            isExplicit = request.isExplicit,
            audioUrl = request.audioData
        )

        return songMapper.toResponse(songRepository.save(song))
    }

    @Transactional
    fun react(userId: UUID, songId: UUID, type: String): Boolean {
        val reactionType = try { ReactionType.valueOf(type.uppercase()) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid reaction type: $type")
        }

        val existing = reactionRepository.findByUserIdAndSongIdAndType(userId, songId, reactionType)
        val song = songRepository.findById(songId)
            .orElseThrow { NoSuchElementException("Song not found") }

        if (existing != null) {
            reactionRepository.delete(existing)
            when (reactionType) {
                ReactionType.FIRE -> song.reactionsFire = maxOf(0, song.reactionsFire - 1)
                ReactionType.GEM -> song.reactionsGem = maxOf(0, song.reactionsGem - 1)
                ReactionType.ONPOINT -> song.reactionsOnpoint = maxOf(0, song.reactionsOnpoint - 1)
                ReactionType.STAR -> song.reactionsStar = maxOf(0, song.reactionsStar - 1)
            }
            songRepository.save(song)
            return false // removed
        } else {
            val user = userRepository.findById(userId)
                .orElseThrow { NoSuchElementException("User not found") }
            reactionRepository.save(ReactionEntity(user = user, song = song, type = reactionType))
            when (reactionType) {
                ReactionType.FIRE -> song.reactionsFire += 1
                ReactionType.GEM -> song.reactionsGem += 1
                ReactionType.ONPOINT -> song.reactionsOnpoint += 1
                ReactionType.STAR -> song.reactionsStar += 1
            }
            songRepository.save(song)

            val reactionEmoji = when (reactionType) {
                ReactionType.FIRE -> "🔥"
                ReactionType.GEM -> "💎"
                ReactionType.ONPOINT -> "🎯"
                ReactionType.STAR -> "🌟"
            }
            notificationService.create(
                recipientId = song.artist.id,
                fromUserId = userId,
                type = NotificationType.REACTION,
                message = "${user.displayName} reacted $reactionEmoji to \"${song.title}\"",
                link = "/artist/${song.artist.id}",
                songId = songId
            )

            return true // added
        }
    }

    @Transactional
    fun recordPlay(songId: UUID, userId: UUID?) {
        val song = songRepository.findById(songId)
            .orElseThrow { NoSuchElementException("Song not found") }

        // Don't count plays from the song's own artist
        if (userId != null && userId == song.artist.id) return

        song.plays += 1
        song.updatedAt = LocalDateTime.now()
        songRepository.save(song)

        // Update artist total plays
        song.artist.totalPlays += 1
        userRepository.save(song.artist)
    }
}
