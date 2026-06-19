package com.newzic.service

import com.newzic.api.dto.CreateSongRequest
import com.newzic.api.dto.SongResponse
import com.newzic.domain.entity.ReactionEntity
import com.newzic.domain.entity.ReactionType
import com.newzic.domain.entity.SongEntity
import com.newzic.domain.repository.AlbumRepository
import com.newzic.domain.repository.ReactionRepository
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.Page
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
    private val songMapper: SongMapper
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

        val preferredGenres = user.preferredGenres
        val userCountry = user.country

        // Fetch all songs (in a real app we'd use a smarter query, but for demo data this is fine)
        val allSongs = songRepository.findAll()
            .filter { it.artist.id != userId }

        data class ScoredSong(val entity: SongEntity, val score: Double)

        val scored = allSongs.map { song ->
            var score = 0.0

            // Genre match: song genre matches user's preferred genres
            if (preferredGenres.isNotEmpty() && song.genre != null) {
                if (song.genre in preferredGenres) {
                    score += 40.0
                }
            }

            // Tag overlap with preferred genres (tags can be sub-genres)
            if (preferredGenres.isNotEmpty() && song.tags.isNotEmpty()) {
                val tagOverlap = preferredGenres.intersect(song.tags).size
                score += tagOverlap * 10.0
            }

            // Same country as artist
            if (userCountry != null && song.artist.country == userCountry) {
                score += 20.0
            }

            // Popularity signal
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
            return true // added
        }
    }

    @Transactional
    fun recordPlay(songId: UUID, userId: UUID?) {
        val song = songRepository.findById(songId)
            .orElseThrow { NoSuchElementException("Song not found") }
        song.plays += 1
        song.updatedAt = LocalDateTime.now()
        songRepository.save(song)

        // Update artist total plays
        song.artist.totalPlays += 1
        userRepository.save(song.artist)
    }
}
