import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { ArtistService } from '../../core/services/artist.service';
import { SongService } from '../../core/services/song.service';
import { SpotlightService } from '../../core/services/spotlight.service';
import { FeedService } from '../../core/services/feed.service';
import { PlayerService } from '../../core/services/player.service';
import { AuthService, DEFAULT_AVATAR } from '../../core/services/auth.service';
import { JournalService } from '../../core/services/journal.service';
import { Artist, Song, Spotlight, FeedPost, JournalPost } from '../../core/models';
import { LogoComponent } from '../../shared/components/logo/logo.component';
import { JournalPostCardComponent } from '../../shared/components/journal-post-card/journal-post-card.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, FormatNumberPipe, LogoComponent, TranslatePipe, JournalPostCardComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit, OnDestroy {
  readonly defaultAvatar = DEFAULT_AVATAR;
  trendingArtists = signal<Artist[]>([]);
  trendingSongs = signal<Song[]>([]);
  newReleases = signal<Song[]>([]);
  producers = signal<Artist[]>([]);
  communityPicks = signal<Artist[]>([]);
  recommendedArtists = signal<Artist[]>([]);
  recommendedSongs = signal<Song[]>([]);
  spotlights = signal<Spotlight[]>([]);
  activeSlide = signal(0);
  feed = signal<FeedPost[]>([]);
  likedSongIds = signal<Set<string>>(new Set());
  likeAnimatingId = signal<string | null>(null);
  journalFeed = signal<JournalPost[]>([]);
  private carouselInterval: any;

  constructor(
    public auth: AuthService,
    private artistService: ArtistService,
    private songService: SongService,
    private spotlightService: SpotlightService,
    private feedService: FeedService,
    public playerService: PlayerService,
    private journalService: JournalService
  ) {}

  ngOnInit(): void {
    this.artistService.getTrending().subscribe(a => this.trendingArtists.set(a));
    this.songService.getTrending().subscribe(s => this.trendingSongs.set(s));
    this.songService.getNewReleases().subscribe(s => this.newReleases.set(s));
    this.artistService.getProducers().subscribe(a => this.producers.set(a));
    this.artistService.getCommunityPicks().subscribe(a => this.communityPicks.set(a));
    this.spotlightService.getWeeklyTop().subscribe(list => {
      this.spotlights.set(list);
      if (list.length > 1) this.startCarousel();
    });
    this.feedService.getFeed().subscribe(f => this.feed.set(f.slice(0, 4)));
    this.journalService.getFeed(0, 6).subscribe(res => this.journalFeed.set(res.content));

    if (this.auth.user()) {
      this.artistService.getRecommended().subscribe(a => this.recommendedArtists.set(a));
      this.songService.getRecommended().subscribe(s => this.recommendedSongs.set(s));
      this.songService.getLikedSongs().subscribe(liked => {
        this.likedSongIds.set(new Set(liked.map(l => l.id)));
      });
    }
  }

  playSong(song: Song): void {
    this.playerService.play(song, this.trendingSongs());
  }

  toggleSong(song: Song): void {
    if (this.isPlayingSong(song)) {
      this.playerService.togglePlay();
    } else {
      this.playerService.play(song, this.newReleases());
    }
  }

  isPlayingSong(song: Song): boolean {
    return this.playerService.currentSong()?.id === song.id && this.playerService.isPlaying();
  }

  playRecommendedSong(song: Song): void {
    this.playerService.play(song, this.recommendedSongs());
  }

  toggleLike(event: Event, song: Song): void {
    event.stopPropagation();
    if (!this.auth.isLoggedIn()) return;
    this.songService.toggleLike(song.id).subscribe(r => {
      const ids = new Set(this.likedSongIds());
      if (r.liked) {
        ids.add(song.id);
        this.likeAnimatingId.set(song.id);
        setTimeout(() => this.likeAnimatingId.set(null), 600);
      } else {
        ids.delete(song.id);
      }
      this.likedSongIds.set(ids);
    });
  }

  getRoleLabel(role: string): string {
    const map: Record<string, string> = {
      singer: 'Singer', producer: 'Producer', band: 'Band',
      musician: 'Musician', beatmaker: 'Beatmaker'
    };
    return map[role] || role;
  }

  getPostTypeLabel(type: string): string {
    const map: Record<string, string> = {
      new_release: 'New Release', snippet: 'Snippet', behind_the_scenes: 'Behind the Scenes',
      milestone: 'Milestone', collab_request: 'Collab Request', update: 'Update'
    };
    return map[type] || type;
  }

  onJournalPostUpdated(post: JournalPost): void {
    this.journalFeed.update(posts => posts.map(p => p.id === post.id ? post : p));
  }

  getPostTypeClass(type: string): string {
    return type.replace(/_/g, '-');
  }

  goToSlide(index: number): void {
    this.activeSlide.set(index);
    this.restartCarousel();
  }

  nextSlide(): void {
    const total = this.spotlights().length;
    if (total > 0) {
      this.activeSlide.set((this.activeSlide() + 1) % total);
    }
  }

  prevSlide(): void {
    const total = this.spotlights().length;
    if (total > 0) {
      this.activeSlide.set((this.activeSlide() - 1 + total) % total);
    }
  }

  private startCarousel(): void {
    this.carouselInterval = setInterval(() => this.nextSlide(), 6000);
  }

  private restartCarousel(): void {
    clearInterval(this.carouselInterval);
    this.startCarousel();
  }

  ngOnDestroy(): void {
    clearInterval(this.carouselInterval);
  }
}
