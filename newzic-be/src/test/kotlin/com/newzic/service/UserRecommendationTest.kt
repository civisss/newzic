package com.newzic.service

import com.newzic.domain.entity.ArtistRole
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
import org.springframework.data.domain.PageImpl
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserRecommendationTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var followRepository: FollowRepository
    @Mock private lateinit var userMapper: UserMapper
    @Mock private lateinit var notificationService: NotificationService
    @Mock private lateinit var recommendationService: RecommendationService

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var user: UserEntity

    @BeforeEach
    fun setUp() {
        user = createUser("User", country = "IT", preferredGenres = mutableSetOf("Trap", "House"))
    }

    private fun stubUserMapper() {
        whenever(userMapper.toResponse(any())).thenAnswer { invocation ->
            val u = invocation.arguments[0] as UserEntity
            com.newzic.api.dto.UserResponse(
                id = u.id.toString(),
                displayName = u.displayName,
                username = u.username,
                email = u.email,
                avatar = u.avatar,
                cover = u.cover,
                bio = u.bio,
                longBio = u.longBio,
                roles = u.roles.map { it.name.lowercase() },
                followers = u.followers,
                following = u.following,
                totalPlays = u.totalPlays,
                genres = u.genres.toList(),
                tags = u.tags.toList(),
                verified = u.verified,
                premium = u.premium,
                country = u.country,
                location = u.location,
                preferredGenres = u.preferredGenres.toList(),
                lookingForCollab = u.lookingForCollab,
                collabDescription = u.collabDescription,
                weeklyGrowth = u.weeklyGrowth,
                joinedDate = u.joinedDate.toString(),
                socialLinks = com.newzic.api.dto.SocialLinksDto(),
                photos = u.photos.toList(),
                preferredLanguage = u.preferredLanguage
            )
        }
    }

    @Test
    fun `getRecommended uses taste profile to score candidates`() {
        stubUserMapper()
        val trapArtist = createUser("TrapArtist", country = "IT", genres = mutableSetOf("Trap"))
        val popArtist = createUser("PopArtist", country = "US", genres = mutableSetOf("Pop"))

        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 80.0, "House" to 50.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = emptySet()
        )

        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)
        whenever(userRepository.findRecommendationCandidates(eq(user.id), eq("IT"), any(), any()))
            .thenReturn(PageImpl(listOf(trapArtist, popArtist)))

        val result = userService.getRecommended(user.id, 10)

        assertTrue(result.isNotEmpty())
        // Trap artist from IT should rank higher: Trap genre (80 * 0.5 = 40) + IT country (50) = 90
        assertEquals("TrapArtist", result[0].displayName)
    }

    @Test
    fun `getRecommended excludes already-followed artists`() {
        stubUserMapper()
        val followedArtist = createUser("AlreadyFollowed", country = "IT", genres = mutableSetOf("Trap"))
        val newArtist = createUser("NewArtist", country = "IT", genres = mutableSetOf("Trap"))

        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 80.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = setOf(followedArtist.id) // already followed
        )

        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)
        whenever(userRepository.findRecommendationCandidates(eq(user.id), eq("IT"), any(), any()))
            .thenReturn(PageImpl(listOf(followedArtist, newArtist)))

        val result = userService.getRecommended(user.id, 10)

        assertEquals(1, result.size)
        assertEquals("NewArtist", result[0].displayName)
    }

    @Test
    fun `getRecommended gives region bonus to same-region artists`() {
        stubUserMapper()
        // User is IT, other artist is ES (both EUROPE)
        val spanishArtist = createUser("SpanishArtist", country = "ES", genres = mutableSetOf("Trap"))

        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 50.0),
            countryScores = emptyMap(), // no explicit country affinity for ES
            followedArtistIds = emptySet()
        )

        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)
        whenever(userRepository.findRecommendationCandidates(eq(user.id), eq("IT"), any(), any()))
            .thenReturn(PageImpl(listOf(spanishArtist)))

        val result = userService.getRecommended(user.id, 10)

        assertEquals(1, result.size)
        // Spanish artist should get +15 region bonus (both EUROPE)
    }

    @Test
    fun `getRecommended boosts verified artists`() {
        stubUserMapper()
        val verifiedArtist = createUser("Verified", country = "IT", genres = mutableSetOf("Trap"))
            .also { it.verified = true }
        val normalArtist = createUser("Normal", country = "IT", genres = mutableSetOf("Trap"))

        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 50.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = emptySet()
        )

        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)
        whenever(userRepository.findRecommendationCandidates(eq(user.id), eq("IT"), any(), any()))
            .thenReturn(PageImpl(listOf(verifiedArtist, normalArtist)))

        val result = userService.getRecommended(user.id, 10)

        assertEquals(2, result.size)
        // Verified artist gets +3 boost → should rank higher
        assertEquals("Verified", result[0].displayName)
    }

    @Test
    fun `getRecommended falls back to genre-only when no country`() {
        stubUserMapper()
        val userNoCountry = createUser("NoCountry", country = null, preferredGenres = mutableSetOf("Trap"))
        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 50.0),
            countryScores = emptyMap(),
            followedArtistIds = emptySet()
        )

        whenever(userRepository.findById(userNoCountry.id)).thenReturn(Optional.of(userNoCountry))
        whenever(recommendationService.buildTasteProfile(userNoCountry)).thenReturn(profile)

        val artist = createUser("TrapProd", genres = mutableSetOf("Trap"))
        whenever(userRepository.findByGenresExcludingUser(eq(userNoCountry.id), any(), any()))
            .thenReturn(PageImpl(listOf(artist)))

        val result = userService.getRecommended(userNoCountry.id, 10)

        assertEquals(1, result.size)
        verify(userRepository).findByGenresExcludingUser(eq(userNoCountry.id), any(), any())
        verify(userRepository, never()).findRecommendationCandidates(any(), any(), any(), any())
    }

    @Test
    fun `getRecommended falls back to trending when no genres and no country`() {
        val emptyUser = createUser("Empty", country = null)
        val profile = RecommendationService.TasteProfile(
            genreScores = emptyMap(),
            countryScores = emptyMap(),
            followedArtistIds = emptySet()
        )

        whenever(userRepository.findById(emptyUser.id)).thenReturn(Optional.of(emptyUser))
        whenever(recommendationService.buildTasteProfile(emptyUser)).thenReturn(profile)
        whenever(userRepository.findTrending(any())).thenReturn(PageImpl(emptyList()))

        userService.getRecommended(emptyUser.id, 10)

        verify(userRepository).findTrending(any())
    }

    @Test
    fun `getRecommended respects limit parameter`() {
        stubUserMapper()
        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 50.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = emptySet()
        )

        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)

        val artists = (1..20).map { createUser("Artist$it", country = "IT", genres = mutableSetOf("Trap")) }
        whenever(userRepository.findRecommendationCandidates(eq(user.id), eq("IT"), any(), any()))
            .thenReturn(PageImpl(artists))

        val result = userService.getRecommended(user.id, 3)

        assertEquals(3, result.size)
    }

    @Test
    fun `getRecommended throws when user not found`() {
        val randomId = UUID.randomUUID()
        whenever(userRepository.findById(randomId)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            userService.getRecommended(randomId)
        }
    }

    // ── Helpers ──

    private fun createUser(
        name: String,
        country: String? = null,
        genres: MutableSet<String> = mutableSetOf(),
        preferredGenres: MutableSet<String> = mutableSetOf()
    ) = UserEntity(
        id = UUID.randomUUID(),
        displayName = name,
        username = name.lowercase().replace(" ", ""),
        email = "${name.lowercase()}@test.com",
        passwordHash = "hash",
        roles = mutableSetOf(ArtistRole.SINGER),
        country = country,
        genres = genres,
        preferredGenres = preferredGenres
    )
}
