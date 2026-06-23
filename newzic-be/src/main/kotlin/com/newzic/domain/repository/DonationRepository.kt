package com.newzic.domain.repository

import com.newzic.domain.entity.DonationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface DonationRepository : JpaRepository<DonationEntity, UUID> {

    fun findByToArtistIdOrderByCreatedAtDesc(artistId: UUID, pageable: Pageable): Page<DonationEntity>

    @Query("SELECT COALESCE(SUM(d.artistCents), 0) FROM DonationEntity d WHERE d.toArtist.id = :artistId")
    fun sumArtistCentsByArtistId(artistId: UUID): Long

    @Query("SELECT COUNT(DISTINCT d.fromUser.id) FROM DonationEntity d WHERE d.toArtist.id = :artistId")
    fun countDistinctSupportersByArtistId(artistId: UUID): Long

    @Query("""
        SELECT d.fromUser.id, d.fromUser.displayName, d.fromUser.avatar, SUM(d.artistCents) 
        FROM DonationEntity d 
        WHERE d.toArtist.id = :artistId 
        GROUP BY d.fromUser.id, d.fromUser.displayName, d.fromUser.avatar
        ORDER BY SUM(d.artistCents) DESC
    """)
    fun findTopSupportersByArtistId(artistId: UUID, pageable: Pageable): List<Array<Any>>
}
