package com.newzic.service

import com.newzic.domain.entity.NotificationEntity
import com.newzic.domain.entity.NotificationType
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.NotificationRepository
import com.newzic.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {

    @Mock
    private lateinit var notificationRepository: NotificationRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var notificationService: NotificationService

    private lateinit var recipient: UserEntity
    private lateinit var sender: UserEntity

    @BeforeEach
    fun setUp() {
        recipient = UserEntity(
            id = UUID.randomUUID(),
            displayName = "Recipient Artist",
            username = "recipient",
            email = "recipient@test.com",
            passwordHash = "hash"
        )
        sender = UserEntity(
            id = UUID.randomUUID(),
            displayName = "Sender User",
            username = "sender",
            email = "sender@test.com",
            passwordHash = "hash"
        )
    }

    @Test
    fun `create saves notification for follow`() {
        whenever(userRepository.findById(recipient.id)).thenReturn(Optional.of(recipient))
        whenever(userRepository.findById(sender.id)).thenReturn(Optional.of(sender))

        notificationService.create(
            recipientId = recipient.id,
            fromUserId = sender.id,
            type = NotificationType.FOLLOW,
            message = "${sender.displayName} started following you",
            link = "/artist/${sender.id}"
        )

        verify(notificationRepository).save(argThat<NotificationEntity> {
            this.type == NotificationType.FOLLOW &&
            this.recipient.id == recipient.id &&
            this.fromUser?.id == sender.id &&
            this.message.contains("started following") &&
            this.isRead == false
        })
    }

    @Test
    fun `create saves notification for reaction with songId`() {
        val songId = UUID.randomUUID()
        whenever(userRepository.findById(recipient.id)).thenReturn(Optional.of(recipient))
        whenever(userRepository.findById(sender.id)).thenReturn(Optional.of(sender))

        notificationService.create(
            recipientId = recipient.id,
            fromUserId = sender.id,
            type = NotificationType.REACTION,
            message = "${sender.displayName} reacted 🔥 to \"My Song\"",
            link = "/artist/${recipient.id}",
            songId = songId
        )

        verify(notificationRepository).save(argThat<NotificationEntity> {
            this.type == NotificationType.REACTION &&
            this.songId == songId &&
            this.message.contains("🔥")
        })
    }

    @Test
    fun `create does not notify yourself`() {
        notificationService.create(
            recipientId = sender.id,
            fromUserId = sender.id,
            type = NotificationType.FOLLOW,
            message = "test"
        )

        verify(notificationRepository, never()).save(any())
    }

    @Test
    fun `create ignores non-existent recipient`() {
        whenever(userRepository.findById(any())).thenReturn(Optional.empty())

        notificationService.create(
            recipientId = UUID.randomUUID(),
            fromUserId = sender.id,
            type = NotificationType.FOLLOW,
            message = "test"
        )

        verify(notificationRepository, never()).save(any())
    }

    @Test
    fun `getForUser returns paginated notifications`() {
        val notification = NotificationEntity(
            type = NotificationType.FOLLOW,
            message = "test notification",
            recipient = recipient,
            fromUser = sender
        )
        val page = PageImpl(listOf(notification))
        whenever(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(eq(recipient.id), any()))
            .thenReturn(page)

        val result = notificationService.getForUser(recipient.id, PageRequest.of(0, 20))

        assertEquals(1, result.content.size)
        assertEquals("follow", result.content[0].type)
        assertEquals(sender.displayName, result.content[0].fromUser)
        assertEquals(false, result.content[0].read)
    }

    @Test
    fun `getUnreadCount returns correct count`() {
        whenever(notificationRepository.countByRecipientIdAndIsReadFalse(recipient.id)).thenReturn(5)

        val count = notificationService.getUnreadCount(recipient.id)

        assertEquals(5, count)
    }

    @Test
    fun `markAllAsRead delegates to repository`() {
        notificationService.markAllAsRead(recipient.id)

        verify(notificationRepository).markAllAsRead(recipient.id)
    }
}
