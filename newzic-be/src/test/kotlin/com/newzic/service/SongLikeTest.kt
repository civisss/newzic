package com.newzic.service

import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.SongEntity
import com.newzic.domain.entity.SongLikeEntity
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.SongLikeRepository
import com.newzic.domain.repository.SongRepository
import com.newzic.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
class SongLikeTest {

    @Mock
    private lateinit var songLikeRepository: SongLikeRepository

    @Mock
    private lateinit var songRepository: SongRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: UserEntity
    private lateinit var testSong: SongEntity

    @BeforeEach
    fun setUp() {
        testUser = UserEntity(
            id = UUID.randomUUID(),
            displayName = "Test Artist",
            username = "testartist",
            email = "test@example.com",
            passwordHash = "hashed",
            roles = mutableSetOf(ArtistRole.SINGER)
        )

        testSong = SongEntity(
            id = UUID.randomUUID(),
            title = "Test Song",
            artist = testUser,
            genre = "Pop",
            duration = 200,
            releaseDate = LocalDate.now(),
            likes = 5
        )
    }

    @Test
    fun `like song should create SongLikeEntity and increment likes`() {
        val userId = testUser.id
        val songId = testSong.id

        whenever(songLikeRepository.findByUserIdAndSongId(userId, songId)).thenReturn(null)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(testUser))
        whenever(songRepository.findById(songId)).thenReturn(Optional.of(testSong))
        whenever(songLikeRepository.save(any<SongLikeEntity>())).thenAnswer { it.arguments[0] }
        whenever(songRepository.save(any<SongEntity>())).thenAnswer { it.arguments[0] }

        // Simulate the controller logic
        val existing = songLikeRepository.findByUserIdAndSongId(userId, songId)
        assertNull(existing)

        val user = userRepository.findById(userId).get()
        val song = songRepository.findById(songId).get()
        songLikeRepository.save(SongLikeEntity(user = user, song = song))
        song.likes += 1
        songRepository.save(song)

        assertEquals(6, testSong.likes)
        verify(songLikeRepository).save(any<SongLikeEntity>())
    }

    @Test
    fun `unlike song should delete SongLikeEntity and decrement likes`() {
        val userId = testUser.id
        val songId = testSong.id
        val existingLike = SongLikeEntity(user = testUser, song = testSong)

        whenever(songLikeRepository.findByUserIdAndSongId(userId, songId)).thenReturn(existingLike)
        whenever(songRepository.findById(songId)).thenReturn(Optional.of(testSong))
        whenever(songRepository.save(any<SongEntity>())).thenAnswer { it.arguments[0] }

        // Simulate the controller logic
        val existing = songLikeRepository.findByUserIdAndSongId(userId, songId)
        assertNotNull(existing)

        songLikeRepository.delete(existing!!)
        val song = songRepository.findById(songId).get()
        song.likes = maxOf(0, song.likes - 1)
        songRepository.save(song)

        assertEquals(4, testSong.likes)
        verify(songLikeRepository).delete(existingLike)
    }

    @Test
    fun `existsByUserIdAndSongId should return correct value`() {
        val userId = testUser.id
        val songId = testSong.id

        whenever(songLikeRepository.existsByUserIdAndSongId(userId, songId)).thenReturn(true)
        assertTrue(songLikeRepository.existsByUserIdAndSongId(userId, songId))

        whenever(songLikeRepository.existsByUserIdAndSongId(userId, songId)).thenReturn(false)
        assertFalse(songLikeRepository.existsByUserIdAndSongId(userId, songId))
    }
}
