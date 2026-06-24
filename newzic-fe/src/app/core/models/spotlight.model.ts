export interface Spotlight {
  artistId: string;
  artistName: string;
  artistUsername: string;
  artistAvatar: string;
  artistCover: string;
  artistFollowers: number;
  artistTotalPlays: number;
  artistGenres: string[];
  artistTotalSongs: number;
  artistVerified: boolean;
  artistLocation?: string;
  category: string;
  categoryLabel: string;
  description: string;
  extraStat?: number;
}
