export type FeedPostType = 'new_release' | 'snippet' | 'behind_the_scenes' | 'milestone' | 'collab_request' | 'update';

export interface FeedPost {
  id: string;
  type: FeedPostType;
  authorId: string;
  authorName: string;
  authorAvatar: string;
  authorRole: string;
  content: string;
  image?: string;
  songId?: string;
  songTitle?: string;
  songCover?: string;
  timestamp: string;
  likes: number;
  comments: number;
  reactions: {
    fire: number;
    gem: number;
    onpoint: number;
    star: number;
  };
}
