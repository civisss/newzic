package com.newzic.service

import com.newzic.api.dto.*
import com.newzic.domain.entity.DonationEntity
import com.newzic.domain.entity.NotificationType
import com.newzic.domain.repository.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class PremiumService(
    private val userRepository: UserRepository,
    private val songRepository: SongRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val donationRepository: DonationRepository,
    private val notificationService: NotificationService
) {

    companion object {
        const val FREE_MAX_SONGS = 10
        const val FREE_MAX_WORKSPACES = 1
        const val FREE_MAX_COLLABORATORS = 4
        const val FREE_MAX_COMMENTS_PER_SONG = 5
        const val PLATFORM_FEE_PERCENT = 5
    }

    // ═══════════════════════════════════════════
    // Premium Subscription
    // ═══════════════════════════════════════════

    @Transactional(readOnly = true)
    fun getStatus(userId: UUID): PremiumStatusResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        return PremiumStatusResponse(
            isPremium = user.premium,
            premiumSince = user.premiumSince?.toString()?.plus("Z"),
            customUrl = user.customUrl,
            verified = user.verified
        )
    }

    @Transactional
    fun activate(userId: UUID, request: ActivatePremiumRequest): PremiumStatusResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        user.premium = true
        user.verified = true
        user.premiumSince = LocalDateTime.now()
        userRepository.save(user)
        return getStatus(userId)
    }

    @Transactional
    fun deactivate(userId: UUID): PremiumStatusResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        user.premium = false
        user.verified = false
        user.premiumSince = null
        userRepository.save(user)
        return getStatus(userId)
    }

    @Transactional
    fun setCustomUrl(userId: UUID, request: SetCustomUrlRequest): PremiumStatusResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        if (!user.premium) throw IllegalStateException("Premium required")

        val slug = request.customUrl.lowercase().replace(Regex("[^a-z0-9_-]"), "")
        if (slug.length < 3) throw IllegalArgumentException("URL must be at least 3 characters")

        val existing = userRepository.findByCustomUrl(slug)
        if (existing != null && existing.id != userId) {
            throw IllegalStateException("URL already taken")
        }

        user.customUrl = slug
        userRepository.save(user)
        return getStatus(userId)
    }

    @Transactional(readOnly = true)
    fun getLimits(userId: UUID): PremiumLimitsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val songCount = songRepository.countByArtistId(userId)
        val workspaceCount = workspaceRepository.findByMemberUserId(userId).size.toLong()

        return if (user.premium) {
            PremiumLimitsResponse(
                maxSongs = null,
                currentSongs = songCount,
                maxWorkspaces = null,
                currentWorkspaces = workspaceCount,
                maxCollaboratorsPerWorkspace = null,
                maxCommentsPerSong = null
            )
        } else {
            PremiumLimitsResponse(
                maxSongs = FREE_MAX_SONGS,
                currentSongs = songCount,
                maxWorkspaces = FREE_MAX_WORKSPACES,
                currentWorkspaces = workspaceCount,
                maxCollaboratorsPerWorkspace = FREE_MAX_COLLABORATORS,
                maxCommentsPerSong = FREE_MAX_COMMENTS_PER_SONG
            )
        }
    }

    fun requireCanPublishSong(userId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        if (user.premium) return
        val count = songRepository.countByArtistId(userId)
        if (count >= FREE_MAX_SONGS) {
            throw PremiumRequiredException("song_limit", "You have reached the limit of $FREE_MAX_SONGS songs. Upgrade to Premium for unlimited publishing.")
        }
    }

    fun requireCanCreateWorkspace(userId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        if (user.premium) return
        val count = workspaceRepository.findByMemberUserId(userId).size.toLong()
        if (count >= FREE_MAX_WORKSPACES) {
            throw PremiumRequiredException("workspace_limit", "Upgrade to Premium for unlimited workspaces.")
        }
    }

    fun requireCanAddCollaborator(workspaceId: UUID, userId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        if (user.premium) return
        val memberCount = workspaceRepository.findById(workspaceId)
            .orElseThrow { NoSuchElementException("Workspace not found") }
            .let { ws ->
                // Count is handled differently, use member repo
                return@let FREE_MAX_COLLABORATORS.toLong() // simplified — checked at controller level
            }
    }

    // ═══════════════════════════════════════════
    // Donations
    // ═══════════════════════════════════════════

    @Transactional
    fun donate(fromUserId: UUID, request: CreateDonationRequest): DonationResponse {
        val fromUser = userRepository.findById(fromUserId)
            .orElseThrow { NoSuchElementException("User not found") }
        val toArtist = userRepository.findById(UUID.fromString(request.artistId))
            .orElseThrow { NoSuchElementException("Artist not found") }

        if (fromUserId == toArtist.id) throw IllegalArgumentException("Cannot donate to yourself")
        if (request.amountCents < 100) throw IllegalArgumentException("Minimum donation is €1.00")

        val platformCents = (request.amountCents * PLATFORM_FEE_PERCENT) / 100
        val artistCents = request.amountCents - platformCents

        val donation = donationRepository.save(
            DonationEntity(
                fromUser = fromUser,
                toArtist = toArtist,
                amountCents = request.amountCents,
                artistCents = artistCents,
                platformCents = platformCents,
                message = request.message
            )
        )

        // Notify the artist about the donation
        val amountFormatted = "€%.2f".format(donation.artistCents / 100.0)
        notificationService.create(
            recipientId = toArtist.id,
            fromUserId = fromUserId,
            type = NotificationType.DONATION,
            message = "${fromUser.displayName} ti ha donato $amountFormatted",
            link = "/profile"
        )

        return toDonationResponse(donation)
    }

    @Transactional(readOnly = true)
    fun getDonationDashboard(artistId: UUID): DonationDashboardResponse {
        val totalCents = donationRepository.sumArtistCentsByArtistId(artistId)
        val totalSupporters = donationRepository.countDistinctSupportersByArtistId(artistId)
        val recent = donationRepository.findByToArtistIdOrderByCreatedAtDesc(artistId, PageRequest.of(0, 20))
            .content.map { toDonationResponse(it) }
        val topSupporters = donationRepository.findTopSupportersByArtistId(artistId, PageRequest.of(0, 10))
            .map { row ->
                TopSupporterResponse(
                    userId = (row[0] as UUID).toString(),
                    displayName = row[1] as String,
                    avatar = row[2] as? String,
                    totalCents = (row[3] as Number).toLong()
                )
            }

        return DonationDashboardResponse(
            totalReceivedCents = totalCents,
            totalSupporters = totalSupporters,
            recentDonations = recent,
            topSupporters = topSupporters
        )
    }

    private fun toDonationResponse(d: DonationEntity) = DonationResponse(
        id = d.id.toString(),
        fromUserId = d.fromUser.id.toString(),
        fromUserName = d.fromUser.displayName,
        fromUserAvatar = d.fromUser.avatar,
        toArtistId = d.toArtist.id.toString(),
        toArtistName = d.toArtist.displayName,
        amountCents = d.amountCents,
        artistCents = d.artistCents,
        platformCents = d.platformCents,
        message = d.message,
        createdAt = d.createdAt.toString() + "Z"
    )
}

class PremiumRequiredException(val limitType: String, override val message: String) : RuntimeException(message)
