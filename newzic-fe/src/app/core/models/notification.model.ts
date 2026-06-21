export type NotificationType = 'follow' | 'reaction' | 'like' | 'comment' | 'release' | 'milestone';

export interface Notification {
  id: string;
  type: NotificationType;
  message: string;
  avatar: string;
  fromUserId?: string;
  fromUser: string;
  songId?: string;
  timestamp: string;
  read: boolean;
  link?: string;
}
