package com.newzic.api.dto

// ── Premium ──

data class PremiumStatusResponse(
    val isPremium: Boolean,
    val premiumSince: String?,
    val customUrl: String?,
    val verified: Boolean
)

data class ActivatePremiumRequest(
    val paymentToken: String? = null
)

data class SetCustomUrlRequest(
    val customUrl: String
)

data class PremiumLimitsResponse(
    val maxSongs: Int?,           // null = unlimited
    val currentSongs: Long,
    val maxWorkspaces: Int?,
    val currentWorkspaces: Long,
    val maxCollaboratorsPerWorkspace: Int?,
    val maxCommentsPerSong: Int?
)

// ── Donations ──

data class CreateDonationRequest(
    val artistId: String,
    val amountCents: Int,
    val message: String? = null
)

data class DonationResponse(
    val id: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromUserAvatar: String?,
    val toArtistId: String,
    val toArtistName: String,
    val amountCents: Int,
    val artistCents: Int,
    val platformCents: Int,
    val message: String?,
    val createdAt: String
)

data class DonationDashboardResponse(
    val totalReceivedCents: Long,
    val totalSupporters: Long,
    val recentDonations: List<DonationResponse>,
    val topSupporters: List<TopSupporterResponse>
)

data class TopSupporterResponse(
    val userId: String,
    val displayName: String,
    val avatar: String?,
    val totalCents: Long
)
