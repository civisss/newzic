import { Component, OnInit, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UpperCasePipe, SlicePipe } from '@angular/common';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { DurationPipe } from '../../shared/pipes/duration.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { ArtistService } from '../../core/services/artist.service';
import { JournalService } from '../../core/services/journal.service';
import { SongService } from '../../core/services/song.service';
import { AlbumService } from '../../core/services/album.service';
import { PlayerService } from '../../core/services/player.service';
import { AuthService } from '../../core/services/auth.service';
import { MessageService } from '../../core/services/message.service';
import { Artist, Song, Album, JournalPost } from '../../core/models';
import { FollowersModalComponent } from '../../shared/components/followers-modal/followers-modal.component';
import { PremiumBadgeComponent } from '../../shared/components/premium-badge/premium-badge.component';
import { VerifiedBadgeComponent } from '../../shared/components/verified-badge/verified-badge.component';
import { DonateModalComponent } from '../../shared/components/donate-modal/donate-modal.component';
import { JournalPostCardComponent } from '../../shared/components/journal-post-card/journal-post-card.component';

@Component({
  selector: 'app-artist',
  standalone: true,
  imports: [UpperCasePipe, SlicePipe, FormatNumberPipe, DurationPipe, TranslatePipe, FollowersModalComponent, PremiumBadgeComponent, VerifiedBadgeComponent, DonateModalComponent, JournalPostCardComponent],
  templateUrl: './artist.component.html',
  styleUrl: './artist.component.scss'
})
export class ArtistComponent implements OnInit {
  artist = signal<Artist | null>(null);
  songs = signal<Song[]>([]);
  songsLoading = signal(true);
  albums = signal<Album[]>([]);
  activeTab = signal<'music' | 'journal' | 'about' | 'photos'>('music');
  journalPosts = signal<JournalPost[]>([]);
  journalLoading = signal(false);
  following = signal(false);
  likedSongIds = signal<Set<string>>(new Set());
  showFollowModal = signal(false);
  followModalMode = signal<'followers' | 'following'>('followers');
  showDonateModal = signal(false);

  totalReactions = computed(() => {
    return this.songs().reduce((sum, s) => {
      const r = s.reactions;
      return sum + (r.fire || 0) + (r.gem || 0) + (r.onpoint || 0) + (r.star || 0);
    }, 0);
  });

  constructor(
    private route: ActivatedRoute,
    private artistService: ArtistService,
    private songService: SongService,
    private albumService: AlbumService,
    public playerService: PlayerService,
    public auth: AuthService,
    private messageService: MessageService,
    private journalService: JournalService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const username = params['username'];
      const currentUser = this.auth.user();
      if (currentUser && currentUser.username === username) {
        this.router.navigate(['/profile'], { replaceUrl: true });
        return;
      }
      this.songsLoading.set(true);
      this.artistService.getByUsername(username).subscribe(a => {
        this.artist.set(a ?? null);
        if (!a) return;
        const id = a.id;
        this.songService.getByArtist(id).subscribe(s => {
          this.songs.set(s);
          this.songsLoading.set(false);
          if (this.auth.isLoggedIn()) {
            this.songService.getLikedSongs().subscribe(liked => {
              this.likedSongIds.set(new Set(liked.map(l => l.id)));
            });
          }
        });
        this.albumService.getByArtist(id).subscribe(al => this.albums.set(al));
        this.loadJournal(id);
        if (this.auth.isLoggedIn()) {
          this.artistService.isFollowing(id).subscribe(r => this.following.set(r.following));
        }
      });
    });
  }

  playSong(song: Song): void {
    this.playerService.play(song, this.songs());
  }

  playAll(): void {
    const s = this.songs();
    if (s.length === 0) return;

    // If currently playing this artist's songs, toggle pause
    const current = this.playerService.currentSong();
    if (current && s.some(song => song.id === current.id)) {
      this.playerService.togglePlay();
    } else {
      this.playerService.play(s[0], s);
    }
  }

  isPlayingArtist(): boolean {
    const current = this.playerService.currentSong();
    const s = this.songs();
    return !!current && s.some(song => song.id === current.id) && this.playerService.isPlaying();
  }

  toggleFollow(): void {
    const a = this.artist();
    if (!a || !this.auth.isLoggedIn()) return;
    this.artistService.follow(a.id).subscribe(r => {
      this.following.set(r.following);
      // Update local follower count
      const updated = { ...a, followers: a.followers + (r.following ? 1 : -1) };
      this.artist.set(updated);
    });
  }

  toggleLike(event: Event, song: Song): void {
    event.stopPropagation();
    if (!this.auth.isLoggedIn()) return;
    this.songService.toggleLike(song.id).subscribe(r => {
      const ids = new Set(this.likedSongIds());
      if (r.liked) {
        ids.add(song.id);
      } else {
        ids.delete(song.id);
      }
      this.likedSongIds.set(ids);
      const updated = this.songs().map(s => s.id === song.id ? { ...s, likes: s.likes + (r.liked ? 1 : -1) } : s);
      this.songs.set(updated);
    });
  }

  reactToSong(event: Event, song: Song, type: string): void {
    event.stopPropagation();
    if (!this.auth.isLoggedIn()) return;
    this.songService.react(song.id, type).subscribe(r => {
      const delta = r.added ? 1 : -1;
      const updated = this.songs().map(s => {
        if (s.id !== song.id) return s;
        const reactions = { ...s.reactions };
        const key = type as keyof typeof reactions;
        reactions[key] = Math.max(0, (reactions[key] || 0) + delta);
        return { ...s, reactions };
      });
      this.songs.set(updated);
    });
  }

  loadJournal(artistId: string): void {
    this.journalLoading.set(true);
    this.journalService.getPostsByAuthor(artistId).subscribe(res => {
      this.journalPosts.set(res.content);
      this.journalLoading.set(false);
    });
  }

  onJournalPostUpdated(post: JournalPost): void {
    this.journalPosts.update(posts => posts.map(p => p.id === post.id ? post : p));
  }

  onJournalPostDeleted(postId: string): void {
    this.journalPosts.update(posts => posts.filter(p => p.id !== postId));
  }

  setTab(tab: 'music' | 'journal' | 'about' | 'photos'): void {
    this.activeTab.set(tab);
  }

  getRoleLabel(role: string): string {
    const map: Record<string, string> = {
      singer: 'Singer', producer: 'Producer', band: 'Band',
      musician: 'Musician', beatmaker: 'Beatmaker'
    };
    return map[role] || role;
  }

  getSocialLabel(key: string): string {
    const labels: Record<string, string> = {
      spotify: 'Spotify', youtubeMusic: 'YouTube Music', appleMusic: 'Apple Music',
      soundcloud: 'SoundCloud', tiktok: 'TikTok', instagram: 'Instagram'
    };
    return labels[key] || key;
  }

  openChat(artistId: string): void {
    this.messageService.requestOpenChat(artistId);
  }

  openFollowersModal(mode: 'followers' | 'following'): void {
    this.followModalMode.set(mode);
    this.showFollowModal.set(true);
  }

  closeFollowModal(): void {
    this.showFollowModal.set(false);
  }

  getSocialEntries(): { key: string; url: string }[] {
    const a = this.artist();
    if (!a?.socialLinks) return [];
    return Object.entries(a.socialLinks)
      .filter(([, v]) => !!v)
      .map(([key, url]) => ({ key, url: url as string }));
  }
}
