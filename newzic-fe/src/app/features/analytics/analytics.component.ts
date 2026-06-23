import { Component, OnInit, signal } from '@angular/core';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { PremiumService } from '../../core/services/premium.service';
import { AdvancedAnalytics, DonationDashboard } from '../../core/models';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [TranslatePipe, FormatNumberPipe],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.scss'
})
export class AnalyticsComponent implements OnInit {
  analytics = signal<AdvancedAnalytics | null>(null);
  donations = signal<DonationDashboard | null>(null);
  loading = signal(true);
  activeSection = signal<'audience' | 'songs' | 'growth' | 'donations'>('audience');

  constructor(private premiumService: PremiumService) {}

  ngOnInit(): void {
    this.premiumService.getAdvancedAnalytics().subscribe({
      next: a => { this.analytics.set(a); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
    this.premiumService.getDonationDashboard().subscribe(d => this.donations.set(d));
  }

  getTrendIcon(trend: string): string {
    return trend === 'up' ? '📈' : trend === 'down' ? '📉' : '➡️';
  }

  formatCents(cents: number): string {
    return (cents / 100).toFixed(2);
  }
}
