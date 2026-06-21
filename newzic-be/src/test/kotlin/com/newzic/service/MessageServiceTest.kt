package com.newzic.service

import com.newzic.domain.entity.ArtistRole
import com.newzic.domain.entity.MessageEntity
import com.newzic.domain.entity.UserEntity
import com.newzic.domain.repository.MessageRepository
import com.newzic.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class MessageServiceTest {

    @Mock
    private lateinit var messageRepository: MessageRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var sender: UserEntity
    private lateinit var recipient: UserEntity

    @BeforeEach
    fun setUp() {
        sender = UserEntity(
            id = UUID.randomUUID(),
            displayName = "Sender",
            username = "sender",
            email = "sender@test.com",
            passwordHash = "h",
            roles = mutableSetOf(ArtistRole.SINGER)
        )
        recipient = UserEntity(
            id = UUID.randomUUID(),
            displayName = "Recipient",
            username = "recipient",
            email = "recipient@test.com",
            passwordHash = "h",
            roles = mutableSetOf(ArtistRole.PRODUCER)
        )
    }

    @Test
    fun `sending a message should persist and return it`() {
        val content = "Hello, love your music!"

        whenever(userRepository.findById(sender.id)).thenReturn(Optional.of(sender))
        whenever(userRepository.findById(recipient.id)).thenReturn(Optional.of(recipient))
        whenever(messageRepository.save(any<MessageEntity>())).thenAnswer { it.arguments[0] }

        val senderEntity = userRepository.findById(sender.id).get()
        val recipientEntity = userRepository.findById(recipient.id).get()
        val message = messageRepository.save(
            MessageEntity(sender = senderEntity, recipient = recipientEntity, content = content)
        )

        assertEquals(content, message.content)
        assertEquals(sender.id, message.sender.id)
        assertEquals(recipient.id, message.recipient.id)
        assertFalse(message.isRead)
        verify(messageRepository).save(any<MessageEntity>())
    }

    @Test
    fun `findConversation should return messages between two users`() {
        val msg1 = MessageEntity(sender = sender, recipient = recipient, content = "Hi!")
        val msg2 = MessageEntity(sender = recipient, recipient = sender, content = "Hey!")

        whenever(messageRepository.findConversation(sender.id, recipient.id))
            .thenReturn(listOf(msg1, msg2))

        val conversation = messageRepository.findConversation(sender.id, recipient.id)

        assertEquals(2, conversation.size)
        assertEquals("Hi!", conversation[0].content)
        assertEquals("Hey!", conversation[1].content)
    }

    @Test
    fun `markConversationAsRead should update unread messages`() {
        whenever(messageRepository.markConversationAsRead(recipient.id, sender.id)).thenReturn(3)

        val updated = messageRepository.markConversationAsRead(recipient.id, sender.id)

        assertEquals(3, updated)
        verify(messageRepository).markConversationAsRead(recipient.id, sender.id)
    }

    @Test
    fun `countUnread should return correct count`() {
        whenever(messageRepository.countUnread(recipient.id)).thenReturn(5L)

        val count = messageRepository.countUnread(recipient.id)

        assertEquals(5L, count)
    }
}
