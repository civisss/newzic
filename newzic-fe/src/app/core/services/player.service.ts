import { Injectable, inject, signal, computed } from '@angular/core';
import { Song } from '../models';
import { SongService } from './song.service';
import { Subscription } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PlayerService {
  private _currentSong = signal<Song | null>(null);
  private _isPlaying = signal(false);
  private _progress = signal(0);
  private _duration = signal(0);
  private _volume = signal(80);
  private _shuffle = signal(false);
  private _repeat = signal<'off' | 'all' | 'one'>('off');
  private _queue = signal<Song[]>([]);
  private _currentIndex = signal(0);

  private audio = new Audio();
  private rafId: number | null = null;
  private songService = inject(SongService);
  private loadSub: Subscription | null = null;

  private _loading = signal(false);
  readonly loading = this._loading.asReadonly();

  readonly currentSong = this._currentSong.asReadonly();
  readonly isPlaying = this._isPlaying.asReadonly();
  readonly progress = this._progress.asReadonly();
  readonly volume = this._volume.asReadonly();
  readonly shuffle = this._shuffle.asReadonly();
  readonly repeat = this._repeat.asReadonly();
  readonly queue = this._queue.asReadonly();

  readonly progressPercent = computed(() => {
    const dur = this._duration();
    if (!dur) return 0;
    return (this._progress() / dur) * 100;
  });

  readonly currentTimeFormatted = computed(() => this.formatTime(this._progress()));
  readonly durationFormatted = computed(() => this.formatTime(this._duration()));

  constructor() {
    this.audio.addEventListener('ended', () => this.onTrackEnd());
    this.audio.addEventListener('loadedmetadata', () => {
      this._duration.set(this.audio.duration);
    });
    this.audio.volume = this._volume() / 100;
  }

  stop(): void {
    this.audio.pause();
    this.audio.src = '';
    this.stopProgressLoop();
    this._currentSong.set(null);
    this._isPlaying.set(false);
    this._progress.set(0);
    this._duration.set(0);
    this._queue.set([]);
    this._currentIndex.set(0);
  }

  play(song: Song, queue?: Song[]): void {
    // Cancel any pending audio fetch
    this.loadSub?.unsubscribe();
    this.loadSub = null;

    this._currentSong.set(song);
    this._progress.set(0);

    // Record play on backend
    this.songService.recordPlay(song.id).subscribe();

    if (queue) {
      this._queue.set(queue);
      this._currentIndex.set(queue.findIndex(s => s.id === song.id));
    }

    if (song.audioUrl) {
      // Audio already available (e.g. from getById)
      this._loading.set(false);
      this.startPlayback(song);
    } else {
      // Lazy-load: fetch full song detail with audioUrl
      this._loading.set(true);
      this._isPlaying.set(false);
      this.loadSub = this.songService.getById(song.id).subscribe(full => {
        this._loading.set(false);
        if (full?.audioUrl) {
          const enriched = { ...song, audioUrl: full.audioUrl };
          this._currentSong.set(enriched);
          // Update in queue too
          this._queue.update(q => q.map(s => s.id === enriched.id ? enriched : s));
          this.startPlayback(enriched);
        } else {
          // No audio data at all — use fallback
          this._isPlaying.set(true);
          this._duration.set(song.duration || 210);
          this.startFallbackProgress(song);
        }
      });
    }
  }

  private startPlayback(song: Song): void {
    if (song.audioUrl) {
      this.audio.src = song.audioUrl;
      this.audio.load();
      this.audio.play().then(() => {
        this._isPlaying.set(true);
        this.startProgressLoop();
      }).catch(() => {
        this._isPlaying.set(true);
        this.startFallbackProgress(song);
      });
    } else {
      this._isPlaying.set(true);
      this._duration.set(song.duration || 210);
      this.startFallbackProgress(song);
    }
  }

  togglePlay(): void {
    if (!this._currentSong()) return;
    if (this._isPlaying()) {
      this.audio.pause();
      this.stopProgressLoop();
      this._isPlaying.set(false);
    } else {
      if (this.audio.src) {
        this.audio.play().catch(() => {});
      }
      this._isPlaying.set(true);
      this.startProgressLoop();
    }
  }

  next(): void {
    const q = this._queue();
    if (q.length === 0) return;

    let nextIdx: number;
    if (this._shuffle()) {
      nextIdx = Math.floor(Math.random() * q.length);
    } else {
      nextIdx = (this._currentIndex() + 1) % q.length;
    }

    this._currentIndex.set(nextIdx);
    this.play(q[nextIdx]);
  }

  previous(): void {
    const q = this._queue();
    if (q.length === 0) return;

    if (this._progress() > 3) {
      this.seekTo(0);
      return;
    }

    let prevIdx = this._currentIndex() - 1;
    if (prevIdx < 0) prevIdx = q.length - 1;

    this._currentIndex.set(prevIdx);
    this.play(q[prevIdx]);
  }

  seekTo(percent: number): void {
    const dur = this._duration();
    if (!dur) return;
    const time = (percent / 100) * dur;
    this._progress.set(time);
    if (this.audio.src) {
      this.audio.currentTime = time;
    }
  }

  setVolume(vol: number): void {
    const v = Math.max(0, Math.min(100, vol));
    this._volume.set(v);
    this.audio.volume = v / 100;
  }

  toggleShuffle(): void {
    this._shuffle.update(v => !v);
  }

  toggleRepeat(): void {
    const modes: ('off' | 'all' | 'one')[] = ['off', 'all', 'one'];
    const current = modes.indexOf(this._repeat());
    this._repeat.set(modes[(current + 1) % modes.length]);
  }

  private onTrackEnd(): void {
    if (this._repeat() === 'one') {
      this.audio.currentTime = 0;
      this.audio.play().catch(() => {});
    } else {
      this.next();
    }
  }

  private startProgressLoop(): void {
    this.stopProgressLoop();
    const tick = () => {
      if (this.audio.src && !this.audio.paused) {
        this._progress.set(this.audio.currentTime);
      }
      this.rafId = requestAnimationFrame(tick);
    };
    this.rafId = requestAnimationFrame(tick);
  }

  private stopProgressLoop(): void {
    if (this.rafId !== null) {
      cancelAnimationFrame(this.rafId);
      this.rafId = null;
    }
    if (this.fallbackInterval) {
      clearInterval(this.fallbackInterval);
      this.fallbackInterval = null;
    }
  }

  private fallbackInterval: any = null;

  private startFallbackProgress(song: Song): void {
    this.stopProgressLoop();
    const dur = song.duration || 210;
    this._duration.set(dur);
    this.fallbackInterval = setInterval(() => {
      if (!this._isPlaying()) return;
      const next = this._progress() + 1;
      if (next >= dur) {
        this.onTrackEnd();
      } else {
        this._progress.set(next);
      }
    }, 1000);
  }

  private formatTime(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
  }
}
