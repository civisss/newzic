import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-verified-badge',
  standalone: true,
  template: `
    <span class="verified-badge" [class.sm]="size === 'sm'" [title]="'Verified'">
      <svg [attr.width]="size === 'sm' ? 14 : 16" [attr.height]="size === 'sm' ? 14 : 16" viewBox="0 0 24 24" fill="none">
        <path d="M9 12l2 2 4-4" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        <circle cx="12" cy="12" r="10" fill="url(#verGrad)" stroke="none"/>
        <path d="M9 12l2 2 4-4" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        <defs><linearGradient id="verGrad" x1="0" y1="0" x2="24" y2="24"><stop stop-color="#A855F7"/><stop offset="1" stop-color="#3B82F6"/></linearGradient></defs>
      </svg>
    </span>
  `,
  styles: [`
    .verified-badge {
      display: inline-flex; align-items: center;
      vertical-align: middle; margin-left: 4px;
    }
    .verified-badge svg { filter: drop-shadow(0 0 3px rgba(168,85,247,0.3)); }
  `]
})
export class VerifiedBadgeComponent {
  @Input() size: 'sm' | 'md' = 'md';
}
