import { Component, OnInit, signal, computed, ViewChild, ElementRef, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { WorkspaceService } from '../../../core/services/workspace.service';
import { AuthService, DEFAULT_AVATAR } from '../../../core/services/auth.service';
import { PlayerService } from '../../../core/services/player.service';
import { ArtistService } from '../../../core/services/artist.service';
import { I18nService } from '../../../core/services/i18n.service';
import { Artist } from '../../../core/models';
import {
  Workspace,
  WorkspaceVersion,
  WorkspaceComment,
  WorkspaceFile,
  WorkspaceChatMessage
} from '../../../core/models';

@Component({
  selector: 'app-workspace-detail',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './workspace-detail.component.html',
  styleUrl: './workspace-detail.component.scss'
})
export class WorkspaceDetailComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('waveformCanvas') waveformCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('audioElement') audioElement!: ElementRef<HTMLAudioElement>;

  workspace = signal<Workspace | null>(null);
  versions = signal<WorkspaceVersion[]>([]);
  activeVersion = signal<WorkspaceVersion | null>(null);
  comments = signal<WorkspaceComment[]>([]);
  files = signal<WorkspaceFile[]>([]);
  chatMessages = signal<WorkspaceChatMessage[]>([]);

  // Audio state
  isPlaying = signal(false);
  currentTime = signal(0);
  duration = signal(0);
  waveformData = signal<number[]>([]);

  // UI state
  activeTab = signal<'comments' | 'files' | 'chat' | 'versions'>('comments');
  showCommentInput = signal(false);
  commentTimestamp = signal(0);
  newComment = '';
  replyToId: string | null = null;
  newChatMessage = '';
  showStatusDropdown = signal(false);

  // Upload modals
  showVersionModal = signal(false);
  newVersionAudioData = '';
  newVersionFileName = '';
  newVersionNotes = '';
  versionDragOver = signal(false);
  versionUploading = signal(false);

  showFileModal = signal(false);
  newFileName = '';
  newFileUrl = '';
  newFileType = 'OTHER';

  showInviteModal = signal(false);
  inviteSearch = '';
  inviteResults = signal<Artist[]>([]);
  selectedInvitee = signal<Artist | null>(null);
  private inviteSearchTimeout: any = null;

  defaultAvatar = DEFAULT_AVATAR;

  private animationFrameId: number | null = null;
  private audio: HTMLAudioElement | null = null;

  readonly currentUser = computed(() => this.authService.user());

  readonly progressPercent = computed(() => {
    const d = this.duration();
    return d > 0 ? (this.currentTime() / d) * 100 : 0;
  });

  readonly formattedCurrentTime = computed(() => this.formatTime(this.currentTime()));
  readonly formattedDuration = computed(() => this.formatTime(this.duration()));

  // Comment pins positioned on waveform
  readonly commentPins = computed(() => {
    const d = this.duration();
    if (d <= 0) return [];
    return this.comments().map(c => ({
      ...c,
      leftPercent: (c.timestampSeconds / d) * 100
    }));
  });

  fileTypes = [
    { value: 'STEM', label: 'Stem' },
    { value: 'SAMPLE', label: 'Sample' },
    { value: 'REFERENCE', label: 'Reference' },
    { value: 'LYRICS', label: 'Lyrics' },
    { value: 'OTHER', label: 'Other' }
  ];

  statuses = ['draft', 'in_progress', 'review', 'done'];

  private onMetadataLoaded = () => {};
  private onAudioEnded = () => {};

  constructor(
    private route: ActivatedRoute,
    private workspaceService: WorkspaceService,
    private authService: AuthService,
    private artistService: ArtistService,
    private playerService: PlayerService,
    private i18n: I18nService,
    private cdr: ChangeDetectorRef
  ) {}

  getStatusLabel(status: string): string {
    return this.i18n.t('workspace.status.' + status);
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.loadWorkspace(id);
  }

  ngAfterViewInit(): void {
    // Canvas setup happens after first version load
  }

  ngOnDestroy(): void {
    this.stopAudio();
    if (this.animationFrameId) cancelAnimationFrame(this.animationFrameId);
  }

  // ── Data Loading ──

  loadWorkspace(id: string): void {
    this.workspaceService.getById(id).subscribe(ws => {
      this.workspace.set(ws);
      this.loadVersions(id);
      this.loadFiles(id);
      this.loadChat(id);
    });
  }

  loadVersions(wsId: string): void {
    this.workspaceService.getVersions(wsId).subscribe(versions => {
      this.versions.set(versions);
      if (versions.length > 0 && !this.activeVersion()) {
        this.selectVersion(versions[0]); // Latest version
      }
    });
  }

  selectVersion(version: WorkspaceVersion): void {
    this.stopAudio();
    this.activeVersion.set(version);
    this.currentTime.set(0);
    this.duration.set(version.duration || 0);
    this.loadComments();
    this.generateWaveform();

    // Force change detection so the template renders the player section
    this.cdr.detectChanges();

    // Setup audio after DOM is ready
    this.setupAudio(version);
  }

  private setupAudio(version: WorkspaceVersion): void {
    // Clean up previous listeners
    if (this.audio) {
      this.audio.removeEventListener('loadedmetadata', this.onMetadataLoaded);
      this.audio.removeEventListener('ended', this.onAudioEnded);
    }

    setTimeout(() => {
      if (this.audioElement?.nativeElement) {
        this.audio = this.audioElement.nativeElement;

        this.onMetadataLoaded = () => {
          this.duration.set(this.audio!.duration);
          this.drawWaveform();
        };
        this.onAudioEnded = () => {
          this.isPlaying.set(false);
        };

        this.audio.addEventListener('loadedmetadata', this.onMetadataLoaded);
        this.audio.addEventListener('ended', this.onAudioEnded);
        this.audio.src = version.audioUrl;
        this.audio.load();
      }
    });
  }

  loadComments(): void {
    const ws = this.workspace();
    const v = this.activeVersion();
    if (!ws || !v) return;
    this.workspaceService.getComments(ws.id, v.id).subscribe(c => this.comments.set(c));
  }

  loadFiles(wsId: string): void {
    this.workspaceService.getFiles(wsId).subscribe(f => this.files.set(f));
  }

  loadChat(wsId: string): void {
    this.workspaceService.getChatMessages(wsId).subscribe(m => this.chatMessages.set(m));
  }

  // ── Audio Controls ──

  togglePlay(): void {
    if (!this.audio) return;
    if (this.isPlaying()) {
      this.audio.pause();
      this.isPlaying.set(false);
      if (this.animationFrameId) cancelAnimationFrame(this.animationFrameId);
    } else {
      // Stop the global player if it's playing
      if (this.playerService.isPlaying()) {
        this.playerService.togglePlay();
      }
      this.audio.play().then(() => {
        this.isPlaying.set(true);
        this.startProgressLoop();
      }).catch(() => {});
    }
  }

  seekTo(event: MouseEvent): void {
    const canvas = this.waveformCanvas?.nativeElement;
    if (!canvas || !this.audio) return;
    const rect = canvas.getBoundingClientRect();
    const percent = (event.clientX - rect.left) / rect.width;
    const time = percent * this.duration();
    this.audio.currentTime = time;
    this.currentTime.set(time);
    this.drawWaveform();
  }

  // ── Waveform click → Add comment ──

  onWaveformClick(event: MouseEvent): void {
    const canvas = this.waveformCanvas?.nativeElement;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const percent = (event.clientX - rect.left) / rect.width;
    const time = percent * this.duration();

    // Seek audio to that point
    if (this.audio) {
      this.audio.currentTime = time;
      this.currentTime.set(time);
    }

    // Show comment input at that timestamp
    this.commentTimestamp.set(time);
    this.showCommentInput.set(true);
    this.replyToId = null;
    this.newComment = '';
  }

  // ── Timestamped Comments ──

  submitComment(): void {
    const ws = this.workspace();
    const v = this.activeVersion();
    if (!ws || !v || !this.newComment.trim()) return;

    this.workspaceService.addComment(ws.id, v.id, {
      content: this.newComment,
      timestampSeconds: this.commentTimestamp(),
      parentId: this.replyToId || undefined
    }).subscribe(() => {
      this.newComment = '';
      this.showCommentInput.set(false);
      this.replyToId = null;
      this.loadComments();
    });
  }

  jumpToTimestamp(seconds: number): void {
    if (this.audio) {
      this.audio.currentTime = seconds;
      this.currentTime.set(seconds);
      if (!this.isPlaying()) {
        this.togglePlay();
      }
      this.drawWaveform();
    }
  }

  replyTo(comment: WorkspaceComment): void {
    this.replyToId = comment.id;
    this.commentTimestamp.set(comment.timestampSeconds);
    this.showCommentInput.set(true);
    this.newComment = `@${comment.authorName} `;
  }

  cancelComment(): void {
    this.showCommentInput.set(false);
    this.newComment = '';
    this.replyToId = null;
  }

  // ── Versions ──

  onVersionFileDrop(event: DragEvent): void {
    event.preventDefault();
    this.versionDragOver.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.readVersionFile(file);
  }

  onVersionDragOver(event: DragEvent): void {
    event.preventDefault();
    this.versionDragOver.set(true);
  }

  onVersionDragLeave(): void {
    this.versionDragOver.set(false);
  }

  onVersionFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.readVersionFile(file);
  }

  private readVersionFile(file: File): void {
    this.newVersionFileName = file.name;
    const reader = new FileReader();
    reader.onload = (e) => {
      this.newVersionAudioData = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }

  uploadVersion(): void {
    const ws = this.workspace();
    if (!ws || !this.newVersionAudioData || this.versionUploading()) return;

    this.versionUploading.set(true);
    this.workspaceService.uploadVersion(ws.id, {
      audioUrl: this.newVersionAudioData,
      notes: this.newVersionNotes || undefined
    }).subscribe({
      next: () => {
        this.showVersionModal.set(false);
        this.newVersionAudioData = '';
        this.newVersionFileName = '';
        this.newVersionNotes = '';
        this.versionUploading.set(false);
        this.loadVersions(ws.id);
      },
      error: () => {
        this.versionUploading.set(false);
      }
    });
  }

  // ── Files ──

  uploadFile(): void {
    const ws = this.workspace();
    if (!ws || !this.newFileName.trim() || !this.newFileUrl.trim()) return;

    this.workspaceService.uploadFile(ws.id, {
      name: this.newFileName,
      url: this.newFileUrl,
      fileType: this.newFileType
    }).subscribe(() => {
      this.showFileModal.set(false);
      this.newFileName = '';
      this.newFileUrl = '';
      this.newFileType = 'OTHER';
      this.loadFiles(ws.id);
    });
  }

  deleteFile(fileId: string): void {
    const ws = this.workspace();
    if (!ws) return;
    this.workspaceService.deleteFile(ws.id, fileId).subscribe(() => {
      this.loadFiles(ws.id);
    });
  }

  // ── Chat ──

  sendChat(): void {
    const ws = this.workspace();
    if (!ws || !this.newChatMessage.trim()) return;

    this.workspaceService.sendChatMessage(ws.id, this.newChatMessage).subscribe(msg => {
      this.chatMessages.update(msgs => [...msgs, msg]);
      this.newChatMessage = '';
    });
  }

  // ── Members ──

  onInviteSearchInput(): void {
    clearTimeout(this.inviteSearchTimeout);
    this.selectedInvitee.set(null);
    const q = this.inviteSearch.trim();
    if (q.length < 2) {
      this.inviteResults.set([]);
      return;
    }
    this.inviteSearchTimeout = setTimeout(() => {
      this.artistService.search(q).subscribe(results => {
        // Exclude current members
        const memberIds = new Set(this.workspace()?.members.map(m => m.id) || []);
        this.inviteResults.set(results.filter(a => !memberIds.has(a.id)));
      });
    }, 300);
  }

  selectInvitee(artist: Artist): void {
    this.selectedInvitee.set(artist);
    this.inviteSearch = artist.name;
    this.inviteResults.set([]);
  }

  clearInvitee(): void {
    this.selectedInvitee.set(null);
    this.inviteSearch = '';
    this.inviteResults.set([]);
  }

  inviteMember(): void {
    const ws = this.workspace();
    const invitee = this.selectedInvitee();
    if (!ws || !invitee) return;

    this.workspaceService.inviteMember(ws.id, invitee.id).subscribe(() => {
      this.showInviteModal.set(false);
      this.clearInvitee();
      this.loadWorkspace(ws.id);
    });
  }

  // ── Status ──

  updateStatus(status: string): void {
    const ws = this.workspace();
    if (!ws) return;
    this.workspaceService.update(ws.id, { status }).subscribe(updated => {
      this.workspace.set(updated);
      this.showStatusDropdown.set(false);
    });
  }

  // ── Waveform Rendering ──

  private generateWaveform(): void {
    // Generate pseudo-random waveform data
    const bars = 120;
    const data: number[] = [];
    for (let i = 0; i < bars; i++) {
      data.push(0.2 + Math.random() * 0.8);
    }
    this.waveformData.set(data);
    // Retry drawing until canvas is available and has dimensions
    this.drawWaveformWithRetry(5);
  }

  private drawWaveformWithRetry(retries: number): void {
    setTimeout(() => {
      const canvas = this.waveformCanvas?.nativeElement;
      if (canvas && canvas.getBoundingClientRect().width > 0) {
        this.drawWaveform();
      } else if (retries > 0) {
        this.drawWaveformWithRetry(retries - 1);
      }
    }, 50);
  }

  private drawWaveform(): void {
    const canvas = this.waveformCanvas?.nativeElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    const data = this.waveformData();
    const barCount = data.length;
    const barWidth = rect.width / barCount;
    const barGap = 1;
    const maxHeight = rect.height;
    const progress = this.progressPercent() / 100;

    ctx.clearRect(0, 0, rect.width, rect.height);

    for (let i = 0; i < barCount; i++) {
      const x = i * barWidth;
      const barH = data[i] * maxHeight * 0.85;
      const y = (maxHeight - barH) / 2;
      const barPercent = (i + 0.5) / barCount;

      if (barPercent <= progress) {
        // Played portion — violet gradient
        const gradient = ctx.createLinearGradient(x, y, x, y + barH);
        gradient.addColorStop(0, '#A855F7');
        gradient.addColorStop(1, '#6366F1');
        ctx.fillStyle = gradient;
      } else {
        // Unplayed — dim
        ctx.fillStyle = 'rgba(255, 255, 255, 0.15)';
      }

      ctx.beginPath();
      ctx.roundRect(x + barGap / 2, y, barWidth - barGap, barH, 2);
      ctx.fill();
    }

    // Draw comment pin markers
    const comments = this.comments();
    const dur = this.duration();
    if (dur > 0) {
      comments.forEach(c => {
        const pinX = (c.timestampSeconds / dur) * rect.width;
        // Pin line
        ctx.strokeStyle = '#F472B6';
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(pinX, 0);
        ctx.lineTo(pinX, maxHeight);
        ctx.stroke();

        // Pin dot
        ctx.fillStyle = '#F472B6';
        ctx.beginPath();
        ctx.arc(pinX, 6, 4, 0, Math.PI * 2);
        ctx.fill();
      });
    }
  }

  private startProgressLoop(): void {
    const tick = () => {
      if (this.audio && !this.audio.paused) {
        this.currentTime.set(this.audio.currentTime);
        this.drawWaveform();
        this.animationFrameId = requestAnimationFrame(tick);
      }
    };
    this.animationFrameId = requestAnimationFrame(tick);
  }

  private stopAudio(): void {
    if (this.audio) {
      this.audio.pause();
      this.audio.currentTime = 0;
    }
    this.isPlaying.set(false);
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
  }

  // ── Utilities ──

  formatTime(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  formatTimestamp(seconds: number): string {
    return this.formatTime(seconds);
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  getFileIcon(type: string): string {
    const map: Record<string, string> = {
      stem: '🎚️', sample: '🎹', reference: '🎵', lyrics: '📝', other: '📄'
    };
    return map[type] || '📄';
  }

  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      draft: '📝', in_progress: '🎧', review: '👀', done: '✅'
    };
    return map[status] || '🎵';
  }

  getAvatar(url: string | null): string {
    return url || this.defaultAvatar;
  }

  trackById(_: number, item: { id: string }): string {
    return item.id;
  }
}
