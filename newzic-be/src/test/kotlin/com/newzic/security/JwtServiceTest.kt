package com.newzic.security

import com.newzic.config.JwtProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtServiceTest {

    private lateinit var jwtService: JwtService

    @BeforeEach
    fun setUp() {
        val props = JwtProperties(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha",
            expirationMs = 3600000 // 1 hour
        )
        jwtService = JwtService(props)
    }

    @Test
    fun `tokens for different users are different`() {
        val userA = UUID.randomUUID().toString()
        val userB = UUID.randomUUID().toString()

        val tokenA = jwtService.generateToken(userA, "alice", listOf("SINGER"), "alice@test.com")
        val tokenB = jwtService.generateToken(userB, "bob", listOf("PRODUCER"), "bob@test.com")

        assertNotEquals(tokenA, tokenB)
    }

    @Test
    fun `token contains correct userId as subject`() {
        val userId = UUID.randomUUID().toString()
        val token = jwtService.generateToken(userId, "testuser")

        assertEquals(userId, jwtService.getUserIdFromToken(token))
    }

    @Test
    fun `token contains correct username claim`() {
        val token = jwtService.generateToken(UUID.randomUUID().toString(), "myartist")

        assertEquals("myartist", jwtService.getUsernameFromToken(token))
    }

    @Test
    fun `token contains roles claim`() {
        val roles = listOf("SINGER", "PRODUCER")
        val token = jwtService.generateToken(UUID.randomUUID().toString(), "user1", roles)

        assertEquals(roles, jwtService.getRolesFromToken(token))
    }

    @Test
    fun `token contains email claim when provided`() {
        val token = jwtService.generateToken(
            UUID.randomUUID().toString(), "user1", emptyList(), "user@example.com"
        )

        assertEquals("user@example.com", jwtService.getEmailFromToken(token))
    }

    @Test
    fun `token email is null when not provided`() {
        val token = jwtService.generateToken(UUID.randomUUID().toString(), "user1")

        assertNull(jwtService.getEmailFromToken(token))
    }

    @Test
    fun `token is valid immediately after generation`() {
        val token = jwtService.generateToken(UUID.randomUUID().toString(), "user1")

        assertTrue(jwtService.isTokenValid(token))
    }

    @Test
    fun `expired token is invalid`() {
        val props = JwtProperties(
            secret = "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha",
            expirationMs = -1000 // already expired
        )
        val expiredJwtService = JwtService(props)

        val token = expiredJwtService.generateToken(UUID.randomUUID().toString(), "user1")

        assertFalse(expiredJwtService.isTokenValid(token))
    }

    @Test
    fun `tampered token is invalid`() {
        val token = jwtService.generateToken(UUID.randomUUID().toString(), "user1")
        val tampered = token.dropLast(3) + "xyz"

        assertFalse(jwtService.isTokenValid(tampered))
    }

    @Test
    fun `same user logged in twice gets different tokens`() {
        val userId = UUID.randomUUID().toString()

        val token1 = jwtService.generateToken(userId, "sameuser", listOf("SINGER"), "same@test.com")
        Thread.sleep(1100) // JWT timestamps have second precision
        val token2 = jwtService.generateToken(userId, "sameuser", listOf("SINGER"), "same@test.com")

        assertNotEquals(token1, token2)
        // But both contain the same user info
        assertEquals(jwtService.getUserIdFromToken(token1), jwtService.getUserIdFromToken(token2))
        assertEquals(jwtService.getUsernameFromToken(token1), jwtService.getUsernameFromToken(token2))
    }

    @Test
    fun `token signed with different secret is rejected`() {
        val otherProps = JwtProperties(
            secret = "different-secret-key-also-at-least-256-bits-long-for-hmac-sha",
            expirationMs = 3600000
        )
        val otherService = JwtService(otherProps)
        val token = otherService.generateToken(UUID.randomUUID().toString(), "hacker")

        assertFalse(jwtService.isTokenValid(token))
    }
}
