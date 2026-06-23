package com.newzic.domain.repository

import com.newzic.domain.entity.TaskStatus
import com.newzic.domain.entity.WorkspaceTaskEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkspaceTaskRepository : JpaRepository<WorkspaceTaskEntity, UUID> {

    fun findByWorkspaceIdOrderByCreatedAtAsc(workspaceId: UUID): List<WorkspaceTaskEntity>

    fun findByWorkspaceIdAndStatusOrderByCreatedAtAsc(workspaceId: UUID, status: TaskStatus): List<WorkspaceTaskEntity>

    fun countByWorkspaceId(workspaceId: UUID): Long

    fun countByWorkspaceIdAndStatus(workspaceId: UUID, status: TaskStatus): Long
}
