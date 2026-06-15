import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import SharedModule from 'app/shared/shared.module';
import HasAnyAuthorityDirective from 'app/shared/auth/has-any-authority.directive';
import { LANGUAGES } from 'app/config/language.constants';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import { ProfileService } from 'app/layouts/profiles/profile.service';
import { EntityNavbarItems } from 'app/entities/entity-navbar-items';
import { environment } from 'environments/environment';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import ActiveMenuDirective from './active-menu.directive';
import NavbarItem from './navbar-item.model';

interface NotificationItem {
  id: number;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  patientName?: string;
}

@Component({
  selector: 'jhi-navbar',
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  imports: [RouterModule, SharedModule, HasAnyAuthorityDirective, ActiveMenuDirective],
})
export default class NavbarComponent implements OnInit, OnDestroy {
  inProduction?: boolean;
  isNavbarCollapsed = signal(true);
  languages = LANGUAGES;
  openAPIEnabled?: boolean;
  version = '';
  account = inject(AccountService).trackCurrentAccount();
  entitiesNavbarItems: NavbarItem[] = [];

  isDarkMode = true;

  unreadCount = 0;
  recentNotifications: NotificationItem[] = [];
  showNotifDropdown = false;
  private notifInterval: any;

  private readonly loginService = inject(LoginService);
  private readonly translateService = inject(TranslateService);
  private readonly stateStorageService = inject(StateStorageService);
  private readonly profileService = inject(ProfileService);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly appConfig = inject(ApplicationConfigService);

  constructor() {
    const { VERSION } = environment;
    if (VERSION) {
      this.version = VERSION.toLowerCase().startsWith('v') ? VERSION : `v${VERSION}`;
    }
    const savedTheme = localStorage.getItem('pm-theme');
    if (savedTheme === 'light') {
      this.isDarkMode = false;
      document.documentElement.setAttribute('data-theme', 'light');
    }
  }

  ngOnInit(): void {
    this.entitiesNavbarItems = EntityNavbarItems;
    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction = profileInfo.inProduction;
      this.openAPIEnabled = profileInfo.openAPIEnabled;
    });
    this.loadUnreadCount();
    this.notifInterval = setInterval(() => {
      if (this.account()) {
        this.loadUnreadCount();
      }
    }, 30000);
  }

  ngOnDestroy(): void {
    if (this.notifInterval) {
      clearInterval(this.notifInterval);
    }
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    if (this.isDarkMode) {
      document.documentElement.removeAttribute('data-theme');
      localStorage.setItem('pm-theme', 'dark');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
      localStorage.setItem('pm-theme', 'light');
    }
  }

  changeLanguage(languageKey: string): void {
    this.stateStorageService.storeLocale(languageKey);
    this.translateService.use(languageKey);
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed.set(true);
    this.showNotifDropdown = false;
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.collapseNavbar();
    this.loginService.logout();
    this.router.navigate(['']);
    this.unreadCount = 0;
    this.recentNotifications = [];
  }

  toggleNavbar(): void {
    this.isNavbarCollapsed.update(isNavbarCollapsed => !isNavbarCollapsed);
  }

  loadUnreadCount(): void {
    this.http.get<{ count: number }>(this.appConfig.getEndpointFor('api/notifications/unread-count')).subscribe({
      next: res => {
        this.unreadCount = res.count;
      },
      error: () => {},
    });
  }

  toggleNotifDropdown(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.showNotifDropdown = !this.showNotifDropdown;
    if (this.showNotifDropdown) {
      this.loadRecentNotifications();
    }
  }

  closeNotifDropdown(): void {
    this.showNotifDropdown = false;
  }

  loadRecentNotifications(): void {
    this.http.get<NotificationItem[]>(this.appConfig.getEndpointFor('api/notifications/recent')).subscribe({
      next: data => {
        this.recentNotifications = data;
      },
      error: () => {},
    });
  }

  markAsRead(notif: NotificationItem): void {
    if (notif.isRead) return;
    this.http.put(this.appConfig.getEndpointFor(`api/notifications/${notif.id}/mark-read`), {}).subscribe(() => {
      notif.isRead = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    });
  }

  markAllRead(): void {
    this.http.put(this.appConfig.getEndpointFor('api/notifications/mark-all-read'), {}).subscribe(() => {
      this.recentNotifications.forEach(n => (n.isRead = true));
      this.unreadCount = 0;
    });
  }

  getNotifIcon(type: string): string {
    switch (type) {
      case 'THRESHOLD_ALERT':
        return '⚠️';
      case 'ANOMALY_DETECTED':
        return '🔴';
      case 'APPOINTMENT_REMINDER':
        return '📅';
      default:
        return '🔔';
    }
  }

  getTimeAgo(dateStr: string): string {
    const now = new Date();
    const date = new Date(dateStr);
    const diffMs = now.getTime() - date.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return 'acum';
    if (diffMin < 60) return diffMin + ' min';
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return diffH + 'h';
    const diffD = Math.floor(diffH / 24);
    return diffD + 'z';
  }
}
