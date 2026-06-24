import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormatNumberPipe } from '../../pipes/format-number.pipe';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { ArtistService } from '../../../core/services/artist.service';
import { Artist } from '../../../core/models';

@Component({
  selector: 'app-followers-modal',
  standalone: true,
  imports: [FormatNumberPipe, TranslatePipe],
  templateUrl: './followers-modal.component.html',
  styleUrl: './followers-modal.component.scss'
})
export class FollowersModalComponent {
  @Input() set userId(id: string) {
    if (id) this._userId = id;
  }
  @Input() set mode(m: 'followers' | 'following') {
    this._mode = m;
    this.load();
  }
  @Output() closed = new EventEmitter<void>();

  _userId = '';
  _mode: 'followers' | 'following' = 'followers';
  users = signal<Artist[]>([]);
  loading = signal(true);

  constructor(
    private artistService: ArtistService,
    private router: Router
  ) {}

  load(): void {
    if (!this._userId) return;
    this.loading.set(true);
    const obs = this._mode === 'followers'
      ? this.artistService.getFollowers(this._userId)
      : this.artistService.getFollowing(this._userId);
    obs.subscribe({
      next: (list) => { this.users.set(list); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  goToArtist(username: string): void {
    this.closed.emit();
    this.router.navigate(['/artist', username]);
  }

  close(): void {
    this.closed.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.close();
    }
  }
}
