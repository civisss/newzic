import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'home',
    canActivate: [authGuard],
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'search',
    canActivate: [authGuard],
    loadComponent: () => import('./features/search/search.component').then(m => m.SearchComponent)
  },
  {
    path: 'artist/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/artist/artist.component').then(m => m.ArtistComponent)
  },
  {
    path: 'song/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/song/song.component').then(m => m.SongComponent)
  },
  {
    path: 'upload',
    canActivate: [authGuard],
    loadComponent: () => import('./features/upload/upload.component').then(m => m.UploadComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: 'collabs',
    canActivate: [authGuard],
    loadComponent: () => import('./features/collabs/collabs.component').then(m => m.CollabsComponent)
  },
  {
    path: 'feed',
    canActivate: [authGuard],
    loadComponent: () => import('./features/feed/feed.component').then(m => m.FeedComponent)
  },
  {
    path: 'messages',
    canActivate: [authGuard],
    loadComponent: () => import('./features/messages/messages.component').then(m => m.MessagesComponent)
  },
  {
    path: 'messages/:userId',
    canActivate: [authGuard],
    loadComponent: () => import('./features/messages/messages.component').then(m => m.MessagesComponent)
  },
  {
    path: 'analytics',
    canActivate: [authGuard],
    loadComponent: () => import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent)
  },
  {
    path: 'workspaces',
    canActivate: [authGuard],
    loadComponent: () => import('./features/workspace/workspace-list/workspace-list.component').then(m => m.WorkspaceListComponent)
  },
  {
    path: 'workspace/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/workspace/workspace-detail/workspace-detail.component').then(m => m.WorkspaceDetailComponent)
  },
  { path: '**', redirectTo: 'login' }
];
