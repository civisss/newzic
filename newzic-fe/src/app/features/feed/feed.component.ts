import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormatNumberPipe } from '../../shared/pipes/format-number.pipe';
import { FeedService } from '../../core/services/feed.service';
import { FeedPost } from '../../core/models';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [RouterLink, DatePipe, FormatNumberPipe],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss'
})
export class FeedComponent implements OnInit {
  posts = signal<FeedPost[]>([]);

  constructor(private feedService: FeedService) {}

  ngOnInit(): void {
    this.feedService.getFeed().subscribe(p => this.posts.set(p));
  }

  getPostTypeLabel(type: string): string {
    const map: Record<string, string> = {
      new_release: 'New Release', snippet: 'Snippet', behind_the_scenes: 'Behind the Scenes',
      milestone: 'Milestone', collab_request: 'Collab Request', update: 'Update'
    };
    return map[type] || type;
  }

  getPostTypeClass(type: string): string {
    return type.replace(/_/g, '-');
  }
}
