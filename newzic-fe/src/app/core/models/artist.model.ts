export type ArtistRole = 'singer' | 'producer' | 'band' | 'musician' | 'beatmaker';

export interface SocialLinks {
  spotify?: string;
  youtubeMusic?: string;
  appleMusic?: string;
  soundcloud?: string;
  tiktok?: string;
  instagram?: string;
}

export interface Artist {
  id: string;
  name: string;
  username: string;
  avatar: string;
  cover: string;
  bio: string;
  longBio?: string;
  role: ArtistRole;
  followers: number;
  following: number;
  totalPlays: number;
  socialLinks: SocialLinks;
  genres: string[];
  tags: string[];
  verified: boolean;
  premium: boolean;
  joinedDate: string;
  location?: string;
  photos?: string[];
  lookingForCollab?: boolean;
  collabDescription?: string;
  weeklyGrowth?: number; // percentage
}
