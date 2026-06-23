package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceActivityEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkspaceActivityRepository : JpaRepository<WorkspaceActivityEntity, UUID> {

    fun findByWorkspaceIdOrderByCreatedAtDesc(workspaceId: UUID, pageable: Pageable): Page<WorkspaceActivityEntity>

    fun findByWorkspaceIdOrderByCreatedAtDesc(workspaceId: UUID): List<WorkspaceActivityEntity>
}
