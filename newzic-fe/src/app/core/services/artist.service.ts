import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Artist } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ArtistService {

  constructor(private http: HttpClient) {}

  getById(id: string): Observable<Artist | undefined> {
    return this.http.get<any>(`${environment.apiUrl}/artists/${id}`).pipe(
      map(u => this.mapArtist(u))
    );
  }

  getTrending(): Observable<Artist[]> {
    return this.http.get<any>(`${environment.apiUrl}/artists/trending?size=10`).pipe(
      map(page => (page.content || []).map((u: any) => this.mapArtist(u)))
    );
  }

  getProducers(): Observable<Artist[]> {
    return this.http.get<any>(`${environment.apiUrl}/artists/producers?size=10`).pipe(
      map(page => (page.content || []).map((u: any) => this.mapArtist(u)))
    );
  }

  getCommunityPicks(): Observable<Artist[]> {
    return this.http.get<any>(`${environment.apiUrl}/artists/community-picks?size=6`).pipe(
      map(page => (page.content || []).map((u: any) => this.mapArtist(u)))
    );
  }

  getRecommended(): Observable<Artist[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/artists/recommended?size=10`).pipe(
      map(list => list.map((u: any) => this.mapArtist(u)))
    );
  }

  follow(artistId: string): Observable<{ following: boolean }> {
    return this.http.post<{ following: boolean }>(`${environment.apiUrl}/artists/${artistId}/follow`, {});
  }

  isFollowing(artistId: string): Observable<{ following: boolean }> {
    return this.http.get<{ following: boolean }>(`${environment.apiUrl}/artists/${artistId}/following`);
  }

  getFollowers(artistId: string): Observable<Artist[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/artists/${artistId}/followers`).pipe(
      map(list => list.map(u => this.mapArtist(u)))
    );
  }

  getFollowing(artistId: string): Observable<Artist[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/artists/${artistId}/following-list`).pipe(
      map(list => list.map(u => this.mapArtist(u)))
    );
  }

  search(query: string): Observable<Artist[]> {
    return this.http.get<any>(`${environment.apiUrl}/artists/search?q=${encodeURIComponent(query)}`).pipe(
      map(page => (page.content || []).map((u: any) => this.mapArtist(u)))
    );
  }

  private mapArtist(u: any): Artist {
    return {
      id: u.id,
      name: u.displayName,
      username: u.username,
      avatar: u.avatar || '',
      cover: u.cover || '',
      bio: u.bio || '',
      longBio: u.longBio,
      role: u.roles?.[0] || 'singer',
      followers: u.followers || 0,
      following: u.following || 0,
      totalPlays: u.totalPlays || 0,
      socialLinks: u.socialLinks || {},
      genres: u.genres || [],
      tags: u.tags || [],
      verified: u.verified || false,
      premium: u.premium || false,
      joinedDate: u.joinedDate || '',
      location: u.location,
      photos: u.photos,
      lookingForCollab: u.lookingForCollab || false,
      collabDescription: u.collabDescription,
      weeklyGrowth: u.weeklyGrowth
    };
  }
}
