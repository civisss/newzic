import { Component, Input, Output, EventEmitter, signal, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { JournalPost, JournalComment, JournalReactionType } from '../../../core/models';
import { JournalService } from '../../../core/services/journal.service';
import { AuthService } from '../../../core/services/auth.service';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { PremiumBadgeComponent } from '../premium-badge/premium-badge.component';
import { VerifiedBadgeComponent } from '../verified-badge/verified-badge.component';

@Component({
  selector: 'app-journal-post-card',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslatePipe, PremiumBadgeComponent, VerifiedBadgeComponent],
  templateUrl: './journal-post-card.component.html',
  styleUrl: './journal-post-card.component.scss'
})
export class JournalPostCardComponent implements OnDestroy {
  @Input({ required: true }) post!: JournalPost;
  @Input() showFullComments = false;
  @Output() deleted = new EventEmitter<string>();
  @Output() updated = new EventEmitter<JournalPost>();

  comments = signal<JournalComment[]>([]);
  showComments = signal(false);
  commentsLoading = signal(false);
  newComment = signal('');
  submittingComment = signal(false);
  showDeleteConfirm = signal(false);

  constructor(
    private journalService: JournalService,
    public auth: AuthService
  ) {}

  ngOnDestroy(): void {
    if (this.showComments()) {
      document.body.style.overflow = '';
    }
  }

  get isOwner(): boolean {
    return this.auth.user()?.id === this.post.authorId;
  }

  get categoryIcon(): string {
    switch (this.post.category) {
      case 'collaboration': return '🤝';
      case 'looking_for_collab': return '🔍';
      case 'announcement': return '📢';
      default: return '✏️';
    }
  }

  get categoryLabel(): string {
    switch (this.post.category) {
      case 'collaboration': return 'journal.category_collaboration';
      case 'looking_for_collab': return 'journal.category_looking';
      case 'announcement': return 'journal.category_announcement';
      default: return 'journal.category_update';
    }
  }

  get formattedContent(): string {
    let html = this.escapeHtml(this.post.content);

    // Highlight @mentions
    html = html.replace(/@(\w+)/g, '<a class="mention" href="/artist/$1">@$1</a>');

    // Highlight #hashtags
    html = html.replace(/#(\w+)/g, '<a class="hashtag">#$1</a>');

    return html;
  }

  hasReacted(type: string): boolean {
    return this.post.reactions.userReactions.includes(type);
  }

  toggleReaction(type: string) {
    if (!this.auth.isLoggedIn()) return;
    this.journalService.toggleReaction(this.post.id, type).subscribe(reactions => {
      this.post = { ...this.post, reactions };
      this.updated.emit(this.post);
    });
  }

  toggleComments() {
    if (!this.showComments()) {
      this.loadComments();
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    this.showComments.update(v => !v);
  }

  loadComments() {
    this.commentsLoading.set(true);
    this.journalService.getComments(this.post.id).subscribe(res => {
      this.comments.set(res.content);
      this.commentsLoading.set(false);
    });
  }

  submitComment() {
    const content = this.newComment().trim();
    if (!content || this.submittingComment()) return;

    this.submittingComment.set(true);
    this.journalService.addComment(this.post.id, content).subscribe(comment => {
      this.comments.update(c => [...c, comment]);
      this.newComment.set('');
      this.submittingComment.set(false);
      this.post = { ...this.post, commentCount: this.post.commentCount + 1 };
      this.updated.emit(this.post);
    });
  }

  deleteComment(commentId: string) {
    this.journalService.deleteComment(commentId).subscribe(() => {
      this.comments.update(c => c.filter(x => x.id !== commentId));
      this.post = { ...this.post, commentCount: Math.max(0, this.post.commentCount - 1) };
      this.updated.emit(this.post);
    });
  }

  confirmDelete() {
    this.showDeleteConfirm.set(true);
  }

  deletePost() {
    this.journalService.deletePost(this.post.id).subscribe(() => {
      this.deleted.emit(this.post.id);
    });
  }

  getTimeAgo(dateStr: string): string {
    const now = new Date();
    const date = new Date(dateStr);
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'now';
    if (minutes < 60) return `${minutes}m`;
    if (hours < 24) return `${hours}h`;
    if (days < 7) return `${days}d`;
    return date.toLocaleDateString();
  }

  private escapeHtml(text: string): string {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
}
