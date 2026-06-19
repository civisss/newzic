package com.newzic.service

import com.newzic.api.dto.ArtistStatsResponse
import com.newzic.api.dto.CityPlaysDto
import com.newzic.api.dto.WeeklyStatsDto
import com.newzic.domain.repository.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class StatsService(
    private val userRepository: UserRepository,
    private val songRepository: SongRepository,
    private val reactionRepository: ReactionRepository,
    private val playEventRepository: PlayEventRepository,
    private val followRepository: FollowRepository
) {

    fun getArtistStats(artistId: UUID): ArtistStatsResponse {
        val user = userRepository.findById(artistId)
            .orElseThrow { NoSuchElementException("User not found") }

        val totalSongs = songRepository.countByArtistId(artistId)
        val totalFollowers = followRepository.countByFollowingId(artistId)
        val totalPlays = user.totalPlays

        val now = LocalDateTime.now()
        val playsThisWeek = playEventRepository.countByArtistIdSince(artistId, now.minusDays(7))
        val playsLastWeek = playEventRepository.countByArtistIdSince(artistId, now.minusDays(14)) - playsThisWeek

        val growthPercent = if (playsLastWeek > 0) {
            ((playsThisWeek - playsLastWeek).toDouble() / playsLastWeek * 100)
        } else 0.0

        val playsTrend = when {
            playsThisWeek > playsLastWeek -> "up"
            playsThisWeek < playsLastWeek -> "down"
            else -> "stable"
        }

        val topCities = playEventRepository.findTopCitiesByArtist(artistId)
            .take(5)
            .map { CityPlaysDto(city = it[0] as String, plays = it[1] as Long) }

        return ArtistStatsResponse(
            totalPlays = totalPlays,
            totalFollowers = totalFollowers,
            totalReactions = 0, // computed later if needed
            totalSongs = totalSongs,
            weeklyData = emptyList(), // TODO: implement weekly aggregation
            topCities = topCities,
            growthPercent = growthPercent,
            playsTrend = playsTrend,
            followersTrend = "stable"
        )
    }
}
