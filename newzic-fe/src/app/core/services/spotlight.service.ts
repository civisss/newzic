import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Spotlight } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SpotlightService {

  constructor(private http: HttpClient) {}

  getWeeklyTop(): Observable<Spotlight[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/spotlight/weekly-top`).pipe(
      map(list => list.map(s => ({
        artistId: s.artistId,
        artistName: s.artistName,
        artistAvatar: s.artistAvatar || '',
        artistCover: s.artistCover || '',
        artistFollowers: s.artistFollowers || 0,
        artistTotalPlays: s.artistTotalPlays || 0,
        artistGenres: s.artistGenres || [],
        artistTotalSongs: s.artistTotalSongs || 0,
        artistVerified: s.artistVerified || false,
        artistLocation: s.artistLocation,
        category: s.category,
        categoryLabel: s.categoryLabel,
        description: s.description,
        extraStat: s.extraStat
      })))
    );
  }
}
