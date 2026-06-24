import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { FeedPost } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FeedService {

  constructor(private http: HttpClient) {}

  getFeed(): Observable<FeedPost[]> {
    return this.http.get<any>(`${environment.apiUrl}/feed`).pipe(
      map(page => (page.content || []).map((p: any) => this.mapPost(p)))
    );
  }

  getByArtist(artistId: string): Observable<FeedPost[]> {
    return this.http.get<any>(`${environment.apiUrl}/feed/user/${artistId}`).pipe(
      map(page => (page.content || []).map((p: any) => this.mapPost(p)))
    );
  }

  private mapPost(p: any): FeedPost {
    return {
      id: p.id,
      type: p.type,
      authorId: p.authorId,
      authorName: p.authorName,
      authorUsername: p.authorUsername || '',
      authorAvatar: p.authorAvatar || '',
      authorRole: p.authorRole,
      content: p.content,
      image: p.image,
      songId: p.songId,
      songTitle: p.songTitle,
      songCover: p.songCover,
      timestamp: p.timestamp,
      likes: p.likes || 0,
      comments: p.comments || 0,
      reactions: p.reactions || { fire: 0, gem: 0, onpoint: 0, star: 0 }
    };
  }
}
