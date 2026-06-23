package com.newzic.service

import com.newzic.api.dto.UpdateProfileRequest
import com.newzic.api.dto.UserResponse
import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.NotificationType
import com.newzic.domain.repository.FollowRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val userMapper: UserMapper,
    private val notificationService: NotificationService,
    private val recommendationService: RecommendationService
) {

    fun getById(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found") }
        return userMapper.toResponse(user)
    }

    fun getByUsername(username: String): UserResponse {
        val user = userRepository.findByUsername(username)
            ?: throw NoSuchElementException("User not found")
        return userMapper.toResponse(user)
    }

    fun getTrending(pageable: Pageable, excludeId: UUID? = null): Page<UserResponse> {
        return userRepository.findTrending(pageable)
            .let { page -> if (excludeId != null) filterPage(page, excludeId) else page }
            .map { userMapper.toResponse(it) }
    }

    fun getProducers(pageable: Pageable, excludeId: UUID? = null): Page<UserResponse> {
        return userRepository.findByRole(ArtistRole.PRODUCER, pageable)
            .let { page -> if (excludeId != null) filterPage(page, excludeId) else page }
            .map { userMapper.toResponse(it) }
    }

    fun getCommunityPicks(pageable: Pageable, excludeId: UUID? = null): Page<UserResponse> {
        return userRepository.findCommunityPicks(pageable)
            .let { page -> if (excludeId != null) filterPage(page, excludeId) else page }
            .map { userMapper.toResponse(it) }
    }

    fun search(query: String, pageable: Pageable, excludeId: UUID? = null): Page<UserResponse> {
        return userRepository.search(query, pageable)
            .let { page -> if (excludeId != null) filterPage(page, excludeId) else page }
            .map { userMapper.toResponse(it) }
    }

    private fun <T : com.newzic.domain.entity.UserEntity> filterPage(page: Page<T>, excludeId: UUID): Page<T> {
        val filtered = page.content.filter { it.id != excludeId }
        return org.springframework.data.domain.PageImpl(filtered, page.pageable, page.totalElements - 1)
    }

    @Transactional(readOnly = true)
    fun getRecommended(userId: UUID, limit: Int = 10): List<UserResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val profile = recommendationService.buildTasteProfile(user)
        val topGenres = profile.topGenres
        val userCountry = user.country

        // ── Fetch candidates from DB (filtered, not findAll) ──
        val candidateLimit = limit * 5
        val candidates = if (topGenres.isNotEmpty() && userCountry != null) {
            userRepository.findRecommendationCandidates(
                userId, userCountry, topGenres, PageRequest.of(0, candidateLimit)
            ).content
        } else if (topGenres.isNotEmpty()) {
            userRepository.findByGenresExcludingUser(
                userId, topGenres, PageRequest.of(0, candidateLimit)
            ).content
        } else {
            userRepository.findTrending(PageRequest.of(0, candidateLimit)).content
                .filter { it.id != userId }
        }

        // ── Score each candidate using the full taste profile ──
        data class ScoredUser(val entity: com.newzic.domain.entity.UserEntity, val score: Double)

        val scored = candidates
            .filter { it.id !in profile.followedArtistIds } // exclude already-followed
            .map { other ->
            var score = 0.0

            // ── GEOGRAPHY from taste profile ──
            if (other.country != null) {
                score += profile.countryScores[other.country] ?: 0.0
                // Region bonus
                if (userCountry != null && other.country != userCountry
                    && getRegion(other.country!!) == getRegion(userCountry)) {
                    score += 15.0
                }
            }

            // ── GENRE MATCH using taste profile scores ──
            if (other.genres.isNotEmpty()) {
                other.genres.forEach { genre ->
                    score += (profile.genreScores[genre] ?: 0.0) * 0.5
                }
            }

            // ── MUTUAL TASTE: user likes ↔ artist likes ──
            if (topGenres.isNotEmpty() && other.preferredGenres.isNotEmpty()) {
                val overlap = topGenres.intersect(other.preferredGenres).size
                score += overlap * 8.0
            }

            // ── POPULARITY BOOST (small, prevents cold-start) ──
            score += (other.followers / 10000.0).coerceAtMost(10.0)

            // ── ENGAGEMENT ──
            score += (other.totalPlays / 500000.0).coerceAtMost(5.0)

            // ── VERIFIED ARTISTS get a small trust boost ──
            if (other.verified) score += 3.0

            // ── PREMIUM DISCOVERY boost (moderate, quality first) ──
            if (other.premium) score += 6.0

            ScoredUser(other, score)
        }

        val minResults = 4.coerceAtMost(limit)
        val matched = scored
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(limit)
            .map { it.entity }

        if (matched.size >= minResults) {
            return matched.map { userMapper.toResponse(it) }
        }

        // Not enough scored results — fill with trending artists
        val excludeIds = matched.map { it.id }.toSet() + profile.followedArtistIds + userId
        val fillCount = minResults - matched.size
        val filler = userRepository.findTrending(PageRequest.of(0, fillCount + 10)).content
            .filter { it.id !in excludeIds }
            .take(fillCount)

        return (matched + filler).map { userMapper.toResponse(it) }
    }

    private fun getRegion(countryCode: String): String {
        return when (countryCode) {
            "IT", "ES", "FR", "DE", "GB", "NL", "SE", "PT", "BE", "AT", "CH", "IE", "NO", "DK", "FI", "PL", "CZ", "GR", "RO", "HU" -> "EUROPE"
            "US", "CA", "MX" -> "NORTH_AMERICA"
            "BR", "AR", "CO", "CL", "PE", "VE", "EC", "UY" -> "SOUTH_AMERICA"
            "JP", "KR", "CN", "IN", "TH", "ID", "PH", "VN", "MY", "SG", "TW" -> "ASIA"
            "AU", "NZ" -> "OCEANIA"
            "NG", "ZA", "KE", "GH", "EG", "MA", "TZ" -> "AFRICA"
            "AE", "SA", "IL", "TR", "QA" -> "MIDDLE_EAST"
            else -> "OTHER"
        }
    }

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        request.displayName?.let { user.displayName = it }
        request.avatar?.let { user.avatar = it }
        request.bio?.let { user.bio = it }
        request.longBio?.let { user.longBio = it }
        request.country?.let { user.country = it }
        request.location?.let { user.location = it }
        request.genres?.let { user.genres = it.toMutableSet() }
        request.tags?.let { user.tags = it.toMutableSet() }
        request.preferredGenres?.let { user.preferredGenres = it.toMutableSet() }
        request.lookingForCollab?.let { user.lookingForCollab = it }
        request.collabDescription?.let { user.collabDescription = it }
        request.preferredLanguage?.let { user.preferredLanguage = it }
        request.socialLinks?.let { links ->
            links.spotify?.let { user.spotifyUrl = it }
            links.youtubeMusic?.let { user.youtubeMusicUrl = it }
            links.appleMusic?.let { user.appleMusicUrl = it }
            links.soundcloud?.let { user.soundcloudUrl = it }
            links.tiktok?.let { user.tiktokUrl = it }
            links.instagram?.let { user.instagramUrl = it }
        }
        user.updatedAt = LocalDateTime.now()

        return userMapper.toResponse(userRepository.save(user))
    }

    @Transactional
    fun follow(followerId: UUID, followingId: UUID): Boolean {
        if (followerId == followingId) throw IllegalArgumentException("Cannot follow yourself")

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            // Unfollow
            val follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            follow?.let { followRepository.delete(it) }

            val following = userRepository.findById(followingId).orElse(null)
            following?.let { it.followers = maxOf(0, it.followers - 1); userRepository.save(it) }
            val follower = userRepository.findById(followerId).orElse(null)
            follower?.let { it.following = maxOf(0, it.following - 1); userRepository.save(it) }

            return false // unfollowed
        } else {
            val follower = userRepository.findById(followerId)
                .orElseThrow { NoSuchElementException("User not found") }
            val following = userRepository.findById(followingId)
                .orElseThrow { NoSuchElementException("User not found") }

            followRepository.save(
                com.newzic.domain.entity.FollowEntity(follower = follower, following = following)
            )
            following.followers += 1
            follower.following += 1
            userRepository.save(following)
            userRepository.save(follower)

            notificationService.create(
                recipientId = followingId,
                fromUserId = followerId,
                type = NotificationType.FOLLOW,
                message = "${follower.displayName} started following you",
                link = "/artist/$followerId"
            )

            return true // followed
        }
    }

    fun isFollowing(followerId: UUID, followingId: UUID): Boolean {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)
    }

    @Transactional(readOnly = true)
    fun getFollowers(userId: UUID): List<UserResponse> {
        return followRepository.findByFollowingId(userId)
            .map { userMapper.toResponse(it.follower) }
    }

    @Transactional(readOnly = true)
    fun getFollowing(userId: UUID): List<UserResponse> {
        return followRepository.findByFollowerId(userId)
            .map { userMapper.toResponse(it.following) }
    }
}
