package com.newzic.service

import com.newzic.domain.entity.*
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
import java.util.*

@ExtendWith(MockitoExtension::class)
class SongServiceTest {

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

    private lateinit var artist: UserEntity
    private lateinit var reactor: UserEntity
    private lateinit var song: SongEntity

    @BeforeEach
    fun setUp() {
        artist = UserEntity(
            id = UUID.randomUUID(), displayName = "Artist", username = "artist",
            email = "artist@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        reactor = UserEntity(
            id = UUID.randomUUID(), displayName = "Fan", username = "fan",
            email = "fan@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        song = SongEntity(
            id = UUID.randomUUID(), title = "Test Song", artist = artist, duration = 200
        )
    }

    @Test
    fun `react should add reaction and increment counter`() {
        whenever(reactionRepository.findByUserIdAndSongIdAndType(reactor.id, song.id, ReactionType.FIRE)).thenReturn(null)
        whenever(songRepository.findById(song.id)).thenReturn(Optional.of(song))
        whenever(userRepository.findById(reactor.id)).thenReturn(Optional.of(reactor))
        whenever(songRepository.save(any<SongEntity>())).thenAnswer { it.arguments[0] }

        val result = songService.react(reactor.id, song.id, "fire")

        assertTrue(result)
        assertEquals(1, song.reactionsFire)
        verify(reactionRepository).save(any<ReactionEntity>())
    }

    @Test
    fun `react should create notification for song artist`() {
        whenever(reactionRepository.findByUserIdAndSongIdAndType(reactor.id, song.id, ReactionType.GEM)).thenReturn(null)
        whenever(songRepository.findById(song.id)).thenReturn(Optional.of(song))
        whenever(userRepository.findById(reactor.id)).thenReturn(Optional.of(reactor))
        whenever(songRepository.save(any<SongEntity>())).thenAnswer { it.arguments[0] }

        songService.react(reactor.id, song.id, "gem")

        verify(notificationService).create(
            eq(artist.id),
            eq(reactor.id),
            eq(NotificationType.REACTION),
            argThat { contains("💎") && contains("Test Song") },
            any(),
            eq(song.id)
        )
    }

    @Test
    fun `react should remove existing reaction and decrement counter`() {
        song.reactionsFire = 3
        val existing = ReactionEntity(user = reactor, song = song, type = ReactionType.FIRE)

        whenever(reactionRepository.findByUserIdAndSongIdAndType(reactor.id, song.id, ReactionType.FIRE)).thenReturn(existing)
        whenever(songRepository.findById(song.id)).thenReturn(Optional.of(song))
        whenever(songRepository.save(any<SongEntity>())).thenAnswer { it.arguments[0] }

        val result = songService.react(reactor.id, song.id, "fire")

        assertFalse(result)
        assertEquals(2, song.reactionsFire)
        verify(reactionRepository).delete(existing)
    }

