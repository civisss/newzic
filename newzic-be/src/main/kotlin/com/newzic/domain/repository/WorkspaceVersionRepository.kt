package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceVersionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface WorkspaceVersionRepository : JpaRepository<WorkspaceVersionEntity, UUID> {

    fun findByWorkspaceIdOrderByVersionNumberDesc(workspaceId: UUID): List<WorkspaceVersionEntity>

    fun findFirstByWorkspaceIdOrderByVersionNumberDesc(workspaceId: UUID): WorkspaceVersionEntity?

    @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM WorkspaceVersionEntity v WHERE v.workspace.id = :workspaceId")
    fun findMaxVersionNumber(workspaceId: UUID): Int

    fun countByWorkspaceId(workspaceId: UUID): Long
}
