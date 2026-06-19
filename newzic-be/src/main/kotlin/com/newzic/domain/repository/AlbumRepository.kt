package com.newzic.domain.repository

import com.newzic.domain.entity.AlbumEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AlbumRepository : JpaRepository<AlbumEntity, UUID> {

    fun findByArtistId(artistId: UUID, pageable: Pageable): Page<AlbumEntity>

    fun findByArtistId(artistId: UUID): List<AlbumEntity>
}
