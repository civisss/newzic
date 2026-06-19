export type CollabStatus = 'open' | 'in_progress' | 'closed';
export type CollabCategory = 'vocalist' | 'producer' | 'beatmaker' | 'guitarist' | 'mixing' | 'mastering' | 'songwriter' | 'other';

export interface Collaboration {
  id: string;
  title: string;
  description: string;
  authorId: string;
  authorName: string;
  authorAvatar: string;
  authorRole: string;
  category: CollabCategory;
  genres: string[];
  status: CollabStatus;
  createdAt: string;
  responses: number;
  tags: string[];
}
