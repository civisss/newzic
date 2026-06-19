package com.newzic.domain.repository

import com.newzic.domain.entity.SpotlightEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpotlightRepository : JpaRepository<SpotlightEntity, UUID> {

    @EntityGraph(attributePaths = ["artist", "featuredSong"])
    fun findFirstByActiveTrueOrderByCreatedAtDesc(): SpotlightEntity?

    @EntityGraph(attributePaths = ["artist", "featuredSong"])
    fun findTop3ByActiveTrueOrderByCreatedAtDesc(): List<SpotlightEntity>
}
