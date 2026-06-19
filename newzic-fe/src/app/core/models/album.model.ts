export type AlbumType = 'album' | 'ep' | 'single';

export interface Album {
  id: string;
  title: string;
  artistId: string;
  artistName: string;
  cover: string;
  type: AlbumType;
  releaseDate: string;
  genre: string;
  trackIds: string[];
  totalPlays: number;
  description?: string;
}
