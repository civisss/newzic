import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PlayerService } from '../../core/services/player.service';

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './player.component.html',
  styleUrl: './player.component.scss'
})
export class PlayerComponent {
  constructor(public player: PlayerService) {}

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
