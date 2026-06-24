import { Component, Input, Output, EventEmitter } from '@angular/core';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { PremiumService } from '../../../core/services/premium.service';

@Component({
  selector: 'app-upgrade-modal',
  standalone: true,
  imports: [TranslatePipe],
  template: `
    <div class="modal-overlay" (click)="close.emit()">
      <div class="upgrade-modal" (click)="$event.stopPropagation()">
        <div class="upgrade-glow"></div>
        <div class="upgrade-header">
          <div class="upgrade-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="url(#upGrad)"/>
              <defs><linearGradient id="upGrad" x1="0" y1="0" x2="24" y2="24"><stop stop-color="#A855F7"/><stop offset="1" stop-color="#3B82F6"/></linearGradient></defs>
            </svg>
          </div>
          <h2>NEWZIC PREMIUM</h2>
          @if (limitMessage) {
            <p class="limit-msg">{{ limitMessage }}</p>
          }
        </div>

        <div class="upgrade-features">
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.unlimited_songs' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.unlimited_journal' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.unlimited_workspaces' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.unlimited_collaborators' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.unlimited_comments' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.premium_badge' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.verified_profile' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.advanced_analytics' | translate }}</span>
          </div>
          <div class="feature-item">
            <span class="check">✓</span>
            <span>{{ 'premium.feature.discovery_boost' | translate }}</span>
          </div>
        </div>

        <div class="upgrade-price">
          <span class="price">€9,99</span>
          <span class="period">/{{ 'premium.month' | translate }}</span>
        </div>

        <div class="upgrade-actions">
          <button class="upgrade-btn" (click)="onUpgrade()">
            🚀 {{ 'premium.upgrade_btn' | translate }}
          </button>
          <button class="skip-btn" (click)="close.emit()">
            {{ 'premium.not_now' | translate }}
          </button>
        </div>
      </div>
    </div>
  `,
  styleUrl: './upgrade-modal.component.scss'
})
export class UpgradeModalComponent {
  @Input() limitMessage: string | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() upgraded = new EventEmitter<void>();

  constructor(private premiumService: PremiumService) {}

  onUpgrade() {
    this.premiumService.activate().subscribe(() => {
      this.upgraded.emit();
      this.close.emit();
    });
  }
}
