import { Component, OnInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import SharedModule from 'app/shared/shared.module';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

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
  selector: 'jhi-notification',
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.scss',
  imports: [RouterModule, SharedModule],
})
export class NotificationComponent implements OnInit {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  allNotifications: NotificationItem[] = [];
  filteredNotifications: NotificationItem[] = [];
  activeFilter = 'ALL';
  isLoading = false;
  unreadCount = 0;

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading = true;
    // Use the recent endpoint but we want ALL - let's fetch more
    this.http.get<NotificationItem[]>(this.appConfig.getEndpointFor('api/notifications/all')).subscribe({
      next: data => {
        this.allNotifications = data;
        this.unreadCount = data.filter(n => !n.isRead).length;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  setFilter(filter: string): void {
    this.activeFilter = filter;
    this.applyFilter();
  }

  applyFilter(): void {
    if (this.activeFilter === 'ALL') {
      this.filteredNotifications = [...this.allNotifications];
    } else if (this.activeFilter === 'UNREAD') {
      this.filteredNotifications = this.allNotifications.filter(n => !n.isRead);
    } else {
      this.filteredNotifications = this.allNotifications.filter(n => n.type === this.activeFilter);
    }
  }

  markAsRead(notif: NotificationItem): void {
    if (notif.isRead) return;
    this.http.put(this.appConfig.getEndpointFor(`api/notifications/${notif.id}/mark-read`), {}).subscribe(() => {
      notif.isRead = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.applyFilter();
    });
  }

  markAllRead(): void {
    this.http.put(this.appConfig.getEndpointFor('api/notifications/mark-all-read'), {}).subscribe(() => {
      this.allNotifications.forEach(n => (n.isRead = true));
      this.unreadCount = 0;
      this.applyFilter();
    });
  }

  getIcon(type: string): string {
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

  getTypeLabel(type: string): string {
    switch (type) {
      case 'THRESHOLD_ALERT':
        return 'Alerta Prag';
      case 'ANOMALY_DETECTED':
        return 'Anomalie';
      case 'APPOINTMENT_REMINDER':
        return 'Programare';
      default:
        return 'General';
    }
  }

  getTimeAgo(dateStr: string): string {
    const now = new Date();
    const date = new Date(dateStr);
    const diffMs = now.getTime() - date.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return 'acum';
    if (diffMin < 60) return 'acum ' + diffMin + ' min';
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return 'acum ' + diffH + 'h';
    const diffD = Math.floor(diffH / 24);
    if (diffD === 1) return 'ieri';
    if (diffD < 7) return 'acum ' + diffD + ' zile';
    return date.toLocaleDateString('ro-RO');
  }
}
