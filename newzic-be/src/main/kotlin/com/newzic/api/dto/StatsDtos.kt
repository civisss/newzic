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

// ── Premium Advanced Analytics ──

data class AdvancedAnalyticsResponse(
    val audienceByCountry: List<PercentageItem>,
    val audienceByGender: List<PercentageItem>,
    val audienceByAge: List<PercentageItem>,
    val audienceInterests: List<PercentageItem>,
    val songPerformance: List<SongPerformanceDto>,
    val growth: GrowthDto
)

data class PercentageItem(
    val label: String,
    val percent: Double
)

data class SongPerformanceDto(
    val songId: String,
    val title: String,
    val cover: String?,
    val plays: Long,
    val likes: Long,
    val shares: Long,
    val saves: Long,
    val newFollowers: Long
)

data class GrowthDto(
    val daily: GrowthPoint,
    val weekly: GrowthPoint,
    val monthly: GrowthPoint
)

data class GrowthPoint(
    val plays: Long,
    val followers: Long,
    val trend: String    // "up" | "down" | "stable"
)
