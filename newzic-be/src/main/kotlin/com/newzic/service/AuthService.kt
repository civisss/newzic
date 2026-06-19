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
            roles = roles,
            country = request.country,
            preferredGenres = request.preferredGenres.toMutableSet()
        )

        val saved = userRepository.save(user)
        val token = jwtService.generateToken(saved.id.toString(), saved.username)
        return AuthResponse(token = token, user = userMapper.toResponse(saved))
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val token = jwtService.generateToken(user.id.toString(), user.username)
        return AuthResponse(token = token, user = userMapper.toResponse(user))
    }
}
