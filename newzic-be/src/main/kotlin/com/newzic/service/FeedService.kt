package com.newzic.service

import com.newzic.api.dto.CreateFeedPostRequest
import com.newzic.api.dto.FeedPostResponse
import com.newzic.api.dto.ReactionsDto
import com.newzic.domain.entity.FeedPostEntity
import com.newzic.domain.entity.FeedPostType
import com.newzic.domain.repository.FeedPostRepository
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FeedService(
    private val feedPostRepository: FeedPostRepository,
    private val userRepository: UserRepository,
    private val songRepository: SongRepository
) {

    fun getFeed(pageable: Pageable): Page<FeedPostResponse> {
        return feedPostRepository.findAllByOrderByCreatedAtDesc(pageable).map { toResponse(it) }
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
