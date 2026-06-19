import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { ArtistService } from '../../core/services/artist.service';
import { SongService } from '../../core/services/song.service';
import { SpotlightService } from '../../core/services/spotlight.service';
import { FeedService } from '../../core/services/feed.service';
import { PlayerService } from '../../core/services/player.service';
import { AuthService } from '../../core/services/auth.service';
import { Artist, Song, Spotlight, FeedPost } from '../../core/models';
import { DatePipe } from '@angular/common';
import { LogoComponent } from '../../shared/components/logo/logo.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, FormatNumberPipe, DatePipe, LogoComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  trendingArtists = signal<Artist[]>([]);
  trendingSongs = signal<Song[]>([]);
  newReleases = signal<Song[]>([]);
  producers = signal<Artist[]>([]);
  communityPicks = signal<Artist[]>([]);
  recommendedArtists = signal<Artist[]>([]);
  recommendedSongs = signal<Song[]>([]);
  spotlight = signal<Spotlight | null>(null);
  feed = signal<FeedPost[]>([]);

  constructor(
    public auth: AuthService,
    private artistService: ArtistService,
    private songService: SongService,
    private spotlightService: SpotlightService,
    private feedService: FeedService,
    public playerService: PlayerService
  ) {}

  ngOnInit(): void {
    this.artistService.getTrending().subscribe(a => this.trendingArtists.set(a));
    this.songService.getTrending().subscribe(s => this.trendingSongs.set(s));
    this.songService.getNewReleases().subscribe(s => this.newReleases.set(s));
    this.artistService.getProducers().subscribe(a => this.producers.set(a));
    this.artistService.getCommunityPicks().subscribe(a => this.communityPicks.set(a));
    this.spotlightService.getCurrent().subscribe(s => this.spotlight.set(s));
    this.feedService.getFeed().subscribe(f => this.feed.set(f.slice(0, 4)));

    if (this.auth.user()) {
      this.artistService.getRecommended().subscribe(a => this.recommendedArtists.set(a));
      this.songService.getRecommended().subscribe(s => this.recommendedSongs.set(s));
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

  getPostTypeClass(type: string): string {
    return type.replace(/_/g, '-');
  }
}
