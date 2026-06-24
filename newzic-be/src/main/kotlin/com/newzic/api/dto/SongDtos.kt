package com.newzic.api.dto

import jakarta.validation.constraints.NotBlank

data class SongResponse(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val artistUsername: String,
    val artistAvatar: String?,
    val albumId: String?,
    val albumName: String?,
    val cover: String?,
    val duration: Int,
    val genre: String?,
    val tags: List<String>,
    val releaseDate: String,
    val plays: Long,
    val likes: Long,
    val reactions: ReactionsDto,
    val audioUrl: String?,
    val isExplicit: Boolean
)

data class ReactionsDto(
    val fire: Long = 0,
    val gem: Long = 0,
    val onpoint: Long = 0,
    val star: Long = 0
)

data class CreateSongRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val genre: String,
    val tags: List<String> = emptyList(),
    val isExplicit: Boolean = false,
    val albumId: String? = null,
    val description: String? = null,
    @field:NotBlank val cover: String,
    val audioData: String? = null
)

data class AlbumResponse(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val cover: String?,
    val type: String,
    val releaseDate: String,
    val genre: String?,
    val trackIds: List<String>,
    val totalPlays: Long,
    val description: String?
)
