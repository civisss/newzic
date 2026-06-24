import { Component, signal, HostListener, ElementRef, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LogoComponent } from '../../../shared/components/logo/logo.component';
import { AuthService } from '../../../core/services/auth.service';
import { SongService } from '../../../core/services/song.service';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink, LogoComponent, TranslatePipe],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  error = signal('');
  loading = signal(false);

  coversTop: string[] = [];
  coversBottom: string[] = [];

  constructor(private auth: AuthService, private router: Router, private el: ElementRef, private songService: SongService) {
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/home']);
    }
  }

  ngOnInit(): void {
    this.songService.getCovers(30).subscribe(covers => {
      this.coversTop = covers;
      this.coversBottom = covers;
    });
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
