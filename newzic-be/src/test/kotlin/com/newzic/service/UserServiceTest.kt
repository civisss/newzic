package com.newzic.service

import com.newzic.api.dto.UpdateProfileRequest
import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.FollowEntity
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.FollowRepository
import com.newzic.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var followRepository: FollowRepository

    @Mock
    private lateinit var userMapper: UserMapper

    @Mock
    private lateinit var notificationService: NotificationService

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var testUser: UserEntity

    @BeforeEach
    fun setUp() {
        testUser = UserEntity(
            id = UUID.randomUUID(),
            displayName = "Test Artist",
            username = "testartist",
            email = "test@example.com",
            passwordHash = "hashed",
            roles = mutableSetOf(ArtistRole.SINGER),
            preferredLanguage = "en"
        )
    }

    @Test
    fun `updateProfile should update preferredLanguage`() {
        val userId = testUser.id
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(testUser))
        whenever(userRepository.save(any<UserEntity>())).thenReturn(testUser)
        whenever(userMapper.toResponse(any())).thenCallRealMethod()

        val request = UpdateProfileRequest(preferredLanguage = "it")
        userService.updateProfile(userId, request)

        assertEquals("it", testUser.preferredLanguage)
        verify(userRepository).save(testUser)
    }

    @Test
    fun `updateProfile should update country and preferredGenres`() {
        val userId = testUser.id
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(testUser))
        whenever(userRepository.save(any<UserEntity>())).thenReturn(testUser)
        whenever(userMapper.toResponse(any())).thenCallRealMethod()

        val request = UpdateProfileRequest(
            country = "IT",
            preferredGenres = listOf("Trap", "House")
        )
        userService.updateProfile(userId, request)

        assertEquals("IT", testUser.country)
        assertEquals(setOf("Trap", "House"), testUser.preferredGenres)
    }

    @Test
    fun `getById should throw when user not found`() {
        val randomId = UUID.randomUUID()
        whenever(userRepository.findById(randomId)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            userService.getById(randomId)
        }
    }

    @Test
    fun `follow should toggle follow state`() {
        val followerId = UUID.randomUUID()
        val followingId = UUID.randomUUID()

        val follower = UserEntity(
            id = followerId, displayName = "Follower", username = "follower",
            email = "f@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        val following = UserEntity(
            id = followingId, displayName = "Following", username = "following",
            email = "g@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )

        // First follow
        whenever(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false)
        whenever(userRepository.findById(followerId)).thenReturn(Optional.of(follower))
        whenever(userRepository.findById(followingId)).thenReturn(Optional.of(following))
        whenever(followRepository.save(any<FollowEntity>())).thenAnswer { it.arguments[0] }
        whenever(userRepository.save(any<UserEntity>())).thenAnswer { it.arguments[0] }

        val result = userService.follow(followerId, followingId)
        assertTrue(result)
        assertEquals(1, following.followers)
        assertEquals(1, follower.following)
    }

    @Test
    fun `follow self should throw`() {
        val userId = UUID.randomUUID()
        assertThrows(IllegalArgumentException::class.java) {
            userService.follow(userId, userId)
        }
    }

    @Test
    fun `unfollow should decrement counts`() {
        val followerId = UUID.randomUUID()
        val followingId = UUID.randomUUID()

        val follower = UserEntity(
            id = followerId, displayName = "Follower", username = "follower",
            email = "f@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER),
            following = 1
        )
        val following = UserEntity(
            id = followingId, displayName = "Following", username = "following",
            email = "g@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER),
            followers = 1
        )
        val existingFollow = FollowEntity(follower = follower, following = following)

        whenever(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true)
        whenever(followRepository.findByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(existingFollow)
        whenever(userRepository.findById(followerId)).thenReturn(Optional.of(follower))
        whenever(userRepository.findById(followingId)).thenReturn(Optional.of(following))
        whenever(userRepository.save(any<UserEntity>())).thenAnswer { it.arguments[0] }

        val result = userService.follow(followerId, followingId)

        assertFalse(result)
        assertEquals(0, following.followers)
        assertEquals(0, follower.following)
        verify(followRepository).delete(existingFollow)
    }

    @Test
    fun `isFollowing should return true when following`() {
        val followerId = UUID.randomUUID()
        val followingId = UUID.randomUUID()
        whenever(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true)

        assertTrue(userService.isFollowing(followerId, followingId))
    }

    @Test
    fun `isFollowing should return false when not following`() {
        val followerId = UUID.randomUUID()
        val followingId = UUID.randomUUID()
        whenever(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false)

        assertFalse(userService.isFollowing(followerId, followingId))
    }

    @Test
    fun `getFollowers should return list of followers mapped to responses`() {
        val userId = UUID.randomUUID()
        val followerUser = UserEntity(
            id = UUID.randomUUID(), displayName = "Fan", username = "fan",
            email = "fan@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        val followEntity = FollowEntity(follower = followerUser, following = testUser)

        whenever(followRepository.findByFollowingId(userId)).thenReturn(listOf(followEntity))
        whenever(userMapper.toResponse(followerUser)).thenCallRealMethod()

        val result = userService.getFollowers(userId)

        assertEquals(1, result.size)
        assertEquals("Fan", result[0].displayName)
        verify(followRepository).findByFollowingId(userId)
    }

    @Test
    fun `getFollowing should return list of users being followed`() {
        val userId = UUID.randomUUID()
        val followedUser = UserEntity(
            id = UUID.randomUUID(), displayName = "Star", username = "star",
            email = "star@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.PRODUCER)
        )
        val followEntity = FollowEntity(follower = testUser, following = followedUser)

        whenever(followRepository.findByFollowerId(userId)).thenReturn(listOf(followEntity))
        whenever(userMapper.toResponse(followedUser)).thenCallRealMethod()

        val result = userService.getFollowing(userId)

        assertEquals(1, result.size)
        assertEquals("Star", result[0].displayName)
        verify(followRepository).findByFollowerId(userId)
    }

    @Test
    fun `getFollowers should return empty list when no followers`() {
        val userId = UUID.randomUUID()
        whenever(followRepository.findByFollowingId(userId)).thenReturn(emptyList())

        val result = userService.getFollowers(userId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getFollowing should return empty list when not following anyone`() {
        val userId = UUID.randomUUID()
        whenever(followRepository.findByFollowerId(userId)).thenReturn(emptyList())

        val result = userService.getFollowing(userId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `follow should create notification for followed user`() {
        val followerId = UUID.randomUUID()
        val followingId = UUID.randomUUID()

        val follower = UserEntity(
            id = followerId, displayName = "Follower", username = "follower",
            email = "f@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        val following = UserEntity(
            id = followingId, displayName = "Following", username = "following",
            email = "g@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )

        whenever(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false)
        whenever(userRepository.findById(followerId)).thenReturn(Optional.of(follower))
        whenever(userRepository.findById(followingId)).thenReturn(Optional.of(following))
        whenever(followRepository.save(any<FollowEntity>())).thenAnswer { it.arguments[0] }
        whenever(userRepository.save(any<UserEntity>())).thenAnswer { it.arguments[0] }

        userService.follow(followerId, followingId)

        verify(notificationService).create(
            eq(followingId),
            eq(followerId),
            eq(com.newzic.domain.entity.NotificationType.FOLLOW),
            argThat { contains("started following") },
            argThat { contains(followerId.toString()) },
            anyOrNull()
        )
    }

    @Test
    fun `unfollow should not create notification`() {
        val followerId = UUID.randomUUID()
        val followingId = UUID.randomUUID()

        val follower = UserEntity(
            id = followerId, displayName = "Follower", username = "follower",
            email = "f@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER),
            following = 1
        )
        val following = UserEntity(
            id = followingId, displayName = "Following", username = "following",
            email = "g@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER),
            followers = 1
        )
        val existingFollow = FollowEntity(follower = follower, following = following)

        whenever(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true)
        whenever(followRepository.findByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(existingFollow)
        whenever(userRepository.findById(followerId)).thenReturn(Optional.of(follower))
        whenever(userRepository.findById(followingId)).thenReturn(Optional.of(following))
        whenever(userRepository.save(any<UserEntity>())).thenAnswer { it.arguments[0] }

        userService.follow(followerId, followingId)

        verify(notificationService, never()).create(any(), anyOrNull(), any(), any(), anyOrNull(), anyOrNull())
    }
}
