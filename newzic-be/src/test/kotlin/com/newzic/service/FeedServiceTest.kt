package com.newzic.service

import com.newzic.domain.entity.*
import com.newzic.domain.repository.FeedPostRepository
import com.newzic.domain.repository.FollowRepository
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class FeedServiceTest {

    @Mock private lateinit var feedPostRepository: FeedPostRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var songRepository: SongRepository
    @Mock private lateinit var followRepository: FollowRepository

    @InjectMocks
    private lateinit var feedService: FeedService

    private lateinit var user: UserEntity
    private lateinit var followedArtist: UserEntity
    private lateinit var discoverArtist: UserEntity

    @BeforeEach
    fun setUp() {
        user = createUser("User")
        followedArtist = createUser("FollowedArtist")
        discoverArtist = createUser("DiscoverArtist")
    }

    @Test
    fun `personalized feed includes posts from followed artists`() {
        val follow = FollowEntity(follower = user, following = followedArtist)
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(listOf(follow))

        val followedPost = createPost(followedArtist, "Followed post", LocalDateTime.now())
        whenever(feedPostRepository.findByAuthorIds(eq(setOf(followedArtist.id)), any()))
            .thenReturn(PageImpl(listOf(followedPost)))
        whenever(feedPostRepository.findDiscoverExcluding(any(), any()))
            .thenReturn(PageImpl(emptyList()))

        val result = feedService.getPersonalizedFeed(user.id, PageRequest.of(0, 20))

        assertTrue(result.content.isNotEmpty())
        assertEquals("Followed post", result.content[0].content)
    }

    @Test
    fun `personalized feed includes discover posts from non-followed artists`() {
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(emptyList())

        val discoverPost = createPost(discoverArtist, "Discover post", LocalDateTime.now())
        whenever(feedPostRepository.findDiscoverExcluding(eq(setOf(user.id)), any()))
            .thenReturn(PageImpl(listOf(discoverPost)))

        val result = feedService.getPersonalizedFeed(user.id, PageRequest.of(0, 20))

        assertTrue(result.content.isNotEmpty())
        assertEquals("Discover post", result.content[0].content)
    }

    @Test
    fun `personalized feed mixes followed and discover posts`() {
        val follow = FollowEntity(follower = user, following = followedArtist)
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(listOf(follow))

        val now = LocalDateTime.now()
        val followedPost = createPost(followedArtist, "From followed", now.minusMinutes(5))
        val discoverPost = createPost(discoverArtist, "From discover", now)

        whenever(feedPostRepository.findByAuthorIds(eq(setOf(followedArtist.id)), any()))
            .thenReturn(PageImpl(listOf(followedPost)))
        whenever(feedPostRepository.findDiscoverExcluding(any(), any()))
            .thenReturn(PageImpl(listOf(discoverPost)))

        val result = feedService.getPersonalizedFeed(user.id, PageRequest.of(0, 20))

        assertEquals(2, result.content.size)
        // Sorted by createdAt DESC, discover post (now) should be first
        assertEquals("From discover", result.content[0].content)
        assertEquals("From followed", result.content[1].content)
    }

    @Test
    fun `personalized feed deduplicates posts`() {
        val follow = FollowEntity(follower = user, following = followedArtist)
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(listOf(follow))

        val post = createPost(followedArtist, "Duplicate post", LocalDateTime.now())

        // Same post returned from both queries (edge case)
        whenever(feedPostRepository.findByAuthorIds(eq(setOf(followedArtist.id)), any()))
            .thenReturn(PageImpl(listOf(post)))
        whenever(feedPostRepository.findDiscoverExcluding(any(), any()))
            .thenReturn(PageImpl(listOf(post)))

        val result = feedService.getPersonalizedFeed(user.id, PageRequest.of(0, 20))

        assertEquals(1, result.content.size)
    }

    @Test
    fun `personalized feed returns empty when no posts exist`() {
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(emptyList())
        whenever(feedPostRepository.findDiscoverExcluding(any(), any()))
            .thenReturn(PageImpl(emptyList()))

        val result = feedService.getPersonalizedFeed(user.id, PageRequest.of(0, 20))

        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `personalized feed excludes user own posts from discover`() {
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(emptyList())

        // User ID should be in the exclude set
        whenever(feedPostRepository.findDiscoverExcluding(eq(setOf(user.id)), any()))
            .thenReturn(PageImpl(emptyList()))

        feedService.getPersonalizedFeed(user.id, PageRequest.of(0, 20))

        verify(feedPostRepository).findDiscoverExcluding(
            argThat { contains(user.id) }, any()
        )
    }

    @Test
    fun `personalized feed excludes followed artist IDs from discover`() {
        val follow = FollowEntity(follower = user, following = followedArtist)
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(listOf(follow))

        whenever(feedPostRepository.findByAuthorIds(any(), any()))
            .thenReturn(PageImpl(emptyList()))
        whenever(feedPostRepository.findDiscoverExcluding(any(), any()))
            .thenReturn(PageImpl(emptyList()))

        feedService.getPersonalizedFeed(user.id, PageRequest.of(0, 20))

        // Discover should exclude both followed artist and user
        verify(feedPostRepository).findDiscoverExcluding(
            argThat { contains(followedArtist.id) && contains(user.id) }, any()
        )
    }

    @Test
    fun `getFeed without auth returns chronological feed`() {
        val post = createPost(discoverArtist, "Public post", LocalDateTime.now())
        whenever(feedPostRepository.findAllByOrderByCreatedAtDesc(any()))
            .thenReturn(PageImpl(listOf(post)))

        val result = feedService.getFeed(PageRequest.of(0, 20))

        assertEquals(1, result.content.size)
        assertEquals("Public post", result.content[0].content)
    }

    // ── Helpers ──

    private fun createUser(name: String) = UserEntity(
        id = UUID.randomUUID(),
        displayName = name,
        username = name.lowercase(),
        email = "${name.lowercase()}@test.com",
        passwordHash = "hash",
        roles = mutableSetOf(ArtistRole.SINGER)
    )

    private fun createPost(
        author: UserEntity,
        content: String,
        createdAt: LocalDateTime
    ): FeedPostEntity {
        val constructor = FeedPostEntity::class.java.getDeclaredConstructor(
            UUID::class.java, FeedPostType::class.java, UserEntity::class.java,
            String::class.java, String::class.java, SongEntity::class.java,
            Long::class.javaPrimitiveType, Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType, Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType, Long::class.javaPrimitiveType,
            LocalDateTime::class.java, LocalDateTime::class.java
        )

        return FeedPostEntity(
            id = UUID.randomUUID(),
            type = FeedPostType.UPDATE,
            author = author,
            content = content
        ).also {
            // Set createdAt via reflection since it's a val
            val field = FeedPostEntity::class.java.getDeclaredField("createdAt")
            field.isAccessible = true
            field.set(it, createdAt)
        }
    }
}
