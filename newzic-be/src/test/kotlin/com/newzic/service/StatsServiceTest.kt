package com.newzic.service

import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.SongEntity
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.*
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
class StatsServiceTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var songRepository: SongRepository
    @Mock private lateinit var reactionRepository: ReactionRepository
    @Mock private lateinit var playEventRepository: PlayEventRepository
    @Mock private lateinit var followRepository: FollowRepository

    @InjectMocks
    private lateinit var statsService: StatsService

    private lateinit var artist: UserEntity

    @BeforeEach
    fun setUp() {
        artist = UserEntity(
            id = UUID.randomUUID(), displayName = "Artist", username = "artist",
            email = "artist@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER),
            totalPlays = 1000
        )
    }

    @Test
    fun `getArtistStats should sum reactions from all songs`() {
        val song1 = SongEntity(title = "Song 1", artist = artist, duration = 200).apply {
            reactionsFire = 10; reactionsGem = 5; reactionsOnpoint = 3; reactionsStar = 2
        }
        val song2 = SongEntity(title = "Song 2", artist = artist, duration = 180).apply {
            reactionsFire = 7; reactionsGem = 3; reactionsOnpoint = 1; reactionsStar = 4
        }

        whenever(userRepository.findById(artist.id)).thenReturn(Optional.of(artist))
        whenever(songRepository.countByArtistId(artist.id)).thenReturn(2)
        whenever(songRepository.findByArtistId(artist.id)).thenReturn(listOf(song1, song2))
        whenever(followRepository.countByFollowingId(artist.id)).thenReturn(50)
        whenever(followRepository.countByFollowerId(artist.id)).thenReturn(10)
        whenever(playEventRepository.countByArtistIdSince(eq(artist.id), any())).thenReturn(0)
        whenever(playEventRepository.findTopCitiesByArtist(artist.id)).thenReturn(emptyList())

        val stats = statsService.getArtistStats(artist.id)

        assertEquals(35, stats.totalReactions) // 10+5+3+2 + 7+3+1+4 = 35
        assertEquals(2, stats.totalSongs)
        assertEquals(50, stats.totalFollowers)
        assertEquals(10, stats.totalFollowing)
        assertEquals(1000, stats.totalPlays)
    }

    @Test
    fun `getArtistStats should return 0 reactions when no songs`() {
        whenever(userRepository.findById(artist.id)).thenReturn(Optional.of(artist))
        whenever(songRepository.countByArtistId(artist.id)).thenReturn(0)
        whenever(songRepository.findByArtistId(artist.id)).thenReturn(emptyList())
        whenever(followRepository.countByFollowingId(artist.id)).thenReturn(0)
        whenever(followRepository.countByFollowerId(artist.id)).thenReturn(0)
        whenever(playEventRepository.countByArtistIdSince(eq(artist.id), any())).thenReturn(0)
        whenever(playEventRepository.findTopCitiesByArtist(artist.id)).thenReturn(emptyList())

        val stats = statsService.getArtistStats(artist.id)

        assertEquals(0, stats.totalReactions)
    }

    @Test
    fun `getArtistStats should throw when user not found`() {
        val randomId = UUID.randomUUID()
        whenever(userRepository.findById(randomId)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            statsService.getArtistStats(randomId)
        }
    }

    @Test
    fun `getArtistStats should calculate growth percent`() {
        whenever(userRepository.findById(artist.id)).thenReturn(Optional.of(artist))
        whenever(songRepository.countByArtistId(artist.id)).thenReturn(0)
        whenever(songRepository.findByArtistId(artist.id)).thenReturn(emptyList())
        whenever(followRepository.countByFollowingId(artist.id)).thenReturn(0)
        whenever(followRepository.countByFollowerId(artist.id)).thenReturn(0)
        // playsThisWeek=150, playsLastWeek=100 => growth=50%
        whenever(playEventRepository.countByArtistIdSince(eq(artist.id), any()))
            .thenReturn(150L)  // this week
            .thenReturn(250L)  // last 14 days (250-150=100 last week)
        whenever(playEventRepository.findTopCitiesByArtist(artist.id)).thenReturn(emptyList())

        val stats = statsService.getArtistStats(artist.id)

        assertEquals(50.0, stats.growthPercent)
        assertEquals("up", stats.playsTrend)
    }
}
