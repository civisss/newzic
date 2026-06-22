package com.newzic.security

import com.newzic.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(private val jwtProperties: JwtProperties) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateToken(userId: String, username: String, roles: List<String> = emptyList(), email: String? = null): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.expirationMs)

        val builder = Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("roles", roles)

        if (email != null) {
            builder.claim("email", email)
        }

        return builder
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): String {
        return getClaims(token).subject
    }

    fun getUsernameFromToken(token: String): String? {
        return getClaims(token)["username"] as? String
    }

    fun getRolesFromToken(token: String): List<String> {
        @Suppress("UNCHECKED_CAST")
        return getClaims(token)["roles"] as? List<String> ?: emptyList()
    }

    fun getEmailFromToken(token: String): String? {
        return getClaims(token)["email"] as? String
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
