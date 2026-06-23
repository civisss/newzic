package com.newzic.service

import com.newzic.api.dto.SongResponse
import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.SongEntity
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.AlbumRepository
import com.newzic.domain.repository.ReactionRepository
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
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
class SongRecommendationTest {

    @Mock private lateinit var songRepository: SongRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var albumRepository: AlbumRepository
    @Mock private lateinit var reactionRepository: ReactionRepository
    @Mock private lateinit var songMapper: SongMapper
    @Mock private lateinit var notificationService: NotificationService
    @Mock private lateinit var recommendationService: RecommendationService
    @Mock private lateinit var premiumService: PremiumService

    @InjectMocks
    private lateinit var songService: SongService

    private lateinit var user: UserEntity
    private lateinit var otherArtist: UserEntity
    private lateinit var sameCountryArtist: UserEntity

    @BeforeEach
    fun setUp() {
        user = createUser("User", country = "IT", preferredGenres = mutableSetOf("Trap", "House"))
        otherArtist = createUser("OtherArtist", country = "US", genres = mutableSetOf("Trap"))
        sameCountryArtist = createUser("ItalianArtist", country = "IT", genres = mutableSetOf("Pop"))
    }

    private fun stubSongMapper() {
        whenever(songMapper.toListResponse(any())).thenAnswer { invocation ->
            val song = invocation.arguments[0] as SongEntity
            SongResponse(
                id = song.id.toString(),
                title = song.title,
                artistId = song.artist.id.toString(),
                artistName = song.artist.displayName,
                artistAvatar = song.artist.avatar,
                albumId = null,
                albumName = null,
                cover = song.cover,
                duration = song.duration,
                genre = song.genre,
                tags = song.tags.toList(),
                releaseDate = song.releaseDate.toString(),
                plays = song.plays,
                likes = song.likes,
                reactions = com.newzic.api.dto.ReactionsDto(
                    fire = song.reactionsFire,
                    gem = song.reactionsGem,
                    onpoint = song.reactionsOnpoint,
                    star = song.reactionsStar
                ),
                audioUrl = null,
                isExplicit = song.isExplicit
            )
        }
    }

