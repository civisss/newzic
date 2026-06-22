package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceEntity
import com.newzic.domain.entity.WorkspaceStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface WorkspaceRepository : JpaRepository<WorkspaceEntity, UUID> {

    @Query("""
        SELECT w FROM WorkspaceEntity w 
        WHERE w.id IN (
            SELECT m.workspace.id FROM WorkspaceMemberEntity m WHERE m.user.id = :userId
        )
        ORDER BY w.updatedAt DESC
    """)
    fun findByMemberUserId(userId: UUID): List<WorkspaceEntity>

    @Query("""
        SELECT w FROM WorkspaceEntity w 
        WHERE w.id IN (
            SELECT m.workspace.id FROM WorkspaceMemberEntity m WHERE m.user.id = :userId
        )
        AND w.status = :status
        ORDER BY w.updatedAt DESC
    """)
    fun findByMemberUserIdAndStatus(userId: UUID, status: WorkspaceStatus): List<WorkspaceEntity>

    fun findByCollaborationId(collaborationId: UUID): WorkspaceEntity?
}
