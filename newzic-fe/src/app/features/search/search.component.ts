import { Component, OnInit, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { DurationPipe } from '../../shared/pipes/duration.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { ArtistService } from '../../core/services/artist.service';
import { SongService } from '../../core/services/song.service';
import { PlayerService } from '../../core/services/player.service';
import { JournalService } from '../../core/services/journal.service';
import { Artist, Song, JournalPost } from '../../core/models';
import { JournalPostCardComponent } from '../../shared/components/journal-post-card/journal-post-card.component';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [RouterLink, FormsModule, FormatNumberPipe, DurationPipe, TranslatePipe, JournalPostCardComponent],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {
  query = '';
  activeFilter = signal<'all' | 'artists' | 'songs' | 'journal'>('all');
  activeGenre = signal<string>('');
  artists = signal<Artist[]>([]);
  songs = signal<Song[]>([]);
  journalPosts = signal<JournalPost[]>([]);

  genres = [
    'Dream Pop', 'Trap', 'Indie Rock', 'K-Pop', 'House', 'Techno',
    'Neo-Soul', 'R&B', 'Lo-Fi', 'Ambient', 'Future Bass', 'Reggaeton',
    'Hip-Hop', 'Pop', 'Post-Rock', 'Latin Pop', 'Electronic', 'Jazz'
  ];
  showAllGenres = signal(false);
  mobileGenreLimit = 8;

  toggleGenres(): void {
    this.showAllGenres.update(v => !v);
  }

  constructor(
    private artistService: ArtistService,
    private songService: SongService,
    private playerService: PlayerService,
    private journalService: JournalService
  ) {}

  ngOnInit(): void {
    this.loadDefaults();
  }

  onSearch(): void {
    this.activeGenre.set('');
    const q = this.query.trim();
    if (!q) {
      this.loadDefaults();
      return;
    }
    this.artistService.search(q).subscribe(a => this.artists.set(a));
    this.songService.search(q).subscribe(s => this.songs.set(s));
    this.journalService.search(q).subscribe(res => this.journalPosts.set(res.content));
  }

  private loadDefaults(): void {
    this.artistService.getTrending().subscribe(a => this.artists.set(a));
    this.songService.getTrending().subscribe(s => this.songs.set(s));
    this.journalService.getFeed(0, 6).subscribe(res => this.journalPosts.set(res.content));
  }

  onJournalPostUpdated(post: JournalPost): void {
    this.journalPosts.update(posts => posts.map(p => p.id === post.id ? post : p));
  }

  setFilter(f: 'all' | 'artists' | 'songs' | 'journal'): void {
    this.activeFilter.set(f);
  }

  selectGenre(genre: string): void {
    this.query = '';
    if (this.activeGenre() === genre) {
      this.activeGenre.set('');
      this.loadDefaults();
      return;
    }
    this.activeGenre.set(genre);
    this.artistService.search(genre).subscribe(a => this.artists.set(a));
    this.songService.search(genre).subscribe(s => this.songs.set(s));
    this.journalService.search(genre).subscribe(res => this.journalPosts.set(res.content));
  }

  playSong(song: Song): void {
    this.playerService.play(song, this.songs());
  }

  getRoleLabel(role: string): string {
    const map: Record<string, string> = {
      singer: 'Singer', producer: 'Producer', band: 'Band',
      musician: 'Musician', beatmaker: 'Beatmaker'
    };
    return map[role] || role;
  }
}
