import { Component, OnInit, signal, computed, ViewChild, ElementRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { AuthService } from '../../core/services/auth.service';
import { JournalService } from '../../core/services/journal.service';
import { I18nService } from '../../core/services/i18n.service';
import { StatsService } from '../../core/services/stats.service';
import { SongService } from '../../core/services/song.service';
import { PlayerService } from '../../core/services/player.service';
import { ArtistStats, Song, JournalPost, CreateJournalPostRequest, Artist } from '../../core/models';
import { environment } from '../../../environments/environment';
import { FollowersModalComponent } from '../../shared/components/followers-modal/followers-modal.component';
import { UpgradeModalComponent } from '../../shared/components/upgrade-modal/upgrade-modal.component';
import { PremiumBadgeComponent } from '../../shared/components/premium-badge/premium-badge.component';
import { JournalPostCardComponent } from '../../shared/components/journal-post-card/journal-post-card.component';
import { ArtistService } from '../../core/services/artist.service';
import { Subject, of } from 'rxjs';
import { debounceTime, switchMap, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormatNumberPipe, FormsModule, TranslatePipe, FollowersModalComponent, UpgradeModalComponent, PremiumBadgeComponent, JournalPostCardComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  stats = signal<ArtistStats | null>(null);
  mySongs = signal<Song[]>([]);
  likedSongs = signal<Song[]>([]);
  songsLoading = signal(true);
  activeTab = signal<'overview' | 'journal' | 'liked' | 'settings'>('overview');
  journalPosts = signal<JournalPost[]>([]);
  journalLoading = signal(false);
  newPostContent = signal('');
  newPostCategory = signal('update');
  postingJournal = signal(false);
  showNewPostForm = signal(false);
  journalPostCount = signal(0);
  journalLimit = 10;
  upgradeLimitMessage = signal<string | null>(null);
  savingPrefs = signal(false);
  savingAvatar = signal(false);
  avatarPreview = signal('');
  showFollowModal = signal(false);
  showUpgradeModal = signal(false);
  followModalMode = signal<'followers' | 'following'>('followers');

  // Mention autocomplete
  mentionSuggestions = signal<Artist[]>([]);
  showMentionDropdown = signal(false);
  mentionQuery = signal('');
  mentionCursorPos = 0;
  mentionStartPos = 0;
  private mentionSearch$ = new Subject<string>();
  @ViewChild('postTextarea') postTextarea!: ElementRef<HTMLTextAreaElement>;
  @ViewChild('highlightOverlay') highlightOverlay!: ElementRef<HTMLDivElement>;

  highlightedContent = computed(() => {
    const text = this.newPostContent();
    if (!text) return '';
    const escaped = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
    return escaped.replace(/@(\w+)/g, '<span class="hl-mention">@$1</span>') + '\n';
  });
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
    private http: HttpClient,
    private journalService: JournalService,
    private artistService: ArtistService,
    private location: Location
  ) {
    this.mentionSearch$.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap(q => q.length >= 1 ? this.artistService.search(q) : of([]))
    ).subscribe(results => {
      this.mentionSuggestions.set(results.slice(0, 6));
      this.showMentionDropdown.set(results.length > 0);
    });
  }

  ngOnInit(): void {
    this.auth.refreshUser();
    this.statsService.getMyStats().subscribe(s => this.stats.set(s));
    const user = this.auth.user();
    if (user) {
      this.location.replaceState('/artist/' + user.username);
      this.songService.getByArtist(user.id).subscribe(songs => {
        this.mySongs.set(songs);
        this.songsLoading.set(false);
      });
      this.songService.getLikedSongs().subscribe(songs => this.likedSongs.set(songs));
      this.loadJournal();
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

  loadJournal(): void {
    const user = this.auth.user();
    if (!user) return;
    this.journalLoading.set(true);
    this.journalService.getPostsByAuthor(user.id).subscribe(res => {
      this.journalPosts.set(res.content);
      this.journalPostCount.set(res.totalElements);
      this.journalLoading.set(false);
    });
  }

  onPostInput(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    const value = textarea.value;
    const cursorPos = textarea.selectionStart || 0;
    this.mentionCursorPos = cursorPos;

    // Find the @ before the cursor
    const textBeforeCursor = value.substring(0, cursorPos);
    const atIndex = textBeforeCursor.lastIndexOf('@');

    if (atIndex >= 0) {
      const textAfterAt = textBeforeCursor.substring(atIndex + 1);
      // Only trigger if there's no space after @ (still typing the mention)
      if (!textAfterAt.includes(' ') && !textAfterAt.includes('\n')) {
        this.mentionStartPos = atIndex;
        this.mentionQuery.set(textAfterAt);
        this.mentionSearch$.next(textAfterAt);
        return;
      }
    }
    this.closeMentionDropdown();
  }

  selectMention(artist: Artist): void {
    const value = this.newPostContent();
    const before = value.substring(0, this.mentionStartPos);
    const after = value.substring(this.mentionCursorPos);
    const newValue = `${before}@${artist.username} ${after}`;
    this.newPostContent.set(newValue);
    this.closeMentionDropdown();

    // Refocus textarea and place cursor after the inserted mention
    setTimeout(() => {
      const textarea = this.postTextarea?.nativeElement;
      if (textarea) {
        const newPos = before.length + artist.username.length + 2; // +2 for @ and space
        textarea.focus();
        textarea.setSelectionRange(newPos, newPos);
      }
    });
  }

  closeMentionDropdown(): void {
    this.showMentionDropdown.set(false);
    this.mentionSuggestions.set([]);
  }

  syncScroll(): void {
    if (this.highlightOverlay && this.postTextarea) {
      this.highlightOverlay.nativeElement.scrollTop = this.postTextarea.nativeElement.scrollTop;
    }
  }

  onTextareaKeydown(event: KeyboardEvent): void {
    if (this.showMentionDropdown() && event.key === 'Escape') {
      this.closeMentionDropdown();
      event.preventDefault();
    }
  }

  submitJournalPost(): void {
    const content = this.newPostContent().trim();
    if (!content || this.postingJournal()) return;
    this.postingJournal.set(true);
    const request: CreateJournalPostRequest = {
      content,
      category: this.newPostCategory()
    };
    this.journalService.createPost(request).subscribe({
      next: post => {
        this.journalPosts.update(posts => [post, ...posts]);
        this.journalPostCount.update(c => c + 1);
        this.newPostContent.set('');
        this.newPostCategory.set('update');
        this.postingJournal.set(false);
        this.showNewPostForm.set(false);
      },
      error: (err) => {
        this.postingJournal.set(false);
        if (err.status === 402) {
          this.showNewPostForm.set(false);
          this.upgradeLimitMessage.set(err.error?.error || null);
          this.showUpgradeModal.set(true);
        }
      }
    });
  }

  onJournalPostUpdated(post: JournalPost): void {
    this.journalPosts.update(posts => posts.map(p => p.id === post.id ? post : p));
  }

  onJournalPostDeleted(postId: string): void {
    this.journalPosts.update(posts => posts.filter(p => p.id !== postId));
    this.journalPostCount.update(c => Math.max(0, c - 1));
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
