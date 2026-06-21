package com.newzic.api.dto

data class ArtistStatsResponse(
    val totalPlays: Long,
    val totalFollowers: Long,
    val totalFollowing: Long = 0,
    val totalReactions: Long,
    val totalSongs: Long,
    val weeklyData: List<WeeklyStatsDto>,
    val topCities: List<CityPlaysDto>,
    val growthPercent: Double,
    val playsTrend: String,
    val followersTrend: String
)

data class WeeklyStatsDto(
    val label: String,
    val plays: Long,
    val followers: Long,
    val reactions: Long
)

data class CityPlaysDto(
    val city: String,
    val plays: Long
)