    @Test
    fun `removing reaction should not create notification`() {
        song.reactionsFire = 1
        val existing = ReactionEntity(user = reactor, song = song, type = ReactionType.FIRE)

        whenever(reactionRepository.findByUserIdAndSongIdAndType(reactor.id, song.id, ReactionType.FIRE)).thenReturn(existing)
        whenever(songRepository.findById(song.id)).thenReturn(Optional.of(song))
        whenever(songRepository.save(any<SongEntity>())).thenAnswer { it.arguments[0] }

        songService.react(reactor.id, song.id, "fire")

        verify(notificationService, never()).create(any(), anyOrNull(), any(), any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `react with invalid type should throw`() {
        assertThrows(IllegalArgumentException::class.java) {
            songService.react(reactor.id, song.id, "invalid")
        }
    }

    @Test
    fun `react with all reaction types should use correct emoji`() {
        val types = mapOf("fire" to "🔥", "gem" to "💎", "onpoint" to "🎯", "star" to "🌟")

        for ((type, emoji) in types) {
            val reactionType = ReactionType.valueOf(type.uppercase())
            whenever(reactionRepository.findByUserIdAndSongIdAndType(reactor.id, song.id, reactionType)).thenReturn(null)
            whenever(songRepository.findById(song.id)).thenReturn(Optional.of(song))
            whenever(userRepository.findById(reactor.id)).thenReturn(Optional.of(reactor))
            whenever(songRepository.save(any<SongEntity>())).thenAnswer { it.arguments[0] }

            songService.react(reactor.id, song.id, type)

            verify(notificationService).create(
                eq(artist.id),
                eq(reactor.id),
                eq(NotificationType.REACTION),
                argThat { contains(emoji) },
                any(),
                eq(song.id)
            )

            clearInvocations(notificationService, reactionRepository, songRepository, userRepository)
        }
    }

    // ── Lazy audio loading: getById returns audioUrl, list methods do not ──

    @Test
    fun `getById should return song with audioUrl`() {
        song.audioUrl = "data:audio/mp3;base64,AAAA"
        whenever(songRepository.findById(song.id)).thenReturn(Optional.of(song))
        whenever(songMapper.toResponse(song)).thenCallRealMethod()

        val result = songService.getById(song.id)

        assertNotNull(result.audioUrl)
        assertEquals("data:audio/mp3;base64,AAAA", result.audioUrl)
        verify(songMapper).toResponse(song)
        verify(songMapper, never()).toListResponse(any())
    }

    @Test
    fun `getByArtist should return songs without audioUrl`() {
        song.audioUrl = "data:audio/mp3;base64,AAAA"
        val page = org.springframework.data.domain.PageImpl(listOf(song))
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        whenever(songRepository.findByArtistId(artist.id, pageable)).thenReturn(page)
        whenever(songMapper.toListResponse(song)).thenCallRealMethod()

        val result = songService.getByArtist(artist.id, pageable)

        assertNull(result.content[0].audioUrl)
        verify(songMapper).toListResponse(song)
        verify(songMapper, never()).toResponse(any())
    }

    @Test
    fun `getTrending should return songs without audioUrl`() {
        song.audioUrl = "data:audio/mp3;base64,AAAA"
        val page = org.springframework.data.domain.PageImpl(listOf(song))
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        whenever(songRepository.findTrending(pageable)).thenReturn(page)
        whenever(songMapper.toListResponse(song)).thenCallRealMethod()

        val result = songService.getTrending(pageable)

        assertNull(result.content[0].audioUrl)
        verify(songMapper).toListResponse(song)
        verify(songMapper, never()).toResponse(any())
    }

    @Test
    fun `getNewReleases should return songs without audioUrl`() {
        song.audioUrl = "data:audio/mp3;base64,AAAA"
        val page = org.springframework.data.domain.PageImpl(listOf(song))
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        whenever(songRepository.findNewReleases(pageable)).thenReturn(page)
        whenever(songMapper.toListResponse(song)).thenCallRealMethod()

        val result = songService.getNewReleases(pageable)

        assertNull(result.content[0].audioUrl)
        verify(songMapper).toListResponse(song)
        verify(songMapper, never()).toResponse(any())
    }

    @Test
    fun `search should return songs without audioUrl`() {
        song.audioUrl = "data:audio/mp3;base64,AAAA"
        val page = org.springframework.data.domain.PageImpl(listOf(song))
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10)
        whenever(songRepository.search("test", pageable)).thenReturn(page)
        whenever(songMapper.toListResponse(song)).thenCallRealMethod()

        val result = songService.search("test", pageable)

        assertNull(result.content[0].audioUrl)
        verify(songMapper).toListResponse(song)
        verify(songMapper, never()).toResponse(any())
    }
}

// ── SongMapper unit tests ──
@ExtendWith(MockitoExtension::class)
class SongMapperTest {

    private val mapper = SongMapper()

    private lateinit var artist: UserEntity
    private lateinit var song: SongEntity

    @BeforeEach
    fun setUp() {
        artist = UserEntity(
            id = UUID.randomUUID(), displayName = "TestArtist", username = "testartist",
            email = "test@test.com", passwordHash = "h", roles = mutableSetOf(ArtistRole.SINGER)
        )
        song = SongEntity(
            id = UUID.randomUUID(), title = "Mapper Song", artist = artist,
            duration = 180, audioUrl = "data:audio/mp3;base64,AAAA"
        )
    }

    @Test
    fun `toResponse should include audioUrl`() {
        val result = mapper.toResponse(song)

        assertEquals(song.id.toString(), result.id)
        assertEquals("Mapper Song", result.title)
        assertEquals("data:audio/mp3;base64,AAAA", result.audioUrl)
    }

    @Test
    fun `toListResponse should exclude audioUrl`() {
        val result = mapper.toListResponse(song)

        assertEquals(song.id.toString(), result.id)
        assertEquals("Mapper Song", result.title)
        assertNull(result.audioUrl)
    }

    @Test
    fun `toResponse and toListResponse should have same metadata`() {
        val full = mapper.toResponse(song)
        val list = mapper.toListResponse(song)

        assertEquals(full.id, list.id)
        assertEquals(full.title, list.title)
        assertEquals(full.artistId, list.artistId)
        assertEquals(full.artistName, list.artistName)
        assertEquals(full.cover, list.cover)
        assertEquals(full.duration, list.duration)
        assertEquals(full.genre, list.genre)
        assertEquals(full.plays, list.plays)
        assertEquals(full.likes, list.likes)
        assertEquals(full.reactions, list.reactions)
        assertEquals(full.isExplicit, list.isExplicit)
        // Only audioUrl differs
        assertNotNull(full.audioUrl)
        assertNull(list.audioUrl)
    }
}
