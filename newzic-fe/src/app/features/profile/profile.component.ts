import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { AuthService } from '../../core/services/auth.service';
import { StatsService } from '../../core/services/stats.service';
import { SongService } from '../../core/services/song.service';
import { PlayerService } from '../../core/services/player.service';
import { ArtistStats, Song } from '../../core/models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [RouterLink, FormatNumberPipe, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  stats = signal<ArtistStats | null>(null);
  mySongs = signal<Song[]>([]);
  activeTab = signal<'overview' | 'settings'>('overview');
  savingPrefs = signal(false);
  savingAvatar = signal(false);
  avatarPreview = signal('');
  editCountry = '';
  editPreferredGenres: string[] = [];

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

  constructor(
    public auth: AuthService,
    private statsService: StatsService,
    private songService: SongService,
    private playerService: PlayerService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.statsService.getMyStats().subscribe(s => this.stats.set(s));
    const user = this.auth.user();
    if (user) {
      this.songService.getByArtist(user.id).subscribe(songs => this.mySongs.set(songs));
      this.editCountry = user.country || '';
      this.editPreferredGenres = [...(user.preferredGenres || [])];
    }
  }

  playSong(song: Song): void {
    this.playerService.play(song, this.mySongs());
  }

  onAvatarSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const dataUri = e.target?.result as string;
        this.avatarPreview.set(dataUri);
        this.saveAvatar(dataUri);
      };
      reader.readAsDataURL(input.files[0]);
    }
  }

  private saveAvatar(dataUri: string): void {
    this.savingAvatar.set(true);
    this.http.patch<any>(`${environment.apiUrl}/users/me`, {
      avatar: dataUri
    }).subscribe({
      next: (updated) => {
        const user = this.auth.user();
        if (user) {
          user.avatar = updated.avatar;
        }
        this.savingAvatar.set(false);
      },
      error: () => this.savingAvatar.set(false)
    });
  }

  getMaxPlays(): number {
    const s = this.stats();
    if (!s) return 1;
    return Math.max(...s.weeklyData.map(w => w.plays));
  }

  getBarHeight(plays: number): number {
    return (plays / this.getMaxPlays()) * 100;
  }

  getTrendIcon(trend: string): string {
    return trend === 'up' ? '↑' : trend === 'down' ? '↓' : '→';
  }

  getTrendClass(trend: string): string {
    return trend === 'up' ? 'trend-up' : trend === 'down' ? 'trend-down' : 'trend-stable';
  }

  togglePrefGenre(genre: string): void {
    if (this.editPreferredGenres.includes(genre)) {
      this.editPreferredGenres = this.editPreferredGenres.filter(g => g !== genre);
    } else {
      this.editPreferredGenres = [...this.editPreferredGenres, genre];
    }
  }

  isPrefGenreSelected(genre: string): boolean {
    return this.editPreferredGenres.includes(genre);
  }

  savePreferences(): void {
    this.savingPrefs.set(true);
    this.http.patch<any>(`${environment.apiUrl}/users/me`, {
      country: this.editCountry || null,
      preferredGenres: this.editPreferredGenres
    }).subscribe({
      next: (updated) => {
        const user = this.auth.user();
        if (user) {
          user.country = updated.country;
          user.preferredGenres = updated.preferredGenres;
        }
        this.savingPrefs.set(false);
      },
      error: () => this.savingPrefs.set(false)
    });
  }
}
