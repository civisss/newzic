import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Collaboration } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CollaborationService {

  constructor(private http: HttpClient) {}

  getAll(): Observable<Collaboration[]> {
    return this.http.get<any>(`${environment.apiUrl}/collaborations`).pipe(
      map(page => (page.content || []).map((c: any) => this.mapCollab(c)))
    );
  }

  getOpen(): Observable<Collaboration[]> {
    return this.http.get<any>(`${environment.apiUrl}/collaborations?status=open`).pipe(
      map(page => (page.content || []).map((c: any) => this.mapCollab(c)))
    );
  }

  getByCategory(category: string): Observable<Collaboration[]> {
    return this.http.get<any>(`${environment.apiUrl}/collaborations?status=open&category=${category}`).pipe(
      map(page => (page.content || []).map((c: any) => this.mapCollab(c)))
    );
  }

  search(query: string): Observable<Collaboration[]> {
    return this.http.get<any>(`${environment.apiUrl}/collaborations/search?q=${encodeURIComponent(query)}`).pipe(
      map(page => (page.content || []).map((c: any) => this.mapCollab(c)))
    );
  }

  respondToCollab(collabId: string): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/collaborations/${collabId}/respond`, {});
  }

  private mapCollab(c: any): Collaboration {
    return {
      id: c.id,
      title: c.title,
      description: c.description,
      authorId: c.authorId,
      authorName: c.authorName,
      authorUsername: c.authorUsername || '',
      authorAvatar: c.authorAvatar || '',
      authorRole: c.authorRole,
      category: c.category,
      genres: c.genres || [],
      status: c.status,
      createdAt: c.createdAt,
      responses: c.responses || 0,
      tags: c.tags || []
    };
  }
}
