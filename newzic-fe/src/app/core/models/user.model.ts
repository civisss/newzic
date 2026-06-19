import { ArtistRole, SocialLinks } from './artist.model';

export interface User {
  id: string;
  username: string;
  email: string;
  displayName: string;
  avatar: string;
  role: ArtistRole;
  bio?: string;
  socialLinks?: SocialLinks;
  joinedDate: string;
  country?: string;
  preferredGenres?: string[];
}
