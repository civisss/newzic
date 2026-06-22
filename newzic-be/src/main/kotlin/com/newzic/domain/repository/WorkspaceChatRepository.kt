package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceChatEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkspaceChatRepository : JpaRepository<WorkspaceChatEntity, UUID> {

    fun findByWorkspaceIdOrderByCreatedAtAsc(workspaceId: UUID): List<WorkspaceChatEntity>

    fun findByWorkspaceIdOrderByCreatedAtDesc(workspaceId: UUID, pageable: Pageable): Page<WorkspaceChatEntity>
}
