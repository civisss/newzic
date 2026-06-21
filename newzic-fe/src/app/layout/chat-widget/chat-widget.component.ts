import { Component, OnInit, signal, computed, ViewChild, ElementRef, AfterViewChecked, effect } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { MessageService, Message, ConversationPreview } from '../../core/services/message.service';
import { AuthService } from '../../core/services/auth.service';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [FormsModule, DatePipe, RouterLink, TranslatePipe],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.scss'
})
export class ChatWidgetComponent implements OnInit, AfterViewChecked {
  isOpen = signal(false);
  conversations = signal<ConversationPreview[]>([]);
  activeConversation = signal<string | null>(null);
  messages = signal<Message[]>([]);
  newMessage = '';
  sending = signal(false);
  private shouldScroll = false;

  @ViewChild('chatMessagesEnd') chatMessagesEnd?: ElementRef;

  readonly currentUserId = computed(() => this.auth.user()?.id ?? '');
  readonly unreadCount = computed(() => this.messageService.unreadCount());

  constructor(
    public messageService: MessageService,
    public auth: AuthService
  ) {
    effect(() => {
      const userId = this.messageService.openChat();
      if (userId) {
        this.openConversationById(userId);
      }
    });
  }

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.messageService.loadConversations();
      this.messageService.loadUnreadCount();
    }
    // Poll for new messages every 30s
    setInterval(() => {
      if (this.auth.isLoggedIn()) {
        this.messageService.loadUnreadCount();
      }
    }, 30000);
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  toggle(): void {
    this.isOpen.update(v => !v);
    if (this.isOpen()) {
      this.messageService.loadConversations();
      setTimeout(() => this.conversations.set([...this.messageService.conversations()]), 300);
    } else {
      this.activeConversation.set(null);
      this.messages.set([]);
    }
  }

  openConversation(userId: string): void {
    this.activeConversation.set(userId);
    this.messageService.getConversation(userId).subscribe(msgs => {
      this.messages.set(msgs);
      this.shouldScroll = true;
    });
    this.messageService.markAsRead(userId).subscribe(() => {
      this.conversations.update(list =>
        list.map(c => c.userId === userId ? { ...c, unread: false } : c)
      );
      this.messageService.loadUnreadCount();
    });
  }

  openConversationById(userId: string): void {
    if (!this.isOpen()) this.isOpen.set(true);
    this.messageService.loadConversations();
    setTimeout(() => {
      this.conversations.set([...this.messageService.conversations()]);
      this.openConversation(userId);
    }, 300);
  }

  backToList(): void {
    this.activeConversation.set(null);
    this.messages.set([]);
  }

  sendMessage(): void {
    const recipientId = this.activeConversation();
    const content = this.newMessage.trim();
    if (!recipientId || !content) return;

    this.sending.set(true);
    this.messageService.sendMessage(recipientId, content).subscribe({
      next: (msg) => {
        this.messages.update(list => [...list, msg]);
        this.newMessage = '';
        this.sending.set(false);
        this.shouldScroll = true;
        this.conversations.update(list => {
          const exists = list.find(c => c.userId === recipientId);
          if (exists) {
            return list.map(c => c.userId === recipientId
              ? { ...c, lastMessage: content, lastMessageTime: msg.createdAt }
              : c
            );
          }
          return [{
            userId: recipientId,
            displayName: msg.recipientName,
            avatar: msg.recipientAvatar,
            lastMessage: content,
            lastMessageTime: msg.createdAt,
            unread: false
          }, ...list];
        });
      },
      error: () => this.sending.set(false)
    });
  }

  getActiveUser(): ConversationPreview | undefined {
    return this.conversations().find(c => c.userId === this.activeConversation());
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom(): void {
    this.chatMessagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' });
  }
}
