import { Component, OnInit, signal, computed, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { MessageService, Message, ConversationPreview } from '../../core/services/message.service';
import { AuthService } from '../../core/services/auth.service';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-messages',
  standalone: true,
  imports: [FormsModule, DatePipe, RouterLink, TranslatePipe],
  templateUrl: './messages.component.html',
  styleUrl: './messages.component.scss'
})
export class MessagesComponent implements OnInit, AfterViewChecked {
  conversations = signal<ConversationPreview[]>([]);
  activeConversation = signal<string | null>(null);
  messages = signal<Message[]>([]);
  newMessage = '';
  sending = signal(false);
  private shouldScroll = false;

  @ViewChild('messagesEnd') messagesEnd?: ElementRef;

  readonly currentUserId = computed(() => this.auth.user()?.id ?? '');

  constructor(
    private messageService: MessageService,
    public auth: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // On desktop, redirect and open chat widget instead
    if (window.innerWidth > 768) {
      this.route.params.subscribe(params => {
        const userId = params['userId'];
        if (userId) {
          this.messageService.requestOpenChat(userId);
        }
        this.router.navigate(['/home']);
      });
      return;
    }

    this.messageService.loadConversations();
    setTimeout(() => {
      this.conversations.set([...this.messageService.conversations()]);
    }, 500);

    this.route.params.subscribe(params => {
      const userId = params['userId'];
      if (userId) {
        this.openConversation(userId);
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
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
    });
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
        // Update conversation preview
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

  closeConversation(): void {
    this.activeConversation.set(null);
    this.messages.set([]);
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
    this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' });
  }
}
