import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-premium-badge',
  standalone: true,
  template: `
    @if (size === 'sm') {
      <span class="premium-badge sm" [title]="'Premium'">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M5 16L3 5l5.5 5L12 2l3.5 8L21 5l-2 11H5z" fill="url(#crownSm)"/>
          <path d="M5 19h14v2H5z" fill="url(#crownSm)" opacity="0.7"/>
          <defs><linearGradient id="crownSm" x1="0" y1="0" x2="24" y2="24"><stop stop-color="#FBBF24"/><stop offset="0.5" stop-color="#F59E0B"/><stop offset="1" stop-color="#A855F7"/></linearGradient></defs>
        </svg>
      </span>
    } @else {
      <span class="premium-badge" [class.lg]="size === 'lg'">
        <span class="badge-glow"></span>
        <span class="badge-shimmer"></span>
        <svg class="crown-icon" [attr.width]="size === 'lg' ? 22 : 18" [attr.height]="size === 'lg' ? 22 : 18" viewBox="0 0 24 24" fill="none">
          <path d="M5 16L3 5l5.5 5L12 2l3.5 8L21 5l-2 11H5z" fill="url(#crownMd)"/>
          <path d="M5 19h14v2H5z" fill="url(#crownMd)" opacity="0.7"/>
          <defs><linearGradient id="crownMd" x1="0" y1="0" x2="24" y2="24"><stop stop-color="#FBBF24"/><stop offset="0.5" stop-color="#F59E0B"/><stop offset="1" stop-color="#EC4899"/></linearGradient></defs>
        </svg>
        <span class="badge-text">PREMIUM</span>
      </span>
    }
  `,
  styles: [`
    :host { display: inline-flex; }

    .premium-badge {
      position: relative; overflow: hidden;
      display: inline-flex; align-items: center; gap: 6px;
      font-size: 0.72rem; font-weight: 800; letter-spacing: 0.1em;
      background: linear-gradient(135deg, rgba(251,191,36,0.18), rgba(245,158,11,0.14), rgba(251,191,36,0.12));
      border: 1.5px solid rgba(251,191,36,0.35);
      padding: 4px 12px 4px 8px; border-radius: 20px;
      animation: premiumGlow 3s ease-in-out infinite;
    }

    .premium-badge.lg {
      font-size: 0.82rem; padding: 5px 14px 5px 10px; gap: 7px;
    }

    .badge-text {
      background: linear-gradient(90deg, #FBBF24, #F59E0B, #D97706, #F59E0B, #FBBF24);
      background-size: 200% 100%;
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
      background-clip: text;
      animation: textShine 3s linear infinite;
    }

    .crown-icon {
      filter: drop-shadow(0 0 4px rgba(251,191,36,0.5));
      animation: crownBounce 2s ease-in-out infinite;
    }

    .badge-glow {
      position: absolute; inset: -1px; border-radius: inherit;
      background: conic-gradient(from 0deg, rgba(251,191,36,0.35), rgba(245,158,11,0.25), rgba(251,191,36,0.3), rgba(245,158,11,0.35));
      animation: glowRotate 4s linear infinite;
      filter: blur(4px); opacity: 0.6; z-index: -1;
    }

    .badge-shimmer {
      position: absolute; top: 0; left: -100%; width: 50%; height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
      animation: shimmerSlide 2.5s ease-in-out infinite;
      z-index: 1; pointer-events: none;
    }

    .premium-badge.sm {
      padding: 0; background: none; border: none;
      animation: none; overflow: visible;
    }
    .premium-badge.sm .badge-glow,
    .premium-badge.sm .badge-shimmer { display: none; }
    .premium-badge.sm svg {
      filter: drop-shadow(0 0 5px rgba(251,191,36,0.5)) drop-shadow(0 0 10px rgba(168,85,247,0.3));
    }

    @keyframes premiumGlow {
      0%, 100% { border-color: rgba(251,191,36,0.35); box-shadow: 0 0 8px rgba(251,191,36,0.15), 0 0 20px rgba(168,85,247,0.1); }
      50% { border-color: rgba(245,158,11,0.5); box-shadow: 0 0 12px rgba(251,191,36,0.25), 0 0 30px rgba(245,158,11,0.15); }
    }

    @keyframes textShine {
      0% { background-position: 200% center; }
      100% { background-position: -200% center; }
    }

    @keyframes crownBounce {
      0%, 100% { transform: translateY(0) rotate(0deg); }
      25% { transform: translateY(-1px) rotate(-3deg); }
      75% { transform: translateY(-1px) rotate(3deg); }
    }

    @keyframes glowRotate {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    @keyframes shimmerSlide {
      0%, 100% { left: -100%; }
      50% { left: 200%; }
    }
  `]
})
export class PremiumBadgeComponent {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
}
