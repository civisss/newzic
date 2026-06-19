package com.newzic.api.controller

import com.newzic.service.NotificationResponse
import com.newzic.service.NotificationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationController(private val notificationService: NotificationService) {

    @GetMapping
    fun getNotifications(
        auth: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<NotificationResponse>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(notificationService.getForUser(userId, PageRequest.of(page, size)))
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(auth: Authentication): ResponseEntity<Map<String, Long>> {
        val userId = auth.principal as UUID
        return ResponseEntity.ok(mapOf("count" to notificationService.getUnreadCount(userId)))
    }

    @PostMapping("/mark-read")
    fun markAllRead(auth: Authentication): ResponseEntity<Void> {
        val userId = auth.principal as UUID
        notificationService.markAllAsRead(userId)
        return ResponseEntity.noContent().build()
    }
}
