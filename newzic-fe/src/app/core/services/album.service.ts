import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Album } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AlbumService {

  constructor(private http: HttpClient) {}

  getByArtist(artistId: string): Observable<Album[]> {
    return this.http.get<any>(`${environment.apiUrl}/albums/artist/${artistId}`).pipe(
      map(res => (Array.isArray(res) ? res : (res.content || [])).map((a: any) => this.mapAlbum(a)))
    );
  }

  getById(id: string): Observable<Album | undefined> {
    return this.http.get<any>(`${environment.apiUrl}/albums/${id}`).pipe(
      map(a => this.mapAlbum(a))
    );
  }

  private mapAlbum(a: any): Album {
    return {
      id: a.id,
      title: a.title,
      artistId: a.artistId,
      artistName: a.artistName,
      cover: a.cover || '',
      type: a.type || 'album',
      releaseDate: a.releaseDate || '',
      genre: a.genre || '',
      trackIds: a.trackIds || [],
      totalPlays: a.totalPlays || 0,
      description: a.description
    };
  }
}
