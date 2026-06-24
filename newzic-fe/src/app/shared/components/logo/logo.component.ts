import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-logo',
  standalone: true,
  template: `
    <a [routerLink]="clickable ? '/home' : null" class="logo-wrap" [style.gap.px]="gap">
      <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" class="logo-icon">
        <defs>
          <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#B06CFF"/>
            <stop offset="100%" stop-color="#3B82F6"/>
          </linearGradient>
          <linearGradient id="waveGrad" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#EC4899"/>
            <stop offset="100%" stop-color="#B06CFF"/>
          </linearGradient>
        </defs>
        <rect width="48" height="48" rx="14" fill="url(#logoGrad)"/>
        <!-- Stylized N -->
        <path d="M14 35V14L24 26V14" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
        <!-- Sound wave arcs -->
        <path d="M29 18c2.5 2 4 5 4 8s-1.5 6-4 8" stroke="white" stroke-width="2.2" stroke-linecap="round" fill="none" opacity="0.9"/>
        <path d="M33 14.5c3.5 3 5.5 7 5.5 11.5s-2 8.5-5.5 11.5" stroke="white" stroke-width="2" stroke-linecap="round" fill="none" opacity="0.5"/>
      </svg>
      @if (showText) {
        <span class="logo-text" [style.font-size.px]="textSize">NEWZIC</span>
        @if (isPremium) {
          <span class="logo-premium" [style.font-size.px]="textSize * 0.5">PREMIUM</span>
        }
      }
    </a>
  `,
  styles: [`
    :host {
      display: inline-flex;
      align-items: center;
    }
    .logo-wrap {
      display: inline-flex;
      align-items: center;
      text-decoration: none;
      color: inherit;
      line-height: 1;
    }

    .logo-icon {
      flex-shrink: 0;
    }

    .logo-text {
      font-family: 'Space Grotesk', sans-serif;
      font-weight: 800;
      letter-spacing: 3px;
      background: linear-gradient(135deg, #B06CFF 0%, #3B82F6 50%, #B06CFF 100%);
      background-size: 200% 100%;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      animation: logoShimmer 4s ease infinite;
      filter: drop-shadow(0 0 8px rgba(176, 108, 255, 0.4));
    }
    @keyframes logoShimmer {
      0%, 100% { background-position: 0% 50%; }
      50% { background-position: 100% 50%; }
    }
    .logo-premium {
      font-family: 'Space Grotesk', sans-serif;
      font-weight: 700;
      letter-spacing: 2px;
      background: linear-gradient(135deg, #A855F7, #3B82F6);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-left: 6px;
      opacity: 0.85;
      align-self: center;
    }
  `],
  imports: [RouterLink]
})
export class LogoComponent {
  @Input() size = 36;
  @Input() textSize = 20;
  @Input() gap = 10;
  @Input() showText = true;
  @Input() clickable = true;
  @Input() isPremium = false;
}
