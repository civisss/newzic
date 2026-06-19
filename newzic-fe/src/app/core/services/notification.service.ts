import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Notification } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _notifications = signal<Notification[]>([]);
  private _unreadCount = signal(0);

  readonly notifications = this._notifications.asReadonly();
  readonly unreadCount = this._unreadCount.asReadonly();

  constructor(private http: HttpClient) {}

  load(): void {
    this.http.get<any>(`${environment.apiUrl}/notifications`).subscribe(page => {
      this._notifications.set((page.content || []).map((n: any) => this.mapNotification(n)));
    });
    this.http.get<{ count: number }>(`${environment.apiUrl}/notifications/unread-count`).subscribe(res => {
      this._unreadCount.set(res.count);
    });
  }

  markAsRead(id: string): void {
    this._notifications.update(list =>
      list.map(n => (n.id === id ? { ...n, read: true } : n))
    );
  }

  markAllAsRead(): void {
    this.http.post(`${environment.apiUrl}/notifications/mark-read`, {}).subscribe(() => {
      this._notifications.update(list => list.map(n => ({ ...n, read: true })));
      this._unreadCount.set(0);
    });
  }

  private mapNotification(n: any): Notification {
    return {
      id: n.id,
      type: n.type,
      message: n.message,
      avatar: n.avatar || '',
      fromUser: n.fromUser || '',
      timestamp: n.timestamp,
      read: n.read,
      link: n.link
    };
  }
}
