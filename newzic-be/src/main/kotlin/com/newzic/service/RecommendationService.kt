package com.newzic.service

import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Builds a user's taste profile from their interactions:
 * - Preferred genres (explicit, from registration)
 * - Genres from liked songs
 * - Genres from reacted songs
 * - Genres from play history
 * - Country affinities from play history
 * - Genres from followed artists
 */
@Service
class RecommendationService(
    private val followRepository: FollowRepository,
    private val songLikeRepository: SongLikeRepository,
    private val reactionRepository: ReactionRepository,
    private val playEventRepository: PlayEventRepository
) {

    data class TasteProfile(
        val genreScores: Map<String, Double>,
        val countryScores: Map<String, Double>,
        val followedArtistIds: Set<UUID>
    ) {
        val topGenres: Set<String>
            get() = genreScores.entries
                .sortedByDescending { it.value }
                .take(10)
                .map { it.key }
                .toSet()

        val topCountries: Set<String>
            get() = countryScores.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }
                .toSet()
    }

    @Transactional(readOnly = true)
    fun buildTasteProfile(user: UserEntity): TasteProfile {
        val userId = user.id
        val genreScores = mutableMapOf<String, Double>()
        val countryScores = mutableMapOf<String, Double>()

        // ── 1. Explicit preferred genres (strongest signal — user told us) ──
        user.preferredGenres.forEach { genre ->
            genreScores[genre] = (genreScores[genre] ?: 0.0) + 50.0
        }

        // ── 2. User's own country (strong affinity) ──
        user.country?.let { country ->
            countryScores[country] = (countryScores[country] ?: 0.0) + 50.0
        }

        // ── 3. Genres from followed artists (very strong implicit signal) ──
        val follows = followRepository.findByFollowerId(userId)
        val followedArtistIds = follows.map { it.following.id }.toSet()
        follows.forEach { follow ->
            follow.following.genres.forEach { genre ->
                genreScores[genre] = (genreScores[genre] ?: 0.0) + 30.0
            }
            follow.following.country?.let { country ->
                countryScores[country] = (countryScores[country] ?: 0.0) + 20.0
            }
        }

        // ── 4. Genres from liked songs (strong implicit signal) ──
        val likeAffinities = songLikeRepository.findGenreAffinitiesByUserId(userId)
        likeAffinities.forEach { row ->
            val genre = row[0] as String
            val count = (row[1] as Long).toDouble()
            genreScores[genre] = (genreScores[genre] ?: 0.0) + count * 20.0
        }

        // ── 5. Genres from reactions (medium implicit signal) ──
        val reactionAffinities = reactionRepository.findGenreAffinitiesByUserId(userId)
        reactionAffinities.forEach { row ->
            val genre = row[0] as String
            val count = (row[1] as Long).toDouble()
            genreScores[genre] = (genreScores[genre] ?: 0.0) + count * 15.0
        }

        // ── 6. Genres from play history (implicit signal, most data) ──
        val playGenreAffinities = playEventRepository.findGenreAffinitiesByUserId(userId)
        playGenreAffinities.forEach { row ->
            val genre = row[0] as String
            val count = (row[1] as Long).toDouble()
            // Logarithmic scaling — 100 plays worth more than 10 but not 10x more
            genreScores[genre] = (genreScores[genre] ?: 0.0) + kotlin.math.ln(1.0 + count) * 10.0
        }

        // ── 7. Country affinities from play history ──
        val playCountryAffinities = playEventRepository.findCountryAffinitiesByUserId(userId)
        playCountryAffinities.forEach { row ->
            val country = row[0] as String
            val count = (row[1] as Long).toDouble()
            countryScores[country] = (countryScores[country] ?: 0.0) + kotlin.math.ln(1.0 + count) * 8.0
        }

        return TasteProfile(
            genreScores = genreScores.toMap(),
            countryScores = countryScores.toMap(),
            followedArtistIds = followedArtistIds
        )
    }
}
