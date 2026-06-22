package com.newzic.service

import com.newzic.api.dto.AuthResponse
import com.newzic.api.dto.LoginRequest
import com.newzic.api.dto.RegisterRequest
import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.UserRepository
import com.newzic.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val userMapper: UserMapper
) {

    companion object {
        const val DEFAULT_AVATAR = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' stop-color='%23B06CFF'/%3E%3Cstop offset='100%25' stop-color='%233B82F6'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='200' height='200' rx='100' fill='url(%23g)'/%3E%3Cpath d='M65 145V60L105 110V60' stroke='white' stroke-width='12' stroke-linecap='round' stroke-linejoin='round' fill='none'/%3E%3Cpath d='M120 78c10 8 16 20 16 32s-6 24-16 32' stroke='white' stroke-width='9' stroke-linecap='round' fill='none' opacity='0.9'/%3E%3Cpath d='M138 62c14 12 22 29 22 46s-8 34-22 46' stroke='white' stroke-width='8' stroke-linecap='round' fill='none' opacity='0.5'/%3E%3C/svg%3E"
    }

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already taken")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        val roles = request.roles.mapNotNull { role ->
            try { ArtistRole.valueOf(role.uppercase()) } catch (e: Exception) { null }
        }.toMutableSet()

        if (roles.isEmpty()) {
            roles.add(ArtistRole.SINGER)
        }

        val user = UserEntity(
            username = request.username,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            displayName = request.artistName,
            avatar = DEFAULT_AVATAR,
            roles = roles,
            country = request.country,
            preferredGenres = request.preferredGenres.toMutableSet()
        )

        val saved = userRepository.save(user)
        val token = jwtService.generateToken(
            saved.id.toString(),
            saved.username,
            saved.roles.map { it.name },
            saved.email
        )
        return AuthResponse(token = token, user = userMapper.toResponse(saved))
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val token = jwtService.generateToken(
            user.id.toString(),
            user.username,
            user.roles.map { it.name },
            user.email
        )
        return AuthResponse(token = token, user = userMapper.toResponse(user))
    }
}
