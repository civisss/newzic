import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LogoComponent } from '../../../shared/components/logo/logo.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink, LogoComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  artistName = '';
  username = '';
  email = '';
  password = '';
  confirmPassword = '';
  country = '';
  selectedRoles: string[] = [];
  selectedGenres: string[] = [];
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

  coversTop = [
    'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1501612780327-45045538702b?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1506157786151-b8491531f063?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1484755560615-a4c64e778a6c?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?w=200&h=200&fit=crop',
  ];

  coversBottom = [
    'https://images.unsplash.com/photo-1571075051280-29b8faee2cb7?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1504898770365-14faca6a7320?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1446057032654-9d8885f8a13e?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1508854710579-5cecc3a9ff17?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1485579149621-3123dd979885?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1460667262436-cf19894f4774?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=200&h=200&fit=crop',
    'https://images.unsplash.com/photo-1526142684086-7ebd69df27a5?w=200&h=200&fit=crop',
  ];

  primaryRoles = [
    { value: 'singer', label: 'Singer', icon: '🎤' },
    { value: 'band', label: 'Band', icon: '🎸' },
  ];

  secondaryRoles = [
    { value: 'producer', label: 'Producer', icon: '🎛️' },
    { value: 'musician', label: 'Musician', icon: '🎹' },
    { value: 'beatmaker', label: 'Beatmaker', icon: '🥁' },
  ];

  constructor(private auth: AuthService, private router: Router) {}

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
      this.error.set('Please fill in all fields');
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
      this.error.set('Please select at least one role');
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
      this.error.set('Please select at least one role');
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
