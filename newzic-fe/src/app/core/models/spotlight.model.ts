export interface Spotlight {
  id: string;
  artistId: string;
  artistName: string;
  artistAvatar: string;
  artistCover: string;
  quote: string;
  featuredSongId: string;
  featuredSongTitle: string;
  featuredSongCover: string;
  editorNote: string;
  weekLabel: string;
  artistFollowers: number;
  artistTotalPlays: number;
  artistGenres: string[];
  artistTotalSongs: number;
  artistVerified: boolean;
  artistLocation?: string;
}
