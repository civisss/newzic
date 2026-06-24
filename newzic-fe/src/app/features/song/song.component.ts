import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SlicePipe } from '@angular/common';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { DurationPipe } from '../../shared/pipes/duration.pipe';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { BackButtonComponent } from '../../shared/components/back-button/back-button.component';
import { SongService } from '../../core/services/song.service';
import { PlayerService } from '../../core/services/player.service';
import { Song } from '../../core/models';

@Component({
  selector: 'app-song',
  standalone: true,
  imports: [RouterLink, SlicePipe, FormatNumberPipe, DurationPipe, TranslatePipe, BackButtonComponent],
  templateUrl: './song.component.html',
  styleUrl: './song.component.scss'
})
export class SongComponent implements OnInit {
  song = signal<Song | null>(null);
  relatedSongs = signal<Song[]>([]);

  constructor(
    private route: ActivatedRoute,
    private songService: SongService,
    private playerService: PlayerService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.songService.getById(params['id']).subscribe(s => {
        this.song.set(s ?? null);
        if (s) {
          this.songService.getByArtist(s.artistId).subscribe(songs =>
            this.relatedSongs.set(songs.filter(x => x.id !== s.id))
          );
        }
      });
    });
  }

  play(): void {
    const s = this.song();
    if (s) this.playerService.play(s, [s, ...this.relatedSongs()]);
  }

  playRelated(song: Song): void {
    this.playerService.play(song, this.relatedSongs());
  }
}
