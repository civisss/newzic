import { Component, signal, HostListener, ElementRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LogoComponent } from '../../../shared/components/logo/logo.component';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, LogoComponent, TranslatePipe],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username = '';
  password = '';
  error = signal('');
  loading = signal(false);

  coversTop = [
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1501612780327-45045538702b?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1571330735066-03aaa9429d89?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=200&h=200&fit=crop&q=80',
  ];

  coversBottom = [
    'https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1484755560615-a4c64e778a6c?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1526142684086-7ebd69df27a5?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1460667262436-cf19894f4774?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1508854710579-5cecc3a9ff17?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1485579149621-3123dd979885?w=200&h=200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1446057032654-9d8885f8a13e?w=200&h=200&fit=crop&q=80',
  ];

  constructor(private auth: AuthService, private router: Router, private el: ElementRef) {
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/home']);
    }
  }

  @HostListener('mousemove', ['$event'])
  onMouseMove(e: MouseEvent): void {
    const orbs = this.el.nativeElement.querySelectorAll('.orb');
    const cx = window.innerWidth / 2;
    const cy = window.innerHeight / 2;
    const dx = (e.clientX - cx) / cx;
    const dy = (e.clientY - cy) / cy;

    orbs.forEach((orb: HTMLElement, i: number) => {
      const intensity = (i + 1) * 30;
      orb.style.transform = `translate(${dx * intensity}px, ${dy * intensity}px)`;
    });
  }

  login(): void {
    this.error.set('');
    this.loading.set(true);

    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.router.navigate(['/home']);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Invalid credentials. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
