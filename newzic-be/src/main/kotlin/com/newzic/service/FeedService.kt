package com.newzic.service

import com.newzic.api.dto.CreateFeedPostRequest
import com.newzic.api.dto.FeedPostResponse
import com.newzic.api.dto.ReactionsDto
import com.newzic.domain.entity.FeedPostEntity
import com.newzic.domain.entity.FeedPostType
import com.newzic.domain.repository.FeedPostRepository
import com.newzic.domain.repository.FollowRepository
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FeedService(
    private val feedPostRepository: FeedPostRepository,
    private val userRepository: UserRepository,
    private val songRepository: SongRepository,
    private val followRepository: FollowRepository
) {

    fun getFeed(pageable: Pageable): Page<FeedPostResponse> {
        return feedPostRepository.findAllByOrderByCreatedAtDesc(pageable).map { toResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getPersonalizedFeed(userId: UUID, pageable: Pageable): Page<FeedPostResponse> {
        val totalSize = pageable.pageSize
        val followedSize = (totalSize * 0.6).toInt().coerceAtLeast(1)
        val discoverSize = totalSize - followedSize

        // Get followed artist IDs
        val followedIds = followRepository.findByFollowerId(userId)
            .map { it.following.id }
            .toSet()

        val allPosts = mutableListOf<FeedPostEntity>()

        // ── 60% from followed artists ──
        if (followedIds.isNotEmpty()) {
            val followedPosts = feedPostRepository.findByAuthorIds(
                followedIds, PageRequest.of(pageable.pageNumber, followedSize)
            ).content
            allPosts.addAll(followedPosts)
        }

        // ── 40% discover (from non-followed, sorted by popularity) ──
        val excludeIds = followedIds + userId
        if (excludeIds.isNotEmpty()) {
            val discoverPosts = feedPostRepository.findDiscoverExcluding(
                excludeIds, PageRequest.of(pageable.pageNumber, discoverSize)
            ).content
            allPosts.addAll(discoverPosts)
        }

        // Sort merged list: followed posts by time, discover by engagement
        val sorted = allPosts
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }

        return PageImpl(sorted.map { toResponse(it) }, pageable, sorted.size.toLong())
    }

    fun getByAuthor(authorId: UUID, pageable: Pageable): Page<FeedPostResponse> {
        return feedPostRepository.findByAuthorId(authorId, pageable).map { toResponse(it) }
    }

    @Transactional
    fun create(userId: UUID, request: CreateFeedPostRequest): FeedPostResponse {
        val author = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val postType = try { FeedPostType.valueOf(request.type.uppercase()) } catch (e: Exception) {
            throw IllegalArgumentException("Invalid post type: ${request.type}")
        }

        val song = request.songId?.let {
            songRepository.findById(UUID.fromString(it)).orElse(null)
        }

        val post = FeedPostEntity(
            type = postType,
            author = author,
            content = request.content,
            image = request.image,
            song = song
        )

        return toResponse(feedPostRepository.save(post))
    }

    private fun toResponse(post: FeedPostEntity): FeedPostResponse {
        return FeedPostResponse(
            id = post.id.toString(),
            type = post.type.name.lowercase(),
            authorId = post.author.id.toString(),
            authorName = post.author.displayName,
            authorAvatar = post.author.avatar,
            authorRole = post.author.roles.firstOrNull()?.name?.lowercase() ?: "singer",
            content = post.content,
            image = post.image,
            songId = post.song?.id?.toString(),
            songTitle = post.song?.title,
            songCover = post.song?.cover,
            timestamp = post.createdAt.toString(),
            likes = post.likes,
            comments = post.comments,
            reactions = ReactionsDto(
                fire = post.reactionsFire,
                gem = post.reactionsGem,
                onpoint = post.reactionsOnpoint,
                star = post.reactionsStar
            )
        )
    }
}
