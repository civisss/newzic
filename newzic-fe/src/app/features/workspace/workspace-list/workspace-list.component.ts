import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { WorkspaceService } from '../../../core/services/workspace.service';
import { Workspace, WorkspaceStatus } from '../../../core/models';
import { AuthService, DEFAULT_AVATAR } from '../../../core/services/auth.service';
import { TranslatePipe } from '../../../shared/pipes/translate.pipe';
import { I18nService } from '../../../core/services/i18n.service';
import { UpgradeModalComponent } from '../../../shared/components/upgrade-modal/upgrade-modal.component';

@Component({
  selector: 'app-workspace-list',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe, TranslatePipe, UpgradeModalComponent],
  templateUrl: './workspace-list.component.html',
  styleUrl: './workspace-list.component.scss'
})
export class WorkspaceListComponent implements OnInit {
  workspaces = signal<Workspace[]>([]);
  activeFilter = signal<string>('all');
  showCreateModal = signal(false);
  showUpgradeModal = signal(false);

  newTitle = '';
  newDescription = '';
  defaultAvatar = DEFAULT_AVATAR;

  filters = [
    { value: 'all', labelKey: 'workspace.filter_all', icon: '🎵' },
    { value: 'draft', labelKey: 'workspace.filter_draft', icon: '📝' },
    { value: 'in_progress', labelKey: 'workspace.filter_in_progress', icon: '🎧' },
    { value: 'review', labelKey: 'workspace.filter_review', icon: '👀' },
    { value: 'done', labelKey: 'workspace.filter_done', icon: '✅' }
  ];

  constructor(
    private workspaceService: WorkspaceService,
    private authService: AuthService,
    private router: Router,
    public i18n: I18nService
  ) {}

  ngOnInit(): void {
    this.loadWorkspaces();
  }

  loadWorkspaces(): void {
    const filter = this.activeFilter();
    if (filter === 'all') {
      this.workspaceService.getMyWorkspaces().subscribe(ws => this.workspaces.set(ws));
    } else {
      this.workspaceService.getMyWorkspaces(filter).subscribe(ws => this.workspaces.set(ws));
    }
  }

  filterBy(value: string): void {
    this.activeFilter.set(value);
    this.loadWorkspaces();
  }

  createWorkspace(): void {
    if (!this.newTitle.trim()) return;

    this.workspaceService.create({
      title: this.newTitle,
      description: this.newDescription || undefined
    }).subscribe({
      next: ws => {
        this.showCreateModal.set(false);
        this.newTitle = '';
        this.newDescription = '';
        this.router.navigate(['/workspace', ws.id]);
      },
      error: err => {
        if (err.status === 402) {
          this.showCreateModal.set(false);
          this.showUpgradeModal.set(true);
        }
      }
    });
  }

  getStatusClass(status: string): string {
    return `status-${status}`;
  }

  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      draft: '📝', in_progress: '🎧', review: '👀', done: '✅'
    };
    return map[status] || '🎵';
  }

  getMemberAvatars(ws: Workspace): { avatar: string; name: string }[] {
    return ws.members.slice(0, 4).map(m => ({
      avatar: m.avatar || this.defaultAvatar,
      name: m.displayName
    }));
  }
}
