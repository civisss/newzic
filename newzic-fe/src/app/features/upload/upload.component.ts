import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SongService } from '../../core/services/song.service';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.scss'
})
export class UploadComponent {
  title = '';
  description = '';
  genre = '';
  tags = '';
  fileName = signal('');
  audioData = signal('');
  coverPreview = signal('');
  uploading = signal(false);
  uploaded = signal(false);
  dragOver = signal(false);
  error = signal('');

  genres = [
    'Dream Pop', 'Trap', 'Indie Rock', 'K-Pop', 'House', 'Techno',
    'Neo-Soul', 'R&B', 'Lo-Fi', 'Ambient', 'Future Bass', 'Reggaeton',
    'Hip-Hop', 'Pop', 'Post-Rock', 'Latin Pop', 'Electronic', 'Jazz', 'Other'
  ];

  constructor(private router: Router, private songService: SongService) {}

  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.readAudioFile(file);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(true);
  }

  onDragLeave(): void {
    this.dragOver.set(false);
  }

  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) this.readAudioFile(input.files[0]);
  }

  private readAudioFile(file: File): void {
    this.fileName.set(file.name);
    const reader = new FileReader();
    reader.onload = (e) => this.audioData.set(e.target?.result as string);
    reader.readAsDataURL(file);
  }

  onCoverSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      const reader = new FileReader();
      reader.onload = (e) => this.coverPreview.set(e.target?.result as string);
      reader.readAsDataURL(input.files[0]);
    }
  }

  submit(): void {
    this.uploading.set(true);
    this.error.set('');

    const tagList = this.tags
      ? this.tags.split(',').map(t => t.trim()).filter(t => t.length > 0)
      : [];

    this.songService.create({
      title: this.title,
      genre: this.genre || undefined,
      tags: tagList,
      cover: this.coverPreview() || undefined,
      description: this.description || undefined,
      audioData: this.audioData() || undefined
    }).subscribe({
      next: () => {
        this.uploading.set(false);
        this.uploaded.set(true);
      },
      error: (err) => {
        this.uploading.set(false);
        this.error.set(err?.error?.details || 'Failed to publish track. Please try again.');
      }
    });
  }

  reset(): void {
    this.title = '';
    this.description = '';
    this.genre = '';
    this.tags = '';
    this.fileName.set('');
    this.audioData.set('');
    this.coverPreview.set('');
    this.uploaded.set(false);
    this.error.set('');
  }
}
