import { Component, OnInit, signal, effect } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { DatePipe } from '@angular/common';
import { LogoComponent } from '../../shared/components/logo/logo.component';
import { TranslatePipe } from '../../shared/pipes/translate.pipe';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { I18nService } from '../../core/services/i18n.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, DatePipe, LogoComponent, TranslatePipe],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  showUserMenu = signal(false);
  showNotifications = signal(false);
  showMobileMenu = signal(false);
  showLangMenu = signal(false);

  constructor(
    public auth: AuthService,
    public notifService: NotificationService,
    public i18n: I18nService
  ) {}

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.notifService.load();
    }
  }

  toggleUserMenu(): void {
    this.showUserMenu.update(v => !v);
    this.showNotifications.set(false);
  }

  toggleNotifications(): void {
    const opening = !this.showNotifications();
    this.showNotifications.set(opening);
    this.showUserMenu.set(false);
    if (opening) {
      this.notifService.load();
    }
  }

  toggleMobileMenu(): void {
    this.showMobileMenu.update(v => !v);
  }

  toggleLangMenu(): void {
    this.showLangMenu.update(v => !v);
    this.showUserMenu.set(false);
    this.showNotifications.set(false);
  }

  selectLang(code: string): void {
    this.i18n.setLanguage(code, this.auth.isLoggedIn());
    this.showLangMenu.set(false);
  }

  closeMenus(): void {
    this.showUserMenu.set(false);
    this.showNotifications.set(false);
    this.showLangMenu.set(false);
  }

  logout(): void {
    this.auth.logout();
    this.closeMenus();
  }
}
