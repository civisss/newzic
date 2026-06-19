import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Spotlight } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SpotlightService {

  constructor(private http: HttpClient) {}

  getCurrent(): Observable<Spotlight> {
    return this.http.get<any>(`${environment.apiUrl}/spotlight/current`).pipe(
      map(s => ({
        id: s.id,
        artistId: s.artistId,
        artistName: s.artistName,
        artistAvatar: s.artistAvatar || '',
        artistCover: s.artistCover || '',
        quote: s.quote || '',
        featuredSongId: s.featuredSongId || '',
        featuredSongTitle: s.featuredSongTitle || '',
        featuredSongCover: s.featuredSongCover || '',
        editorNote: s.editorNote || '',
        weekLabel: s.weekLabel || '',
        artistFollowers: s.artistFollowers || 0,
        artistTotalPlays: s.artistTotalPlays || 0,
        artistGenres: s.artistGenres || [],
        artistTotalSongs: s.artistTotalSongs || 0,
        artistVerified: s.artistVerified || false,
        artistLocation: s.artistLocation
      }))
    );
  }
}
