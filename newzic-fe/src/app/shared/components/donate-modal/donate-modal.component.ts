import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { PremiumService } from '../../../core/services/premium.service';

@Component({
  selector: 'app-donate-modal',
  standalone: true,
  imports: [DecimalPipe, FormsModule, TranslatePipe],
  template: `
    <div class="modal-overlay" (click)="close.emit()">
      <div class="donate-modal" (click)="$event.stopPropagation()">
        <h2>💜 {{ 'donate.title' | translate }}</h2>
        <p class="donate-subtitle">{{ artistName }}</p>

        <div class="amount-grid">
          @for (amt of presetAmounts; track amt) {
            <button class="amount-btn" [class.active]="selectedAmount() === amt"
                    (click)="selectAmount(amt)">
              €{{ (amt / 100) | number:'1.0-0' }}
            </button>
          }
        </div>

        <div class="custom-amount">
          <label>{{ 'donate.custom_amount' | translate }}</label>
          <div class="amount-input-wrap">
            <span class="currency">€</span>
            <input type="number" min="1" step="1" [(ngModel)]="customAmountEuro"
                   (input)="onCustomInput()" [placeholder]="'0'" />
          </div>
        </div>

        <div class="donate-message">
          <label>{{ 'donate.message_label' | translate }}</label>
          <textarea [(ngModel)]="message" rows="2"
                    [placeholder]="'donate.message_placeholder' | translate"></textarea>
        </div>

        <div class="transparency">
          <p>{{ 'donate.transparency' | translate }}</p>
          @if (selectedAmount() > 0) {
            <div class="breakdown">
              <div class="breakdown-row">
                <span>{{ 'donate.donation' | translate }}</span>
                <strong>€{{ (selectedAmount() / 100).toFixed(2) }}</strong>
              </div>
              <div class="breakdown-row artist">
                <span>{{ 'donate.to_artist' | translate }}</span>
                <strong>€{{ (artistCents() / 100).toFixed(2) }}</strong>
              </div>
              <div class="breakdown-row platform">
                <span>Newzic</span>
                <span>€{{ (platformCents() / 100).toFixed(2) }}</span>
              </div>
            </div>
          }
        </div>

        <div class="modal-actions">
          <button class="btn-ghost" (click)="close.emit()">{{ 'donate.cancel' | translate }}</button>
          <button class="donate-btn" (click)="submitDonation()" [disabled]="selectedAmount() < 100 || sending()">
            💜 {{ 'donate.send_btn' | translate }}
          </button>
        </div>
      </div>
    </div>
  `,
  styleUrl: './donate-modal.component.scss'
})
export class DonateModalComponent {
  @Input() artistId = '';
  @Input() artistName = '';
  @Output() close = new EventEmitter<void>();
  @Output() donated = new EventEmitter<void>();

  presetAmounts = [100, 300, 500, 1000, 2500];
  selectedAmount = signal(0);
  customAmountEuro = '';
  message = '';
  sending = signal(false);

  constructor(private premiumService: PremiumService) {}

  artistCents = () => Math.round(this.selectedAmount() * 0.95);
  platformCents = () => this.selectedAmount() - this.artistCents();

  selectAmount(cents: number) {
    this.selectedAmount.set(cents);
    this.customAmountEuro = '';
  }

  onCustomInput() {
    const val = parseFloat(this.customAmountEuro);
    if (!isNaN(val) && val >= 1) {
      this.selectedAmount.set(Math.round(val * 100));
    } else {
      this.selectedAmount.set(0);
    }
  }

  submitDonation() {
    if (this.selectedAmount() < 100) return;
    this.sending.set(true);
    this.premiumService.donate(this.artistId, this.selectedAmount(), this.message || undefined)
      .subscribe({
        next: () => {
          this.donated.emit();
          this.close.emit();
        },
        error: () => this.sending.set(false)
      });
  }
}
