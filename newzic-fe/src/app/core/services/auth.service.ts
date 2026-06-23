import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, map } from 'rxjs';
import { User } from '../models';
import { environment } from '../../../environments/environment';
import { PlayerService } from './player.service';
import { I18nService } from './i18n.service';

export const DEFAULT_AVATAR = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0%25' y1='0%25' x2='100%25' y2='100%25'%3E%3Cstop offset='0%25' stop-color='%23B06CFF'/%3E%3Cstop offset='100%25' stop-color='%233B82F6'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='200' height='200' rx='100' fill='url(%23g)'/%3E%3Cpath d='M65 145V60L105 110V60' stroke='white' stroke-width='12' stroke-linecap='round' stroke-linejoin='round' fill='none'/%3E%3Cpath d='M120 78c10 8 16 20 16 32s-6 24-16 32' stroke='white' stroke-width='9' stroke-linecap='round' fill='none' opacity='0.9'/%3E%3Cpath d='M138 62c14 12 22 29 22 46s-8 34-22 46' stroke='white' stroke-width='8' stroke-linecap='round' fill='none' opacity='0.5'/%3E%3C/svg%3E";

interface AuthResponse {
  token: string;
  user: any;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser = signal<User | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => !!this.currentUser());

  constructor(private http: HttpClient, private router: Router, private player: PlayerService, private i18n: I18nService) {
    const stored = localStorage.getItem('newzic_user');
    if (stored) {
      this.currentUser.set(JSON.parse(stored));
    }
  }

  login(username: string, password: string): Observable<boolean> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, { username, password }).pipe(
      tap(res => this.handleAuth(res)),
      map(() => true)
    );
  }

  register(data: {
    artistName: string;
    username: string;
    email: string;
    password: string;
    role: string;
    country?: string;
    preferredGenres?: string[];
  }): Observable<boolean> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, {
      artistName: data.artistName,
      username: data.username,
      email: data.email,
      password: data.password,
      roles: [data.role],
      country: data.country || null,
      preferredGenres: data.preferredGenres || []
    }).pipe(
      tap(res => this.handleAuth(res)),
      map(() => true)
    );
  }

  logout(): void {
    this.player.stop();
    this.currentUser.set(null);
    localStorage.removeItem('newzic_user');
    localStorage.removeItem('newzic_token');
    this.router.navigate(['/login']);
  }

  refreshUser(): void {
    this.http.get<any>(`${environment.apiUrl}/users/me`).subscribe(u => {
      const user = this.mapUser(u);
      this.currentUser.set(user);
      localStorage.setItem('newzic_user', JSON.stringify(user));
    });
  }

  private handleAuth(res: AuthResponse): void {
    localStorage.setItem('newzic_token', res.token);
    const user = this.mapUser(res.user);
    this.currentUser.set(user);
    localStorage.setItem('newzic_user', JSON.stringify(user));
    // Only apply server language if user hasn't manually chosen one locally
    const localLang = localStorage.getItem('newzic_lang');
    if (!localLang && user.preferredLanguage) {
      this.i18n.setLanguage(user.preferredLanguage);
    }
  }

  private mapUser(u: any): User {
    return {
      id: u.id,
      username: u.username,
      email: u.email,
      displayName: u.displayName,
      avatar: u.avatar || DEFAULT_AVATAR,
      role: u.roles?.[0] || 'singer',
      bio: u.bio,
      socialLinks: u.socialLinks,
      joinedDate: u.joinedDate,
      followers: u.followers || 0,
      following: u.following || 0,
      country: u.country,
      preferredGenres: u.preferredGenres,
      preferredLanguage: u.preferredLanguage,
      premium: u.premium || false,
      verified: u.verified || false,
      customUrl: u.customUrl
    };
  }
}
