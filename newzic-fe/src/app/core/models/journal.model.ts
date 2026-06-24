export type JournalCategory = 'update' | 'collaboration' | 'looking_for_collab' | 'announcement';
export type JournalReactionType = 'like' | 'fire' | 'music' | 'hype';

export interface JournalPost {
  id: string;
  authorId: string;
  authorName: string;
  authorUsername: string;
  authorAvatar: string | null;
  authorRole: string;
  authorPremium: boolean;
  authorVerified: boolean;
  content: string;
  imageUrl: string | null;
  category: JournalCategory;
  hashtags: string[];
  taggedUsers: TaggedUser[];
  reactions: ReactionSummary;
  commentCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface TaggedUser {
  id: string;
  username: string;
  displayName: string;
  avatar: string | null;
}

export interface ReactionSummary {
  total: number;
  like: number;
  fire: number;
  music: number;
  hype: number;
  userReactions: string[];
}

export interface JournalComment {
  id: string;
  postId: string;
  authorId: string;
  authorName: string;
  authorUsername: string;
  authorAvatar: string | null;
  content: string;
  createdAt: string;
}

export interface CreateJournalPostRequest {
  content: string;
  imageUrl?: string;
  category?: string;
  hashtags?: string[];
  taggedUserIds?: string[];
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}
