package com.newzic.domain.repository

import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun findByUsername(username: String): UserEntity?

    fun findByEmail(email: String): UserEntity?

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM UserEntity u WHERE :role MEMBER OF u.roles ORDER BY u.followers DESC")
    fun findByRole(role: ArtistRole, pageable: Pageable): Page<UserEntity>

    @Query("SELECT u FROM UserEntity u ORDER BY u.followers DESC")
    fun findTrending(pageable: Pageable): Page<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE :role MEMBER OF u.roles ORDER BY u.totalPlays DESC")
    fun findTopByRole(role: ArtistRole, pageable: Pageable): Page<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.verified = true ORDER BY u.totalPlays DESC")
    fun findCommunityPicks(pageable: Pageable): Page<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.lookingForCollab = true")
    fun findLookingForCollab(pageable: Pageable): Page<UserEntity>

    @Query("""
        SELECT u FROM UserEntity u 
        WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun search(query: String, pageable: Pageable): Page<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.country = :country AND u.id <> :excludeId")
    fun findByCountry(country: String, excludeId: UUID): List<UserEntity>

    @Query("SELECT u FROM UserEntity u WHERE u.id <> :excludeId")
    fun findAllExcept(excludeId: UUID): List<UserEntity>
}
