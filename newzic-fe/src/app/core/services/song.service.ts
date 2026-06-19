import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Song } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SongService {

  constructor(private http: HttpClient) {}

  getById(id: string): Observable<Song | undefined> {
    return this.http.get<any>(`${environment.apiUrl}/songs/${id}`).pipe(
      map(s => this.mapSong(s))
    );
  }

  getByArtist(artistId: string): Observable<Song[]> {
    return this.http.get<any>(`${environment.apiUrl}/songs/artist/${artistId}?size=50`).pipe(
      map(page => (page.content || []).map((s: any) => this.mapSong(s)))
    );
  }

  getTrending(): Observable<Song[]> {
    return this.http.get<any>(`${environment.apiUrl}/songs/trending?size=10`).pipe(
      map(page => (page.content || []).map((s: any) => this.mapSong(s)))
    );
  }

  getNewReleases(): Observable<Song[]> {
    return this.http.get<any>(`${environment.apiUrl}/songs/new-releases?size=10`).pipe(
      map(page => (page.content || []).map((s: any) => this.mapSong(s)))
    );
  }

  search(query: string): Observable<Song[]> {
    return this.http.get<any>(`${environment.apiUrl}/songs/search?q=${encodeURIComponent(query)}`).pipe(
      map(page => (page.content || []).map((s: any) => this.mapSong(s)))
    );
  }

  getRecommended(): Observable<Song[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/songs/recommended?size=10`).pipe(
      map(list => list.map((s: any) => this.mapSong(s)))
    );
  }

  create(data: { title: string; genre?: string; tags?: string[]; cover?: string; description?: string; audioData?: string }): Observable<Song> {
    return this.http.post<any>(`${environment.apiUrl}/songs`, data).pipe(
      map(s => this.mapSong(s))
    );
  }

  recordPlay(songId: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/songs/${songId}/play`, {});
  }

  react(songId: string, type: string): Observable<{ added: boolean }> {
    return this.http.post<{ added: boolean }>(`${environment.apiUrl}/songs/${songId}/react?type=${type}`, {});
  }

  private mapSong(s: any): Song {
    return {
      id: s.id,
      title: s.title,
      artistId: s.artistId,
      artistName: s.artistName,
      artistAvatar: s.artistAvatar,
      albumId: s.albumId,
      albumName: s.albumName,
      cover: s.cover || '',
      duration: s.duration || 0,
      genre: s.genre || '',
      tags: s.tags || [],
      releaseDate: s.releaseDate || '',
      plays: s.plays || 0,
      likes: s.likes || 0,
      reactions: s.reactions || { fire: 0, gem: 0, onpoint: 0, star: 0 },
      audioUrl: s.audioUrl,
      isExplicit: s.isExplicit || false
    };
  }
}
