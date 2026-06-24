import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JournalPost, JournalComment, ReactionSummary, PagedResponse, CreateJournalPostRequest } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class JournalService {

  private url = `${environment.apiUrl}/journal`;

  constructor(private http: HttpClient) {}

  // ── Posts ──

  createPost(request: CreateJournalPostRequest): Observable<JournalPost> {
    return this.http.post<JournalPost>(this.url, request);
  }

  updatePost(postId: string, request: Partial<CreateJournalPostRequest>): Observable<JournalPost> {
    return this.http.put<JournalPost>(`${this.url}/${postId}`, request);
  }

  deletePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${postId}`);
  }

  getPost(postId: string): Observable<JournalPost> {
    return this.http.get<JournalPost>(`${this.url}/${postId}`);
  }

  getPostsByAuthor(authorId: string, page = 0, size = 20): Observable<PagedResponse<JournalPost>> {
    return this.http.get<PagedResponse<JournalPost>>(`${this.url}/user/${authorId}?page=${page}&size=${size}`);
  }

  // ── Feed ──

  getFeed(page = 0, size = 20): Observable<PagedResponse<JournalPost>> {
    return this.http.get<PagedResponse<JournalPost>>(`${this.url}/feed?page=${page}&size=${size}`);
  }

  // ── Search ──

  search(query: string, page = 0, size = 20): Observable<PagedResponse<JournalPost>> {
    return this.http.get<PagedResponse<JournalPost>>(`${this.url}/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`);
  }

  // ── Reactions ──

  toggleReaction(postId: string, type: string): Observable<ReactionSummary> {
    return this.http.post<ReactionSummary>(`${this.url}/${postId}/reactions`, { type });
  }

  // ── Comments ──

  getComments(postId: string, page = 0, size = 50): Observable<PagedResponse<JournalComment>> {
    return this.http.get<PagedResponse<JournalComment>>(`${this.url}/${postId}/comments?page=${page}&size=${size}`);
  }

  addComment(postId: string, content: string): Observable<JournalComment> {
    return this.http.post<JournalComment>(`${this.url}/${postId}/comments`, { content });
  }

  deleteComment(commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/comments/${commentId}`);
  }
}
