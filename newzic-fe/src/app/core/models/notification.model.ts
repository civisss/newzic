export type NotificationType = 'follow' | 'like' | 'comment' | 'release' | 'milestone';

export interface Notification {
  id: string;
  type: NotificationType;
  message: string;
  avatar: string;
  fromUser: string;
  timestamp: string;
  read: boolean;
  link?: string;
}
