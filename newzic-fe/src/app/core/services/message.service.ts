import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Message {
  id: string;
  senderId: string;
  senderName: string;
  senderAvatar: string | null;
  recipientId: string;
  recipientName: string;
  recipientAvatar: string | null;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface ConversationPreview {
  userId: string;
  username: string;
  displayName: string;
  avatar: string | null;
  lastMessage: string;
  lastMessageTime: string;
  unread: boolean;
}

@Injectable({ providedIn: 'root' })
export class MessageService {
  private _conversations = signal<ConversationPreview[]>([]);
  private _unreadCount = signal(0);

  readonly conversations = this._conversations.asReadonly();
  readonly unreadCount = this._unreadCount.asReadonly();

  private _openChat = signal<string | null>(null);
  readonly openChat = this._openChat.asReadonly();

  constructor(private http: HttpClient) {}

  requestOpenChat(userId: string): void {
    this._openChat.set(userId);
    // Reset after consumption
    setTimeout(() => this._openChat.set(null), 100);
  }

  loadConversations(): void {
    this.http.get<ConversationPreview[]>(`${environment.apiUrl}/messages/conversations`).subscribe(list => {
      this._conversations.set(list);
    });
  }

  loadUnreadCount(): void {
    this.http.get<{ count: number }>(`${environment.apiUrl}/messages/unread-count`).subscribe(res => {
      this._unreadCount.set(res.count);
    });
  }

  getConversation(otherId: string): Observable<Message[]> {
    return this.http.get<Message[]>(`${environment.apiUrl}/messages/conversation/${otherId}`);
  }

  sendMessage(recipientId: string, content: string): Observable<Message> {
    return this.http.post<Message>(`${environment.apiUrl}/messages/send/${recipientId}`, { content });
  }

  markAsRead(senderId: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/messages/read/${senderId}`, {});
  }
}
