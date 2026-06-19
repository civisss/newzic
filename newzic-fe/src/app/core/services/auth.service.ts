import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, map } from 'rxjs';
import { User } from '../models';
import { environment } from '../../../environments/environment';
import { PlayerService } from './player.service';

interface AuthResponse {
  token: string;
  user: any;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser = signal<User | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => !!this.currentUser());

  constructor(private http: HttpClient, private router: Router, private player: PlayerService) {
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
  }

  private mapUser(u: any): User {
    return {
      id: u.id,
      username: u.username,
      email: u.email,
      displayName: u.displayName,
      avatar: u.avatar || '',
      role: u.roles?.[0] || 'singer',
      bio: u.bio,
      socialLinks: u.socialLinks,
      joinedDate: u.joinedDate
    };
  }
}
