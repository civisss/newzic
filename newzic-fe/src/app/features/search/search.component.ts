import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { DurationPipe } from '../../shared/pipes/duration.pipe';
import { ArtistService } from '../../core/services/artist.service';
import { SongService } from '../../core/services/song.service';
import { PlayerService } from '../../core/services/player.service';
import { Artist, Song } from '../../core/models';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [RouterLink, FormsModule, FormatNumberPipe, DurationPipe],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {
  query = '';
  activeFilter = signal<'all' | 'artists' | 'songs'>('all');
  activeGenre = signal<string>('');
  artists = signal<Artist[]>([]);
  songs = signal<Song[]>([]);

  genres = [
    'Dream Pop', 'Trap', 'Indie Rock', 'K-Pop', 'House', 'Techno',
    'Neo-Soul', 'R&B', 'Lo-Fi', 'Ambient', 'Future Bass', 'Reggaeton',
    'Hip-Hop', 'Pop', 'Post-Rock', 'Latin Pop', 'Electronic', 'Jazz'
  ];

  constructor(
    private artistService: ArtistService,
    private songService: SongService,
    private playerService: PlayerService
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
  }

  private loadDefaults(): void {
    this.artistService.getTrending().subscribe(a => this.artists.set(a));
    this.songService.getTrending().subscribe(s => this.songs.set(s));
  }

  setFilter(f: 'all' | 'artists' | 'songs'): void {
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
