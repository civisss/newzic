package com.newzic.service

import com.newzic.api.dto.*
import com.newzic.domain.entity.*
import com.newzic.domain.repository.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JournalServiceTest {

    @Mock private lateinit var journalPostRepository: JournalPostRepository
    @Mock private lateinit var journalReactionRepository: JournalReactionRepository
    @Mock private lateinit var journalCommentRepository: JournalCommentRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var notificationService: NotificationService

    @InjectMocks
    private lateinit var journalService: JournalService

    private lateinit var author: UserEntity
    private lateinit var otherUser: UserEntity
    private lateinit var post: JournalPostEntity

    @BeforeEach
    fun setUp() {
        author = UserEntity(
            id = UUID.randomUUID(), displayName = "TestArtist", username = "testartist",
            email = "artist@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        otherUser = UserEntity(
            id = UUID.randomUUID(), displayName = "OtherUser", username = "otheruser",
            email = "other@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        post = JournalPostEntity(
            id = UUID.randomUUID(),
            author = author,
            content = "Working on something new! #beats @otheruser",
            category = JournalCategory.UPDATE,
            hashtags = mutableSetOf("beats"),
            taggedUserIds = mutableSetOf(otherUser.id)
        )
    }

    // ═══════════════════════════════════════════
    // CREATE POST
    // ═══════════════════════════════════════════

    @Test
    fun `createPost should create post and return response`() {
        val request = CreateJournalPostRequest(content = "Hello #world", category = "update")

        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalPostRepository.countByAuthorId(author.id)).thenReturn(0)
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())

        val response = journalService.createPost(author.id, request)

        assertEquals("Hello #world", response.content)
        assertEquals(author.username, response.authorUsername)
        assertEquals("update", response.category)
        verify(journalPostRepository).save(any<JournalPostEntity>())
    }

    @Test
    fun `createPost should notify tagged users`() {
        val taggedUserId = otherUser.id
        val request = CreateJournalPostRequest(
            content = "Hey @otheruser check this",
            taggedUserIds = listOf(taggedUserId.toString())
        )

        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalPostRepository.countByAuthorId(author.id)).thenReturn(0)
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }
        whenever(userRepository.findAllById(any<Iterable<UUID>>())).thenReturn(listOf(otherUser))
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())

        journalService.createPost(author.id, request)

        verify(notificationService).create(
            recipientId = eq(taggedUserId),
            fromUserId = eq(author.id),
            type = eq(NotificationType.JOURNAL_TAG),
            message = any(),
            link = any(),
            songId = isNull()
        )
    }

    @Test
    fun `createPost should extract hashtags from content`() {
        val request = CreateJournalPostRequest(content = "Working on #beats and #lofi vibes")

        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalPostRepository.countByAuthorId(author.id)).thenReturn(0)
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())

        val response = journalService.createPost(author.id, request)

        assertTrue(response.hashtags.contains("beats"))
        assertTrue(response.hashtags.contains("lofi"))
    }

    // ═══════════════════════════════════════════
    // FREE USER POST LIMIT
    // ═══════════════════════════════════════════

    @Test
    fun `createPost should throw PremiumRequiredException when free user exceeds limit`() {
        val request = CreateJournalPostRequest(content = "Too many posts")

        author.premium = false
        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalPostRepository.countByAuthorId(author.id)).thenReturn(10)

        val ex = assertThrows<PremiumRequiredException> {
            journalService.createPost(author.id, request)
        }

        assertEquals("journal_posts", ex.limitType)
        assertTrue(ex.message.contains("10"))
        verify(journalPostRepository, never()).save(any<JournalPostEntity>())
    }

    @Test
    fun `createPost should allow free user under the limit`() {
        val request = CreateJournalPostRequest(content = "Still room")

        author.premium = false
        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalPostRepository.countByAuthorId(author.id)).thenReturn(9)
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())

        val response = journalService.createPost(author.id, request)

        assertNotNull(response)
        verify(journalPostRepository).save(any<JournalPostEntity>())
    }

    @Test
    fun `createPost should allow premium user past the limit`() {
        val request = CreateJournalPostRequest(content = "Unlimited power")

        author.premium = true
        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())

        val response = journalService.createPost(author.id, request)

        assertNotNull(response)
        verify(journalPostRepository, never()).countByAuthorId(any())
    }

    // ═══════════════════════════════════════════
    // UPDATE POST
    // ═══════════════════════════════════════════

    @Test
    fun `updatePost should update content`() {
        val request = UpdateJournalPostRequest(content = "Updated content")

        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())
        whenever(userRepository.findAllById(any<Iterable<UUID>>())).thenReturn(listOf(otherUser))

        val response = journalService.updatePost(author.id, post.id, request)

        assertEquals("Updated content", response.content)
    }

    @Test
    fun `updatePost should throw if not owner`() {
        val request = UpdateJournalPostRequest(content = "Hacked")
        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))

        assertThrows<IllegalArgumentException> {
            journalService.updatePost(otherUser.id, post.id, request)
        }
    }

    // ═══════════════════════════════════════════
    // DELETE POST
    // ═══════════════════════════════════════════

    @Test
    fun `deletePost should delete own post`() {
        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))

        journalService.deletePost(author.id, post.id)

        verify(journalPostRepository).delete(post)
    }

    @Test
    fun `deletePost should throw if not owner`() {
        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))

        assertThrows<IllegalArgumentException> {
            journalService.deletePost(otherUser.id, post.id)
        }
    }

    // ═══════════════════════════════════════════
    // REACTIONS
    // ═══════════════════════════════════════════

    @Test
    fun `toggleReaction should add reaction and notify post author`() {
        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))
        whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))
        whenever(journalReactionRepository.existsByPostIdAndUserIdAndType(post.id, otherUser.id, JournalReactionType.FIRE)).thenReturn(false)
        whenever(journalReactionRepository.save(any<JournalReactionEntity>())).thenAnswer { it.arguments[0] as JournalReactionEntity }
        whenever(journalReactionRepository.countByPostId(post.id)).thenReturn(1)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(post.id, JournalReactionType.FIRE)).thenReturn(1)
        whenever(journalReactionRepository.findByPostIdAndUserId(post.id, otherUser.id)).thenReturn(
            listOf(JournalReactionEntity(user = otherUser, post = post, type = JournalReactionType.FIRE))
        )
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }

        val summary = journalService.toggleReaction(otherUser.id, post.id, "fire")

        assertEquals(1, summary.fire)
        verify(notificationService).create(
            recipientId = eq(author.id),
            fromUserId = eq(otherUser.id),
            type = eq(NotificationType.JOURNAL_REACTION),
            message = any(),
            link = any(),
            songId = isNull()
        )
    }

    @Test
    fun `toggleReaction should remove existing reaction`() {
        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))
        whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))
        whenever(journalReactionRepository.existsByPostIdAndUserIdAndType(post.id, otherUser.id, JournalReactionType.LIKE)).thenReturn(true)
        whenever(journalReactionRepository.countByPostId(post.id)).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(post.id, otherUser.id)).thenReturn(emptyList())
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }

        val summary = journalService.toggleReaction(otherUser.id, post.id, "like")

        assertEquals(0, summary.total)
        verify(journalReactionRepository).deleteByPostIdAndUserIdAndType(post.id, otherUser.id, JournalReactionType.LIKE)
        verify(notificationService, never()).create(any(), any(), any(), any(), any(), anyOrNull())
    }

    @Test
    fun `toggleReaction should not notify when reacting to own post`() {
        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))
        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalReactionRepository.existsByPostIdAndUserIdAndType(post.id, author.id, JournalReactionType.LIKE)).thenReturn(false)
        whenever(journalReactionRepository.save(any<JournalReactionEntity>())).thenAnswer { it.arguments[0] as JournalReactionEntity }
        whenever(journalReactionRepository.countByPostId(post.id)).thenReturn(1)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(post.id, author.id)).thenReturn(
            listOf(JournalReactionEntity(user = author, post = post, type = JournalReactionType.LIKE))
        )
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }

        journalService.toggleReaction(author.id, post.id, "like")

        verify(notificationService, never()).create(any(), any(), any(), any(), any(), anyOrNull())
    }

    // ═══════════════════════════════════════════
    // COMMENTS
    // ═══════════════════════════════════════════

    @Test
    fun `addComment should add comment and notify author`() {
        val request = CreateJournalCommentRequest(content = "Great post!")

        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))
        whenever(userRepository.findById(otherUser.id)).thenReturn(Optional.of(otherUser))
        whenever(journalCommentRepository.save(any<JournalCommentEntity>())).thenAnswer { it.arguments[0] as JournalCommentEntity }
        whenever(journalCommentRepository.countByPostId(post.id)).thenReturn(1)
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }

        val response = journalService.addComment(otherUser.id, post.id, request)

        assertEquals("Great post!", response.content)
        assertEquals(otherUser.username, response.authorUsername)
        verify(notificationService).create(
            recipientId = eq(author.id),
            fromUserId = eq(otherUser.id),
            type = eq(NotificationType.JOURNAL_COMMENT),
            message = any(),
            link = any(),
            songId = isNull()
        )
    }

    @Test
    fun `addComment should not notify when commenting on own post`() {
        val request = CreateJournalCommentRequest(content = "Self comment")

        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))
        whenever(userRepository.findById(author.id)).thenReturn(Optional.of(author))
        whenever(journalCommentRepository.save(any<JournalCommentEntity>())).thenAnswer { it.arguments[0] as JournalCommentEntity }
        whenever(journalCommentRepository.countByPostId(post.id)).thenReturn(1)
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }

        journalService.addComment(author.id, post.id, request)

        verify(notificationService, never()).create(any(), any(), any(), any(), any(), anyOrNull())
    }

    @Test
    fun `deleteComment should delete own comment and update count`() {
        val comment = JournalCommentEntity(
            id = UUID.randomUUID(), post = post, author = otherUser, content = "To delete"
        )

        whenever(journalCommentRepository.findById(comment.id)).thenReturn(Optional.of(comment))
        whenever(journalPostRepository.findById(post.id)).thenReturn(Optional.of(post))
        whenever(journalCommentRepository.countByPostId(post.id)).thenReturn(0)
        whenever(journalPostRepository.save(any<JournalPostEntity>())).thenAnswer { it.arguments[0] as JournalPostEntity }

        journalService.deleteComment(otherUser.id, comment.id)

        verify(journalCommentRepository).delete(comment)
        assertEquals(0, post.commentCount)
    }

    @Test
    fun `deleteComment should throw if not owner`() {
        val comment = JournalCommentEntity(
            id = UUID.randomUUID(), post = post, author = otherUser, content = "Not yours"
        )

        whenever(journalCommentRepository.findById(comment.id)).thenReturn(Optional.of(comment))

        assertThrows<IllegalArgumentException> {
            journalService.deleteComment(author.id, comment.id)
        }
    }

    // ═══════════════════════════════════════════
    // FEED & SEARCH
    // ═══════════════════════════════════════════

    @Test
    fun `getFeed should return paged posts`() {
        val page = PageImpl(listOf(post))
        val pageable = PageRequest.of(0, 20)

        whenever(journalPostRepository.findFeedPosts(pageable)).thenReturn(page)
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())
        whenever(userRepository.findAllById(any<Iterable<UUID>>())).thenReturn(listOf(otherUser))

        val result = journalService.getFeed(author.id, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(post.content, result.content[0].content)
    }

    @Test
    fun `search by hashtag should delegate to findByHashtag`() {
        val page = PageImpl(listOf(post))
        val pageable = PageRequest.of(0, 20)

        whenever(journalPostRepository.findByHashtag("beats", pageable)).thenReturn(page)
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())
        whenever(userRepository.findAllById(any<Iterable<UUID>>())).thenReturn(listOf(otherUser))

        val result = journalService.search("#beats", author.id, pageable)

        assertEquals(1, result.totalElements)
        verify(journalPostRepository).findByHashtag("beats", pageable)
        verify(journalPostRepository, never()).searchByContent(any(), any())
    }

    @Test
    fun `search by text should delegate to searchByContent`() {
        val page = PageImpl(listOf(post))
        val pageable = PageRequest.of(0, 20)

        whenever(journalPostRepository.searchByContent("something", pageable)).thenReturn(page)
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())
        whenever(userRepository.findAllById(any<Iterable<UUID>>())).thenReturn(listOf(otherUser))

        val result = journalService.search("something", author.id, pageable)

        assertEquals(1, result.totalElements)
        verify(journalPostRepository).searchByContent("something", pageable)
    }

    @Test
    fun `getPostsByAuthor should return author posts`() {
        val page = PageImpl(listOf(post))
        val pageable = PageRequest.of(0, 20)

        whenever(journalPostRepository.findByAuthorIdOrderByCreatedAtDesc(author.id, pageable)).thenReturn(page)
        whenever(journalReactionRepository.countByPostId(any())).thenReturn(0)
        whenever(journalReactionRepository.countByPostIdAndType(any(), any())).thenReturn(0)
        whenever(journalReactionRepository.findByPostIdAndUserId(any(), any())).thenReturn(emptyList())
        whenever(userRepository.findAllById(any<Iterable<UUID>>())).thenReturn(listOf(otherUser))

        val result = journalService.getPostsByAuthor(author.id, author.id, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(author.username, result.content[0].authorUsername)
    }
}
