package com.newzic.service

import com.newzic.api.dto.UpdateProfileRequest
import com.newzic.api.dto.UserResponse
import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.NotificationType
import com.newzic.domain.repository.FollowRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.Page
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
    private val notificationService: NotificationService
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

    fun getTrending(pageable: Pageable): Page<UserResponse> {
        return userRepository.findTrending(pageable).map { userMapper.toResponse(it) }
    }

    fun getProducers(pageable: Pageable): Page<UserResponse> {
        return userRepository.findByRole(ArtistRole.PRODUCER, pageable).map { userMapper.toResponse(it) }
    }

    fun getCommunityPicks(pageable: Pageable): Page<UserResponse> {
        return userRepository.findCommunityPicks(pageable).map { userMapper.toResponse(it) }
    }

    fun search(query: String, pageable: Pageable): Page<UserResponse> {
        return userRepository.search(query, pageable).map { userMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getRecommended(userId: UUID, limit: Int = 10): List<UserResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val allOthers = userRepository.findAllExcept(userId)
        val userPreferredGenres = user.preferredGenres
        val userProducedGenres = user.genres
        val userCountry = user.country

        data class ScoredUser(val entity: com.newzic.domain.entity.UserEntity, val score: Double)

        val scored = allOthers.map { other ->
            var score = 0.0

            // ── GEOGRAPHY ──
            if (userCountry != null && other.country != null) {
                if (other.country == userCountry) {
                    // Same country → strong affinity
                    score += 40.0
                } else if (getRegion(other.country!!) == getRegion(userCountry)) {
                    // Same region (e.g. both European) → moderate affinity
                    score += 15.0
                }
            }

            // ── GENRE MATCH: user likes ↔ artist produces ──
            if (userPreferredGenres.isNotEmpty() && other.genres.isNotEmpty()) {
                val overlap = userPreferredGenres.intersect(other.genres).size
                // Each matching genre is worth 25 points (strongest signal)
                score += overlap * 25.0
            }

            // ── GENRE MATCH: user produces ↔ artist produces (similar taste) ──
            if (userProducedGenres.isNotEmpty() && other.genres.isNotEmpty()) {
                val overlap = userProducedGenres.intersect(other.genres).size
                // Same genre scene → moderate affinity
                score += overlap * 10.0
            }

            // ── MUTUAL TASTE: user likes ↔ artist likes ──
            if (userPreferredGenres.isNotEmpty() && other.preferredGenres.isNotEmpty()) {
                val overlap = userPreferredGenres.intersect(other.preferredGenres).size
                score += overlap * 8.0
            }

            // ── POPULARITY BOOST (small, prevents cold-start) ──
            score += (other.followers / 10000.0).coerceAtMost(10.0)

            // ── ENGAGEMENT: artists with more plays are slightly more relevant ──
            score += (other.totalPlays / 500000.0).coerceAtMost(5.0)

            // ── VERIFIED ARTISTS get a small trust boost ──
            if (other.verified) score += 3.0

            ScoredUser(other, score)
        }

        return scored
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(limit)
            .map { userMapper.toResponse(it.entity) }
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
