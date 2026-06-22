package com.newzic.service

import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.FollowEntity
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.FollowRepository
import com.newzic.domain.repository.PlayEventRepository
import com.newzic.domain.repository.ReactionRepository
import com.newzic.domain.repository.SongLikeRepository
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
class RecommendationServiceTest {

    @Mock private lateinit var followRepository: FollowRepository
    @Mock private lateinit var songLikeRepository: SongLikeRepository
    @Mock private lateinit var reactionRepository: ReactionRepository
    @Mock private lateinit var playEventRepository: PlayEventRepository

    @InjectMocks
    private lateinit var recommendationService: RecommendationService

    private lateinit var user: UserEntity

    @BeforeEach
    fun setUp() {
        user = UserEntity(
            id = UUID.randomUUID(),
            displayName = "Test User",
            username = "testuser",
            email = "test@test.com",
            passwordHash = "hash",
            roles = mutableSetOf(ArtistRole.SINGER),
            country = "IT",
            preferredGenres = mutableSetOf("Trap", "House")
        )
    }

    // ── Explicit preferences ──

    @Test
    fun `taste profile includes explicit preferred genres with high score`() {
        stubEmptyInteractions()

        val profile = recommendationService.buildTasteProfile(user)

        assertTrue(profile.genreScores.containsKey("Trap"))
        assertTrue(profile.genreScores.containsKey("House"))
        assertEquals(50.0, profile.genreScores["Trap"])
        assertEquals(50.0, profile.genreScores["House"])
    }

    @Test
    fun `taste profile includes user country with high score`() {
        stubEmptyInteractions()

        val profile = recommendationService.buildTasteProfile(user)

        assertTrue(profile.countryScores.containsKey("IT"))
        assertEquals(50.0, profile.countryScores["IT"])
    }

    @Test
    fun `topGenres returns genres sorted by score descending`() {
        stubEmptyInteractions()

        val profile = recommendationService.buildTasteProfile(user)

        assertTrue(profile.topGenres.contains("Trap"))
        assertTrue(profile.topGenres.contains("House"))
    }

    // ── Follow signals ──

    @Test
    fun `taste profile includes genres from followed artists`() {
        val followedArtist = createArtist("Producer", genres = mutableSetOf("Trap", "Drill"), country = "US")
        val follow = FollowEntity(follower = user, following = followedArtist)

        whenever(followRepository.findByFollowerId(user.id)).thenReturn(listOf(follow))
        stubEmptyLikes()
        stubEmptyReactions()
        stubEmptyPlays()

        val profile = recommendationService.buildTasteProfile(user)

        // Trap: 50 (preferred) + 30 (follow) = 80
        assertEquals(80.0, profile.genreScores["Trap"])
        // Drill: 30 (follow only)
        assertEquals(30.0, profile.genreScores["Drill"])
    }

    @Test
    fun `taste profile includes countries from followed artists`() {
        val followedArtist = createArtist("USArtist", country = "US")
        val follow = FollowEntity(follower = user, following = followedArtist)

        whenever(followRepository.findByFollowerId(user.id)).thenReturn(listOf(follow))
        stubEmptyLikes()
        stubEmptyReactions()
        stubEmptyPlays()

        val profile = recommendationService.buildTasteProfile(user)

        // US: 20 (from follow)
        assertEquals(20.0, profile.countryScores["US"])
        // IT: 50 (own country)
        assertEquals(50.0, profile.countryScores["IT"])
    }

    @Test
    fun `followedArtistIds contains IDs of followed artists`() {
        val artist1 = createArtist("A1")
        val artist2 = createArtist("A2")
        val follows = listOf(
            FollowEntity(follower = user, following = artist1),
            FollowEntity(follower = user, following = artist2)
        )

        whenever(followRepository.findByFollowerId(user.id)).thenReturn(follows)
        stubEmptyLikes()
        stubEmptyReactions()
        stubEmptyPlays()

        val profile = recommendationService.buildTasteProfile(user)

        assertEquals(setOf(artist1.id, artist2.id), profile.followedArtistIds)
    }

    // ── Like signals ──

