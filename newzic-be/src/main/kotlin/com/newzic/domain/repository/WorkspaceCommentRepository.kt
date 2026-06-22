package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceCommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkspaceCommentRepository : JpaRepository<WorkspaceCommentEntity, UUID> {

    fun findByVersionIdOrderByTimestampSecondsAsc(versionId: UUID): List<WorkspaceCommentEntity>

    fun findByVersionIdAndParentIsNullOrderByTimestampSecondsAsc(versionId: UUID): List<WorkspaceCommentEntity>

    fun findByParentIdOrderByCreatedAtAsc(parentId: UUID): List<WorkspaceCommentEntity>

    fun countByVersionId(versionId: UUID): Long
}
