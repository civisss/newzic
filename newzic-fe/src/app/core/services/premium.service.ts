import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PremiumStatus,
  PremiumLimits,
  Donation,
  DonationDashboard,
  AdvancedAnalytics
} from '../models';

@Injectable({ providedIn: 'root' })
export class PremiumService {

  private api = `${environment.apiUrl}/premium`;
  private statsApi = `${environment.apiUrl}/stats`;

  readonly status = signal<PremiumStatus | null>(null);
  readonly limits = signal<PremiumLimits | null>(null);

  constructor(private http: HttpClient) {}

  loadStatus(): Observable<PremiumStatus> {
    return this.http.get<PremiumStatus>(`${this.api}/status`).pipe(
      tap(s => this.status.set(s))
    );
  }

  loadLimits(): Observable<PremiumLimits> {
    return this.http.get<PremiumLimits>(`${this.api}/limits`).pipe(
      tap(l => this.limits.set(l))
    );
  }

  activate(): Observable<PremiumStatus> {
    return this.http.post<PremiumStatus>(`${this.api}/activate`, {}).pipe(
      tap(s => this.status.set(s))
    );
  }

  deactivate(): Observable<PremiumStatus> {
    return this.http.post<PremiumStatus>(`${this.api}/deactivate`, {}).pipe(
      tap(s => this.status.set(s))
    );
  }

  setCustomUrl(customUrl: string): Observable<PremiumStatus> {
    return this.http.put<PremiumStatus>(`${this.api}/custom-url`, { customUrl }).pipe(
      tap(s => this.status.set(s))
    );
  }

  donate(artistId: string, amountCents: number, message?: string): Observable<Donation> {
    return this.http.post<Donation>(`${this.api}/donate`, { artistId, amountCents, message });
  }

  getDonationDashboard(): Observable<DonationDashboard> {
    return this.http.get<DonationDashboard>(`${this.api}/donations/dashboard`);
  }

  getArtistDonationDashboard(artistId: string): Observable<DonationDashboard> {
    return this.http.get<DonationDashboard>(`${this.api}/donations/${artistId}`);
  }

  getAdvancedAnalytics(): Observable<AdvancedAnalytics> {
    return this.http.get<AdvancedAnalytics>(`${this.statsApi}/analytics`);
  }

  isPremiumRequired(error: any): { limitType: string; message: string } | null {
    if (error?.status === 402 && error?.error?.limitType) {
      return { limitType: error.error.limitType, message: error.error.error };
    }
    return null;
  }
}
