package com.newzic.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

data class RegisterRequest(
    @field:NotBlank val artistName: String,
    @field:NotBlank @field:Size(min = 3, max = 30) val username: String,
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    val roles: List<String> = emptyList(),
    val country: String? = null,
    val preferredGenres: List<String> = emptyList()
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)
