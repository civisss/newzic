import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ArtistStats } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StatsService {

  constructor(private http: HttpClient) {}

  getMyStats(): Observable<ArtistStats> {
    return this.http.get<ArtistStats>(`${environment.apiUrl}/stats/me`);
  }

  getArtistStats(artistId: string): Observable<ArtistStats> {
    return this.http.get<ArtistStats>(`${environment.apiUrl}/stats/artist/${artistId}`);
  }
}
