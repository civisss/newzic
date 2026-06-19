import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { CollaborationService } from '../../core/services/collaboration.service';
import { Collaboration, CollabCategory } from '../../core/models';

@Component({
  selector: 'app-collabs',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './collabs.component.html',
  styleUrl: './collabs.component.scss'
})
export class CollabsComponent implements OnInit {
  collabs = signal<Collaboration[]>([]);
  activeCategory = signal<string>('all');
  searchQuery = '';

  categories: { value: string; label: string; icon: string }[] = [
    { value: 'all', label: 'All', icon: '🎵' },
    { value: 'vocalist', label: 'Vocalist', icon: '🎤' },
    { value: 'producer', label: 'Producer', icon: '🎛️' },
    { value: 'beatmaker', label: 'Beatmaker', icon: '🥁' },
    { value: 'guitarist', label: 'Guitarist', icon: '🎸' },
    { value: 'mixing', label: 'Mixing', icon: '🎚️' },
    { value: 'mastering', label: 'Mastering', icon: '💿' },
    { value: 'songwriter', label: 'Songwriter', icon: '✍️' }
  ];

  constructor(private collabService: CollaborationService) {}

  ngOnInit(): void {
    this.collabService.getAll().subscribe(c => this.collabs.set(c));
  }

  filterByCategory(cat: string): void {
    this.activeCategory.set(cat);
    this.searchQuery = '';
    if (cat === 'all') {
      this.collabService.getAll().subscribe(c => this.collabs.set(c));
    } else {
      this.collabService.getByCategory(cat).subscribe(c => this.collabs.set(c));
    }
  }

  onSearch(): void {
    this.activeCategory.set('all');
    if (!this.searchQuery.trim()) {
      this.collabService.getAll().subscribe(c => this.collabs.set(c));
      return;
    }
    this.collabService.search(this.searchQuery).subscribe(c => this.collabs.set(c));
  }

  getStatusClass(status: string): string {
    return `status-${status}`;
  }

  getCategoryLabel(cat: string): string {
    const found = this.categories.find(c => c.value === cat);
    return found ? found.label : cat;
  }
}
