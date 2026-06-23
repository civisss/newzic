import { Component, computed } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from './layout/navbar/navbar.component';
import { PlayerComponent } from './layout/player/player.component';
import { ChatWidgetComponent } from './layout/chat-widget/chat-widget.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, PlayerComponent, ChatWidgetComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(public auth: AuthService, private router: Router) {}

  showChatWidget(): boolean {
    return !this.router.url.startsWith('/messages');
  }
}
