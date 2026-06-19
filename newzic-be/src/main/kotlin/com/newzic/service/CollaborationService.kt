package com.newzic.service

import com.newzic.domain.entity.CollabStatus
import com.newzic.domain.repository.CollaborationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

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
    private val collaborationRepository: CollaborationRepository
) {

    fun getAll(pageable: Pageable): Page<CollaborationResponse> {
        return collaborationRepository.findAllByOrderByCreatedAtDesc(pageable).map { toResponse(it) }
    }

    fun getByStatus(status: String, pageable: Pageable): Page<CollaborationResponse> {
        val collabStatus = CollabStatus.valueOf(status.uppercase())
        return collaborationRepository.findByStatus(collabStatus, pageable).map { toResponse(it) }
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
