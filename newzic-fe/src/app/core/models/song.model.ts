export type ReactionType = 'fire' | 'gem' | 'onpoint' | 'star';

export interface Reactions {
  fire: number;
  gem: number;
  onpoint: number;
  star: number;
}

export interface Song {
  id: string;
  title: string;
  artistId: string;
  artistName: string;
  artistUsername: string;
  artistAvatar?: string;
  albumId?: string;
  albumName?: string;
  cover: string;
  duration: number; // seconds
  genre: string;
  tags: string[];
  releaseDate: string;
  plays: number;
  likes: number;
  reactions: Reactions;
  audioUrl?: string;
  isExplicit?: boolean;
}
