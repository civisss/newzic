package com.newzic.service

import com.newzic.api.dto.CreateWorkspaceRequest
import com.newzic.api.dto.WorkspaceResponse
import com.newzic.domain.entity.CollabStatus
import com.newzic.domain.repository.CollaborationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class CollaborationResponse(
    val id: String,
    val title: String,
    val description: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String?,
    val authorRole: String,
    val category: String,
    val genres: List<String>,
    val status: String,
    val createdAt: String,
    val responses: Int,
    val tags: List<String>
)

@Service
class CollaborationService(
    private val collaborationRepository: CollaborationRepository,
    private val workspaceService: WorkspaceService
) {

    fun getAll(pageable: Pageable): Page<CollaborationResponse> {
        return collaborationRepository.findAllByOrderByCreatedAtDesc(pageable).map { toResponse(it) }
    }

    fun getByStatus(status: String, pageable: Pageable): Page<CollaborationResponse> {
        val collabStatus = CollabStatus.valueOf(status.uppercase())
        return collaborationRepository.findByStatus(collabStatus, pageable).map { toResponse(it) }
    }

    @Transactional
    fun respondToCollab(collabId: UUID, responderId: UUID): WorkspaceResponse {
        val collab = collaborationRepository.findById(collabId)
            .orElseThrow { NoSuchElementException("Collaboration not found") }

        if (collab.status != CollabStatus.OPEN) {
            throw IllegalStateException("This collaboration is no longer open")
        }

        collab.status = CollabStatus.IN_PROGRESS
        collab.responses += 1
        collaborationRepository.save(collab)

        // Create workspace with collab author + responder
        return workspaceService.create(
            userId = collab.author.id,
            request = CreateWorkspaceRequest(
                title = collab.title,
                description = collab.description,
                collaborationId = collab.id.toString(),
                inviteUserIds = listOf(responderId.toString())
            )
        )
    }

    private fun toResponse(c: com.newzic.domain.entity.CollaborationEntity): CollaborationResponse {
        return CollaborationResponse(
            id = c.id.toString(),
            title = c.title,
            description = c.description,
            authorId = c.author.id.toString(),
            authorName = c.author.displayName,
            authorAvatar = c.author.avatar,
            authorRole = c.author.roles.firstOrNull()?.name?.lowercase() ?: "singer",
            category = c.category.name.lowercase(),
            genres = c.genres.toList(),
            status = c.status.name.lowercase(),
            createdAt = c.createdAt.toString(),
            responses = c.responses,
            tags = c.tags.toList()
        )
    }
}