    @Test
    fun `getRecommended uses taste profile genres and country`() {
        stubSongMapper()
        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 80.0, "House" to 50.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = emptySet()
        )
        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)

        val trapSong = createSong("Trap Song", otherArtist, genre = "Trap", plays = 10000)
        val popSong = createSong("Pop Song", sameCountryArtist, genre = "Pop", plays = 5000)

        whenever(songRepository.findRecommendationCandidates(eq(user.id), any(), eq("IT"), any()))
            .thenReturn(PageImpl(listOf(trapSong, popSong)))

        val result = songService.getRecommended(user.id, 10)

        assertEquals(2, result.size)
        // Trap song should rank higher (genre score 80 > Pop score 0)
        assertEquals("Trap Song", result[0].title)
    }

    @Test
    fun `getRecommended boosts songs from followed artists`() {
        stubSongMapper()
        val followedArtist = createUser("FollowedArtist", country = "US", genres = mutableSetOf("Jazz"))
        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Jazz" to 30.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = setOf(followedArtist.id)
        )
        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)

        val followedSong = createSong("Followed Jazz", followedArtist, genre = "Jazz", plays = 100)
        val otherSong = createSong("Other Jazz", otherArtist, genre = "Jazz", plays = 50000)

        whenever(songRepository.findRecommendationCandidates(eq(user.id), any(), eq("IT"), any()))
            .thenReturn(PageImpl(listOf(followedSong, otherSong)))

        val result = songService.getRecommended(user.id, 10)

        assertEquals(2, result.size)
        // Followed song gets +25 boost, should rank higher despite fewer plays
        assertEquals("Followed Jazz", result[0].title)
    }

    @Test
    fun `getRecommended boosts fresh songs`() {
        stubSongMapper()
        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 50.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = emptySet()
        )
        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)

        val newSong = createSong("New Trap", otherArtist, genre = "Trap", plays = 100,
            releaseDate = LocalDate.now().minusDays(5))
        val oldSong = createSong("Old Trap", otherArtist, genre = "Trap", plays = 100,
            releaseDate = LocalDate.now().minusDays(200))

        whenever(songRepository.findRecommendationCandidates(eq(user.id), any(), eq("IT"), any()))
            .thenReturn(PageImpl(listOf(newSong, oldSong)))

        val result = songService.getRecommended(user.id, 10)

        assertEquals(2, result.size)
        // New song gets +8 freshness boost
        assertEquals("New Trap", result[0].title)
    }

    @Test
    fun `getRecommended excludes zero-score songs`() {
        val emptyUser = createUser("EmptyUser", country = null)
        val profile = RecommendationService.TasteProfile(
            genreScores = emptyMap(),
            countryScores = emptyMap(),
            followedArtistIds = emptySet()
        )
        whenever(userRepository.findById(emptyUser.id)).thenReturn(Optional.of(emptyUser))
        whenever(recommendationService.buildTasteProfile(emptyUser)).thenReturn(profile)

        // No genres, no country → fallback to trending
        val zeroScoreSong = createSong("Unknown Genre", otherArtist, genre = "Polka", plays = 0,
            releaseDate = LocalDate.now().minusDays(200))

        whenever(songRepository.findTrending(any()))
            .thenReturn(PageImpl(listOf(zeroScoreSong)))

        val result = songService.getRecommended(emptyUser.id, 10)

        // Song has 0 plays and no genre/country match → score 0 → filtered out
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getRecommended limits results to requested size`() {
        stubSongMapper()
        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 80.0),
            countryScores = mapOf("IT" to 50.0),
            followedArtistIds = emptySet()
        )
        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        whenever(recommendationService.buildTasteProfile(user)).thenReturn(profile)

        val songs = (1..20).map { createSong("Song $it", otherArtist, genre = "Trap", plays = 1000L * it) }

        whenever(songRepository.findRecommendationCandidates(eq(user.id), any(), eq("IT"), any()))
            .thenReturn(PageImpl(songs))

        val result = songService.getRecommended(user.id, 5)

        assertEquals(5, result.size)
    }

    @Test
    fun `getRecommended falls back to genre-only when no country`() {
        stubSongMapper()
        val userNoCountry = createUser("NoCountry", country = null, preferredGenres = mutableSetOf("Trap"))
        val profile = RecommendationService.TasteProfile(
            genreScores = mapOf("Trap" to 50.0),
            countryScores = emptyMap(),
            followedArtistIds = emptySet()
        )
        whenever(userRepository.findById(userNoCountry.id)).thenReturn(Optional.of(userNoCountry))
        whenever(recommendationService.buildTasteProfile(userNoCountry)).thenReturn(profile)

        val song = createSong("Trap Song", otherArtist, genre = "Trap", plays = 1000)
        whenever(songRepository.findByGenresExcludingUser(eq(userNoCountry.id), any(), any()))
            .thenReturn(PageImpl(listOf(song)))

        val result = songService.getRecommended(userNoCountry.id, 10)

        assertEquals(1, result.size)
        verify(songRepository).findByGenresExcludingUser(eq(userNoCountry.id), any(), any())
        verify(songRepository, never()).findRecommendationCandidates(any(), any(), any(), any())
    }

    @Test
    fun `getRecommended falls back to trending when no genres and no country`() {
        val emptyUser = createUser("EmptyUser", country = null)
        val profile = RecommendationService.TasteProfile(
            genreScores = emptyMap(),
            countryScores = emptyMap(),
            followedArtistIds = emptySet()
        )
        whenever(userRepository.findById(emptyUser.id)).thenReturn(Optional.of(emptyUser))
        whenever(recommendationService.buildTasteProfile(emptyUser)).thenReturn(profile)

        val song = createSong("Trending Hit", otherArtist, genre = "Pop", plays = 100000)
        whenever(songRepository.findTrending(any())).thenReturn(PageImpl(listOf(song)))

        songService.getRecommended(emptyUser.id, 10)

        verify(songRepository).findTrending(any())
        verify(songRepository, never()).findRecommendationCandidates(any(), any(), any(), any())
    }

    @Test
    fun `getRecommended throws when user not found`() {
        val randomId = UUID.randomUUID()
        whenever(userRepository.findById(randomId)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            songService.getRecommended(randomId)
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

    private fun createSong(
        title: String,
        artist: UserEntity,
        genre: String? = null,
        plays: Long = 0,
        releaseDate: LocalDate = LocalDate.now().minusDays(60)
    ) = SongEntity(
        id = UUID.randomUUID(),
        title = title,
        artist = artist,
        genre = genre,
        plays = plays,
        releaseDate = releaseDate
    )
}