    @Test
    fun `taste profile includes genres from liked songs`() {
        stubEmptyFollows()
        whenever(songLikeRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(
            listOf(
                arrayOf("R&B" as Any, 5L as Any),
                arrayOf("Trap" as Any, 3L as Any)
            )
        )
        stubEmptyReactions()
        stubEmptyPlays()

        val profile = recommendationService.buildTasteProfile(user)

        // R&B: 5 * 20 = 100 (likes only)
        assertEquals(100.0, profile.genreScores["R&B"])
        // Trap: 50 (preferred) + 3 * 20 = 110
        assertEquals(110.0, profile.genreScores["Trap"])
    }

    // ── Reaction signals ──

    @Test
    fun `taste profile includes genres from reactions`() {
        stubEmptyFollows()
        stubEmptyLikes()
        whenever(reactionRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(
            listOf(arrayOf("Lo-Fi" as Any, 4L as Any))
        )
        stubEmptyPlays()

        val profile = recommendationService.buildTasteProfile(user)

        // Lo-Fi: 4 * 15 = 60 (reactions only)
        assertEquals(60.0, profile.genreScores["Lo-Fi"])
    }

    // ── Play history signals ──

    @Test
    fun `taste profile includes genres from play history with logarithmic scaling`() {
        stubEmptyFollows()
        stubEmptyLikes()
        stubEmptyReactions()
        whenever(playEventRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(
            listOf(
                arrayOf("Electronic" as Any, 100L as Any),
                arrayOf("Jazz" as Any, 1L as Any)
            )
        )
        whenever(playEventRepository.findCountryAffinitiesByUserId(user.id)).thenReturn(emptyList())

        val profile = recommendationService.buildTasteProfile(user)

        // Electronic: ln(101) * 10 ≈ 46.15
        val electronicScore = profile.genreScores["Electronic"]!!
        assertTrue(electronicScore > 40.0 && electronicScore < 50.0,
            "Electronic score should be ~46 (ln scaling), was $electronicScore")

        // Jazz: ln(2) * 10 ≈ 6.93
        val jazzScore = profile.genreScores["Jazz"]!!
        assertTrue(jazzScore > 6.0 && jazzScore < 8.0,
            "Jazz score should be ~6.9 (ln scaling), was $jazzScore")
    }

    @Test
    fun `taste profile includes country affinities from play history`() {
        stubEmptyFollows()
        stubEmptyLikes()
        stubEmptyReactions()
        whenever(playEventRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(emptyList())
        whenever(playEventRepository.findCountryAffinitiesByUserId(user.id)).thenReturn(
            listOf(arrayOf("KR" as Any, 20L as Any))
        )

        val profile = recommendationService.buildTasteProfile(user)

        // KR: ln(21) * 8 ≈ 24.36
        val krScore = profile.countryScores["KR"]!!
        assertTrue(krScore > 20.0 && krScore < 30.0,
            "KR country score should be ~24 (ln scaling), was $krScore")
    }

    // ── Combined signals ──

    @Test
    fun `all signals combine additively`() {
        // Follow a Trap artist
        val followedArtist = createArtist("TrapGod", genres = mutableSetOf("Trap"), country = "IT")
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(
            listOf(FollowEntity(follower = user, following = followedArtist))
        )

        // Like 2 Trap songs
        whenever(songLikeRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(
            listOf(arrayOf("Trap" as Any, 2L as Any))
        )

        // React to 1 Trap song
        whenever(reactionRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(
            listOf(arrayOf("Trap" as Any, 1L as Any))
        )

        // Play 10 Trap songs
        whenever(playEventRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(
            listOf(arrayOf("Trap" as Any, 10L as Any))
        )
        whenever(playEventRepository.findCountryAffinitiesByUserId(user.id)).thenReturn(emptyList())

        val profile = recommendationService.buildTasteProfile(user)

        // Trap: 50 (preferred) + 30 (follow) + 40 (2 likes * 20) + 15 (1 reaction * 15) + ln(11)*10 (plays) ≈ 158.97
        val trapScore = profile.genreScores["Trap"]!!
        assertTrue(trapScore > 155.0 && trapScore < 165.0,
            "Trap combined score should be ~159, was $trapScore")
    }

    @Test
    fun `country scores combine from user country and follow country`() {
        val followedArtist = createArtist("ItalianArtist", country = "IT")
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(
            listOf(FollowEntity(follower = user, following = followedArtist))
        )
        stubEmptyLikes()
        stubEmptyReactions()
        stubEmptyPlays()

        val profile = recommendationService.buildTasteProfile(user)

        // IT: 50 (own country) + 20 (from follow) = 70
        assertEquals(70.0, profile.countryScores["IT"])
    }

    // ── Edge cases ──

    @Test
    fun `empty profile for user with no preferences or interactions`() {
        val emptyUser = UserEntity(
            id = UUID.randomUUID(), displayName = "New User", username = "newuser",
            email = "new@test.com", passwordHash = "hash", roles = mutableSetOf(ArtistRole.SINGER)
        )

        stubEmptyInteractionsFor(emptyUser.id)

        val profile = recommendationService.buildTasteProfile(emptyUser)

        assertTrue(profile.genreScores.isEmpty())
        assertTrue(profile.countryScores.isEmpty())
        assertTrue(profile.followedArtistIds.isEmpty())
        assertTrue(profile.topGenres.isEmpty())
        assertTrue(profile.topCountries.isEmpty())
    }

    @Test
    fun `topGenres limited to 10 even with many genres`() {
        val userWithManyGenres = UserEntity(
            id = UUID.randomUUID(), displayName = "Eclectic", username = "eclectic",
            email = "e@test.com", passwordHash = "hash", roles = mutableSetOf(ArtistRole.SINGER),
            preferredGenres = mutableSetOf(
                "G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "G10", "G11", "G12"
            )
        )

        stubEmptyInteractionsFor(userWithManyGenres.id)

        val profile = recommendationService.buildTasteProfile(userWithManyGenres)

        assertTrue(profile.topGenres.size <= 10, "topGenres should be capped at 10")
    }

    @Test
    fun `topCountries limited to 3`() {
        stubEmptyFollows()
        stubEmptyLikes()
        stubEmptyReactions()
        whenever(playEventRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(emptyList())
        whenever(playEventRepository.findCountryAffinitiesByUserId(user.id)).thenReturn(
            listOf(
                arrayOf("US" as Any, 100L as Any),
                arrayOf("KR" as Any, 50L as Any),
                arrayOf("JP" as Any, 30L as Any),
                arrayOf("BR" as Any, 20L as Any),
                arrayOf("FR" as Any, 10L as Any)
            )
        )

        val profile = recommendationService.buildTasteProfile(user)

        assertTrue(profile.topCountries.size <= 3, "topCountries should be capped at 3")
    }

    // ── Helper methods ──

    private fun createArtist(
        name: String,
        genres: MutableSet<String> = mutableSetOf(),
        country: String? = null
    ): UserEntity = UserEntity(
        id = UUID.randomUUID(),
        displayName = name,
        username = name.lowercase().replace(" ", ""),
        email = "${name.lowercase()}@test.com",
        passwordHash = "hash",
        roles = mutableSetOf(ArtistRole.SINGER),
        genres = genres,
        country = country
    )

    private fun stubEmptyInteractions() {
        stubEmptyFollows()
        stubEmptyLikes()
        stubEmptyReactions()
        stubEmptyPlays()
    }

    private fun stubEmptyInteractionsFor(userId: UUID) {
        whenever(followRepository.findByFollowerId(userId)).thenReturn(emptyList())
        whenever(songLikeRepository.findGenreAffinitiesByUserId(userId)).thenReturn(emptyList())
        whenever(reactionRepository.findGenreAffinitiesByUserId(userId)).thenReturn(emptyList())
        whenever(playEventRepository.findGenreAffinitiesByUserId(userId)).thenReturn(emptyList())
        whenever(playEventRepository.findCountryAffinitiesByUserId(userId)).thenReturn(emptyList())
    }

    private fun stubEmptyFollows() {
        whenever(followRepository.findByFollowerId(user.id)).thenReturn(emptyList())
    }

    private fun stubEmptyLikes() {
        whenever(songLikeRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(emptyList())
    }

    private fun stubEmptyReactions() {
        whenever(reactionRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(emptyList())
    }

    private fun stubEmptyPlays() {
        whenever(playEventRepository.findGenreAffinitiesByUserId(user.id)).thenReturn(emptyList())
        whenever(playEventRepository.findCountryAffinitiesByUserId(user.id)).thenReturn(emptyList())
    }
}
