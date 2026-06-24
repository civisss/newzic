package com.newzic.service

import com.newzic.api.dto.*
import com.newzic.domain.entity.*
import com.newzic.domain.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class JournalService(
    private val journalPostRepository: JournalPostRepository,
    private val journalReactionRepository: JournalReactionRepository,
    private val journalCommentRepository: JournalCommentRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    // ═══════════════════════════════════════════
    // POST CRUD
    // ═══════════════════════════════════════════

    companion object {
        const val FREE_POST_LIMIT = 10
    }

    @Transactional
    fun createPost(userId: UUID, request: CreateJournalPostRequest): JournalPostResponse {
        val author = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        // Free users limited to 10 journal posts
        if (!author.premium) {
            val count = journalPostRepository.countByAuthorId(userId)
            if (count >= FREE_POST_LIMIT) {
                throw PremiumRequiredException(
                    limitType = "journal_posts",
                    message = "Free users can publish a maximum of $FREE_POST_LIMIT journal posts. Upgrade to Premium for unlimited posts."
                )
            }
        }

        val hashtags = extractHashtags(request.content) + request.hashtags
        val taggedIds = request.taggedUserIds.map { UUID.fromString(it) }.toMutableSet()

        val post = journalPostRepository.save(
            JournalPostEntity(
                author = author,
                content = request.content,
                imageUrl = request.imageUrl,
                category = parseCategory(request.category),
                hashtags = hashtags.map { it.lowercase().removePrefix("#") }.toMutableSet(),
                taggedUserIds = taggedIds
            )
        )

        // Notify tagged users
        taggedIds.forEach { taggedUserId ->
            notificationService.create(
                recipientId = taggedUserId,
                fromUserId = userId,
                type = NotificationType.JOURNAL_TAG,
                message = "${author.displayName} ti ha menzionato in un post",
                link = "/journal/${post.id}"
            )
        }

        return toResponse(post, userId)
    }

    @Transactional
    fun updatePost(userId: UUID, postId: UUID, request: UpdateJournalPostRequest): JournalPostResponse {
        val post = journalPostRepository.findById(postId)
            .orElseThrow { NoSuchElementException("Post not found") }
        if (post.author.id != userId) throw IllegalArgumentException("Not your post")

        request.content?.let { post.content = it }
        request.imageUrl?.let { post.imageUrl = it }
        request.category?.let { post.category = parseCategory(it) }
        request.hashtags?.let { post.hashtags = it.map { h -> h.lowercase().removePrefix("#") }.toMutableSet() }
        request.taggedUserIds?.let { post.taggedUserIds = it.map { UUID.fromString(it) }.toMutableSet() }
        post.updatedAt = LocalDateTime.now()

        return toResponse(journalPostRepository.save(post), userId)
    }

    @Transactional
    fun deletePost(userId: UUID, postId: UUID) {
        val post = journalPostRepository.findById(postId)
            .orElseThrow { NoSuchElementException("Post not found") }
        if (post.author.id != userId) throw IllegalArgumentException("Not your post")
        journalPostRepository.delete(post)
    }

    @Transactional(readOnly = true)
    fun getPost(postId: UUID, currentUserId: UUID?): JournalPostResponse {
        val post = journalPostRepository.findById(postId)
            .orElseThrow { NoSuchElementException("Post not found") }
        return toResponse(post, currentUserId)
    }

    @Transactional(readOnly = true)
    fun getPostsByAuthor(authorId: UUID, currentUserId: UUID?, pageable: Pageable): Page<JournalPostResponse> {
        return journalPostRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable)
            .map { toResponse(it, currentUserId) }
    }

    // ═══════════════════════════════════════════
    // FEED
    // ═══════════════════════════════════════════

    @Transactional(readOnly = true)
    fun getFeed(currentUserId: UUID?, pageable: Pageable): Page<JournalPostResponse> {
        return journalPostRepository.findFeedPosts(pageable)
            .map { toResponse(it, currentUserId) }
    }

    // ═══════════════════════════════════════════
    // SEARCH
    // ═══════════════════════════════════════════

    @Transactional(readOnly = true)
    fun search(query: String, currentUserId: UUID?, pageable: Pageable): Page<JournalPostResponse> {
        return if (query.startsWith("#")) {
            journalPostRepository.findByHashtag(query.removePrefix("#").lowercase(), pageable)
        } else {
            journalPostRepository.searchByContent(query, pageable)
        }.map { toResponse(it, currentUserId) }
    }

    // ═══════════════════════════════════════════
    // REACTIONS
    // ═══════════════════════════════════════════

    @Transactional
    fun toggleReaction(userId: UUID, postId: UUID, type: String): ReactionSummaryResponse {
        val post = journalPostRepository.findById(postId)
            .orElseThrow { NoSuchElementException("Post not found") }
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        val reactionType = JournalReactionType.valueOf(type.uppercase())

        val exists = journalReactionRepository.existsByPostIdAndUserIdAndType(postId, userId, reactionType)
        if (exists) {
            journalReactionRepository.deleteByPostIdAndUserIdAndType(postId, userId, reactionType)
        } else {
            journalReactionRepository.save(
                JournalReactionEntity(user = user, post = post, type = reactionType)
            )

            // Notify post author
            if (post.author.id != userId) {
                val emoji = when (reactionType) {
                    JournalReactionType.LIKE -> "❤️"
                    JournalReactionType.FIRE -> "🔥"
                    JournalReactionType.MUSIC -> "🎵"
                    JournalReactionType.HYPE -> "🚀"
                }
                notificationService.create(
                    recipientId = post.author.id,
                    fromUserId = userId,
                    type = NotificationType.JOURNAL_REACTION,
                    message = "${user.displayName} ha reagito $emoji al tuo post",
                    link = "/journal/${post.id}"
                )
            }
        }

        // Update cached count
        post.reactionCount = journalReactionRepository.countByPostId(postId)
        journalPostRepository.save(post)

        return getReactionSummary(postId, userId)
    }

    // ═══════════════════════════════════════════
    // COMMENTS
    // ═══════════════════════════════════════════

    @Transactional
    fun addComment(userId: UUID, postId: UUID, request: CreateJournalCommentRequest): JournalCommentResponse {
        val post = journalPostRepository.findById(postId)
            .orElseThrow { NoSuchElementException("Post not found") }
        val author = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }

        val comment = journalCommentRepository.save(
            JournalCommentEntity(post = post, author = author, content = request.content)
        )

        // Update cached count
        post.commentCount = journalCommentRepository.countByPostId(postId)
        journalPostRepository.save(post)

        // Notify post author
        if (post.author.id != userId) {
            notificationService.create(
                recipientId = post.author.id,
                fromUserId = userId,
                type = NotificationType.JOURNAL_COMMENT,
                message = "${author.displayName} ha commentato il tuo post",
                link = "/journal/${post.id}"
            )
        }

        return toCommentResponse(comment)
    }

    @Transactional
    fun deleteComment(userId: UUID, commentId: UUID) {
        val comment = journalCommentRepository.findById(commentId)
            .orElseThrow { NoSuchElementException("Comment not found") }
        if (comment.author.id != userId) throw IllegalArgumentException("Not your comment")

        val postId = comment.post.id
        journalCommentRepository.delete(comment)

        // Update cached count
        val post = journalPostRepository.findById(postId).orElse(null)
        if (post != null) {
            post.commentCount = journalCommentRepository.countByPostId(postId)
            journalPostRepository.save(post)
        }
    }

    @Transactional(readOnly = true)
    fun getComments(postId: UUID, pageable: Pageable): Page<JournalCommentResponse> {
        return journalCommentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)
            .map { toCommentResponse(it) }
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private fun extractHashtags(content: String): List<String> {
        return Regex("#(\\w+)").findAll(content).map { it.groupValues[1] }.toList()
    }

    private fun parseCategory(cat: String): JournalCategory {
        return try {
            JournalCategory.valueOf(cat.uppercase())
        } catch (e: IllegalArgumentException) {
            JournalCategory.UPDATE
        }
    }

    private fun getReactionSummary(postId: UUID, currentUserId: UUID?): ReactionSummaryResponse {
        val userReactions = if (currentUserId != null) {
            journalReactionRepository.findByPostIdAndUserId(postId, currentUserId)
                .map { it.type.name.lowercase() }
        } else emptyList()

        return ReactionSummaryResponse(
            total = journalReactionRepository.countByPostId(postId),
            like = journalReactionRepository.countByPostIdAndType(postId, JournalReactionType.LIKE),
            fire = journalReactionRepository.countByPostIdAndType(postId, JournalReactionType.FIRE),
            music = journalReactionRepository.countByPostIdAndType(postId, JournalReactionType.MUSIC),
            hype = journalReactionRepository.countByPostIdAndType(postId, JournalReactionType.HYPE),
            userReactions = userReactions
        )
    }

    private fun toResponse(post: JournalPostEntity, currentUserId: UUID?): JournalPostResponse {
        val taggedUsers = if (post.taggedUserIds.isNotEmpty()) {
            userRepository.findAllById(post.taggedUserIds).map {
                TaggedUserResponse(it.id.toString(), it.username, it.displayName, it.avatar)
            }
        } else emptyList()

        return JournalPostResponse(
            id = post.id.toString(),
            authorId = post.author.id.toString(),
            authorName = post.author.displayName,
            authorUsername = post.author.username,
            authorAvatar = post.author.avatar,
            authorRole = post.author.roles.firstOrNull()?.name?.lowercase() ?: "singer",
            authorPremium = post.author.premium,
            authorVerified = post.author.verified,
            content = post.content,
            imageUrl = post.imageUrl,
            category = post.category.name.lowercase(),
            hashtags = post.hashtags.toList(),
            taggedUsers = taggedUsers,
            reactions = getReactionSummary(post.id, currentUserId),
            commentCount = post.commentCount,
            createdAt = post.createdAt.toString() + "Z",
            updatedAt = post.updatedAt.toString() + "Z"
        )
    }

    private fun toCommentResponse(c: JournalCommentEntity): JournalCommentResponse {
        return JournalCommentResponse(
            id = c.id.toString(),
            postId = c.post.id.toString(),
            authorId = c.author.id.toString(),
            authorName = c.author.displayName,
            authorUsername = c.author.username,
            authorAvatar = c.author.avatar,
            content = c.content,
            createdAt = c.createdAt.toString() + "Z"
        )
    }
}
