import { Component, signal, computed, effect } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PlayerService } from '../../core/services/player.service';
import { SongService } from '../../core/services/song.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './player.component.html',
  styleUrl: './player.component.scss'
})
export class PlayerComponent {
  liked = signal(false);
  likeAnimating = signal(false);
  isOwnSong = computed(() => {
    const song = this.player.currentSong();
    const user = this.auth.user();
    return !!(song && user && song.artistId === user.id);
  });
  private lastCheckedId = '';

  constructor(
    public player: PlayerService,
    private songService: SongService,
    private auth: AuthService
  ) {
    effect(() => {
      const song = this.player.currentSong();
      if (song && this.auth.isLoggedIn() && song.id !== this.lastCheckedId) {
        this.lastCheckedId = song.id;
        this.songService.isLiked(song.id).subscribe(r => this.liked.set(r.liked));
      }
    });
  }

  toggleLike(): void {
    const song = this.player.currentSong();
    if (!song || !this.auth.isLoggedIn()) return;
    this.songService.toggleLike(song.id).subscribe(r => {
      this.liked.set(r.liked);
      if (r.liked) {
        this.likeAnimating.set(true);
        setTimeout(() => this.likeAnimating.set(false), 600);
      }
    });
  }

  onProgressClick(event: MouseEvent): void {
    const bar = event.currentTarget as HTMLElement;
    const rect = bar.getBoundingClientRect();
    const percent = ((event.clientX - rect.left) / rect.width) * 100;
    this.player.seekTo(percent);
  }

  onVolumeClick(event: MouseEvent): void {
    const bar = event.currentTarget as HTMLElement;
    const rect = bar.getBoundingClientRect();
    const vol = ((event.clientX - rect.left) / rect.width) * 100;
    this.player.setVolume(vol);
  }
}
