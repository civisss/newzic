import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { AuthService } from '../../core/services/auth.service';
import { I18nService } from '../../core/services/i18n.service';
import { StatsService } from '../../core/services/stats.service';
import { SongService } from '../../core/services/song.service';
import { PlayerService } from '../../core/services/player.service';
import { ArtistStats, Song } from '../../core/models';
import { environment } from '../../../environments/environment';
import { FollowersModalComponent } from '../../shared/components/followers-modal/followers-modal.component';
import { UpgradeModalComponent } from '../../shared/components/upgrade-modal/upgrade-modal.component';
import { PremiumBadgeComponent } from '../../shared/components/premium-badge/premium-badge.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [RouterLink, FormatNumberPipe, FormsModule, TranslatePipe, FollowersModalComponent, UpgradeModalComponent, PremiumBadgeComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  stats = signal<ArtistStats | null>(null);
  mySongs = signal<Song[]>([]);
  likedSongs = signal<Song[]>([]);
  songsLoading = signal(true);
  activeTab = signal<'overview' | 'liked' | 'settings'>('overview');
  savingPrefs = signal(false);
  savingAvatar = signal(false);
  avatarPreview = signal('');
  showFollowModal = signal(false);
  showUpgradeModal = signal(false);
  followModalMode = signal<'followers' | 'following'>('followers');
  editCountry = '';
  editPreferredGenres: string[] = [];
  editSocialLinks: { spotify: string; youtubeMusic: string; appleMusic: string; soundcloud: string; tiktok: string; instagram: string } = {
    spotify: '', youtubeMusic: '', appleMusic: '', soundcloud: '', tiktok: '', instagram: ''
  };
  savingSocials = signal(false);

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
    public i18n: I18nService,
    private statsService: StatsService,
    private songService: SongService,
    private playerService: PlayerService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.auth.refreshUser();
    this.statsService.getMyStats().subscribe(s => this.stats.set(s));
    const user = this.auth.user();
    if (user) {
      this.songService.getByArtist(user.id).subscribe(songs => {
        this.mySongs.set(songs);
        this.songsLoading.set(false);
      });
      this.songService.getLikedSongs().subscribe(songs => this.likedSongs.set(songs));
      this.editCountry = user.country || '';
      this.editPreferredGenres = [...(user.preferredGenres || [])];
      const sl = user.socialLinks;
      if (sl) {
        this.editSocialLinks = {
          spotify: sl.spotify || '',
          youtubeMusic: sl.youtubeMusic || '',
          appleMusic: sl.appleMusic || '',
          soundcloud: sl.soundcloud || '',
          tiktok: sl.tiktok || '',
          instagram: sl.instagram || ''
        };
      }
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

  openFollowersModal(mode: 'followers' | 'following'): void {
    this.followModalMode.set(mode);
    this.showFollowModal.set(true);
  }

  closeFollowModal(): void {
    this.showFollowModal.set(false);
  }

  saveSocialLinks(): void {
    this.savingSocials.set(true);
    const links: any = {};
    if (this.editSocialLinks.spotify) links.spotify = this.editSocialLinks.spotify;
    if (this.editSocialLinks.youtubeMusic) links.youtubeMusic = this.editSocialLinks.youtubeMusic;
    if (this.editSocialLinks.appleMusic) links.appleMusic = this.editSocialLinks.appleMusic;
    if (this.editSocialLinks.soundcloud) links.soundcloud = this.editSocialLinks.soundcloud;
    if (this.editSocialLinks.tiktok) links.tiktok = this.editSocialLinks.tiktok;
    if (this.editSocialLinks.instagram) links.instagram = this.editSocialLinks.instagram;
    this.http.patch<any>(`${environment.apiUrl}/users/me`, {
      socialLinks: links
    }).subscribe({
      next: () => {
        this.auth.refreshUser();
        this.savingSocials.set(false);
      },
      error: () => this.savingSocials.set(false)
    });
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
