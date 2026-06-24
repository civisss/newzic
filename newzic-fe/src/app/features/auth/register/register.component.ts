import { Component, signal, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LogoComponent } from '../../../shared/components/logo/logo.component';
import { AuthService } from '../../../core/services/auth.service';
import { SongService } from '../../../core/services/song.service';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { I18nService } from '../../../core/services/i18n.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink, LogoComponent, TranslatePipe],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  artistName = '';
  username = '';
  email = '';
  password = '';
  confirmPassword = '';
  country = '';
  selectedRoles: string[] = [];
  selectedGenres: string[] = [];
  acceptTerms = false;
  acceptAge = false;
  step = signal(1);
  error = signal('');
  loading = signal(false);
  fieldErrors = signal<Record<string, string>>({});

  countries = [
    { code: 'IT', label: 'Italy' },
    { code: 'ES', label: 'Spain' },
    { code: 'DE', label: 'Germany' },
    { code: 'GB', label: 'United Kingdom' },
    { code: 'US', label: 'United States' },
    { code: 'JP', label: 'Japan' },
    { code: 'FR', label: 'France' },
    { code: 'BR', label: 'Brazil' },
    { code: 'KR', label: 'South Korea' },
    { code: 'CA', label: 'Canada' },
    { code: 'AU', label: 'Australia' },
    { code: 'SE', label: 'Sweden' },
    { code: 'NL', label: 'Netherlands' },
    { code: 'MX', label: 'Mexico' },
    { code: 'AR', label: 'Argentina' },
    { code: 'NG', label: 'Nigeria' },
    { code: 'IN', label: 'India' },
    { code: 'ZA', label: 'South Africa' },
  ];

  availableGenres = [
    'Dream Pop', 'Trap', 'Indie Rock', 'K-Pop', 'House', 'Techno',
    'Neo-Soul', 'R&B', 'Lo-Fi', 'Ambient', 'Future Bass', 'Reggaeton',
    'Hip-Hop', 'Pop', 'Post-Rock', 'Latin Pop', 'Electronic', 'Jazz',
  ];

  coversTop: string[] = [];
  coversBottom: string[] = [];

  primaryRoles = [
    { value: 'singer', label: 'Singer', icon: '🎤' },
    { value: 'band', label: 'Band', icon: '🎸' },
  ];

  secondaryRoles = [
    { value: 'producer', label: 'Producer', icon: '🎛️' },
    { value: 'musician', label: 'Musician', icon: '🎹' },
    { value: 'beatmaker', label: 'Beatmaker', icon: '🥁' },
  ];

  constructor(private auth: AuthService, private router: Router, public i18n: I18nService, private songService: SongService) {}

  ngOnInit(): void {
    this.songService.getCovers(24).subscribe(covers => {
      const half = Math.ceil(covers.length / 2);
      this.coversTop = covers.slice(0, half);
      this.coversBottom = covers.slice(half);
    });
  }

  toggleRole(value: string): void {
    // Singer and Band are mutually exclusive
    if (value === 'singer' || value === 'band') {
      const other = value === 'singer' ? 'band' : 'singer';
      this.selectedRoles = this.selectedRoles.filter(r => r !== other);
    }

    if (this.selectedRoles.includes(value)) {
      this.selectedRoles = this.selectedRoles.filter(r => r !== value);
    } else {
      this.selectedRoles = [...this.selectedRoles, value];
    }
  }

  isSelected(value: string): boolean {
    return this.selectedRoles.includes(value);
  }

  toggleGenre(genre: string): void {
    if (this.selectedGenres.includes(genre)) {
      this.selectedGenres = this.selectedGenres.filter(g => g !== genre);
    } else {
      this.selectedGenres = [...this.selectedGenres, genre];
    }
  }

  isGenreSelected(genre: string): boolean {
    return this.selectedGenres.includes(genre);
  }

  validateField(field: string): void {
    const errors = { ...this.fieldErrors() };
    switch (field) {
      case 'email':
        if (this.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) {
          errors['email'] = 'Invalid email format';
        } else {
          delete errors['email'];
        }
        break;
      case 'password':
        if (this.password && (this.password.length < 8 || this.password.length > 128)) {
          errors['password'] = 'Password must be 8–128 characters';
        } else {
          delete errors['password'];
        }
        if (this.confirmPassword && this.password !== this.confirmPassword) {
          errors['confirmPassword'] = 'Passwords do not match';
        } else {
          delete errors['confirmPassword'];
        }
        break;
      case 'confirmPassword':
        if (this.confirmPassword && this.password !== this.confirmPassword) {
          errors['confirmPassword'] = 'Passwords do not match';
        } else {
          delete errors['confirmPassword'];
        }
        break;
      case 'username':
        if (this.username && this.username.length < 3) {
          errors['username'] = 'Username must be at least 3 characters';
        } else {
          delete errors['username'];
        }
        break;
      case 'artistName':
        if (!this.artistName) {
          errors['artistName'] = 'Artist name is required';
        } else {
          delete errors['artistName'];
        }
        break;
    }
    this.fieldErrors.set(errors);
  }

  hasError(field: string): boolean {
    return !!this.fieldErrors()[field];
  }

  nextStep(): void {
    this.error.set('');
    this.fieldErrors.set({});
    if (!this.artistName || !this.username || !this.email || !this.password || !this.confirmPassword) {
      this.error.set(this.i18n.t('auth.fill_all_fields'));
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) {
      this.fieldErrors.set({ email: 'Invalid email format' });
      return;
    }
    if (this.password.length < 8) {
      this.fieldErrors.set({ password: 'Password must be at least 8 characters' });
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.fieldErrors.set({ confirmPassword: 'Passwords do not match' });
      return;
    }
    if (this.selectedRoles.length === 0) {
      this.error.set(this.i18n.t('auth.select_role'));
      return;
    }
    this.step.set(2);
  }

  prevStep(): void {
    this.error.set('');
    this.step.set(1);
  }

  register(): void {
    this.error.set('');

    if (this.password !== this.confirmPassword) {
      this.error.set('Passwords do not match');
      return;
    }
    if (this.selectedRoles.length === 0) {
      this.error.set(this.i18n.t('auth.select_role'));
      return;
    }
    if (!this.acceptTerms) {
      this.error.set(this.i18n.t('auth.must_accept_terms'));
      return;
    }
    if (!this.acceptAge) {
      this.error.set(this.i18n.t('auth.must_accept_age'));
      return;
    }

    this.loading.set(true);
    this.auth.register({
      artistName: this.artistName,
      username: this.username,
      email: this.email,
      password: this.password,
      role: this.selectedRoles[0],
      country: this.country || undefined,
      preferredGenres: this.selectedGenres
    }).subscribe({
      next: () => {
        this.router.navigate(['/home']);
        this.loading.set(false);
      },
      error: (err) => {
        const body = err?.error;
        if (body?.details) {
          const errors: Record<string, string> = {};
          for (const [key, msgs] of Object.entries(body.details)) {
            errors[key] = Array.isArray(msgs) ? msgs[0] : String(msgs);
          }
          this.fieldErrors.set(errors);
          if (Object.keys(errors).some(k => ['email', 'password', 'username', 'artistName'].includes(k))) {
            this.step.set(1);
          }
        }
        this.error.set(body?.error || body?.message || 'Registration failed. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
