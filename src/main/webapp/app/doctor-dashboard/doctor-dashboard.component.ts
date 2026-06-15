import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { DoctorDashboardService, DoctorDashboardData, DoctorPatient } from './doctor-dashboard.service';
import { PatientMonitorWebSocketService } from '../core/tracker/patient-monitor-websocket.service';
import { AccountService } from '../core/auth/account.service';

@Component({
  selector: 'jhi-doctor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './doctor-dashboard.component.html',
  styleUrl: './doctor-dashboard.component.scss',
})
export default class DoctorDashboardComponent implements OnInit, OnDestroy {
  private dashboardService = inject(DoctorDashboardService);
  private wsService = inject(PatientMonitorWebSocketService);
  private accountService = inject(AccountService);

  data: DoctorDashboardData | null = null;
  loading = true;
  error = false;

  // Notification modal
  showNotifyModal = false;
  notifyPatient: DoctorPatient | null = null;
  notifyMessage = 'Va recomandam un control medical de rutina.';
  notifySending = false;
  notifySuccess = false;

  private wsSub: Subscription | null = null;
  private reloadTimeout: any = null;

  ngOnInit(): void {
    this.loadData();
    this.setupWebSocket();
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    if (this.reloadTimeout) clearTimeout(this.reloadTimeout);
    this.wsService.disconnect();
  }

  private setupWebSocket(): void {
    this.wsService.connect();

    this.accountService.identity().subscribe(account => {
      if (account) {
        this.wsService.subscribeToDoctorUpdates(account.login);
        this.wsService.subscribeToNotifications(account.login);
        this.wsService.subscribeToDashboardUpdates();
      }
    });

    this.wsSub = this.wsService.medicalData$.subscribe(() => {
      this.debouncedReload();
    });
  }

  private debouncedReload(): void {
    if (this.reloadTimeout) return;
    this.reloadTimeout = setTimeout(() => {
      this.reloadTimeout = null;
      this.loadData();
    }, 5000);
  }

  loadData(): void {
    this.loading = true;
    this.error = false;
    this.dashboardService.getMyPatients().subscribe({
      next: data => {
        this.data = data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'STABLE':
        return 'status-stable';
      case 'ALERT':
        return 'status-alert';
      case 'CRITICAL':
        return 'status-critical';
      case 'NO_DATA':
        return 'status-nodata';
      default:
        return 'status-nodata';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'STABLE':
        return 'Stabil';
      case 'ALERT':
        return 'Alerta';
      case 'CRITICAL':
        return 'Critic';
      case 'NO_DATA':
        return 'Fara date';
      default:
        return 'Necunoscut';
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'CARDIAC':
        return 'Cardiac';
      case 'DIABETES':
        return 'Diabet';
      case 'RESPIRATORY':
        return 'Respirator';
      default:
        return type;
    }
  }

  openNotifyModal(patient: DoctorPatient): void {
    this.notifyPatient = patient;
    this.notifyMessage = 'Va recomandam un control medical de rutina.';
    this.notifySuccess = false;
    this.showNotifyModal = true;
  }

  closeNotifyModal(): void {
    this.showNotifyModal = false;
    this.notifyPatient = null;
  }

  sendNotification(): void {
    if (!this.notifyPatient) return;
    this.notifySending = true;
    this.dashboardService.sendNotification(this.notifyPatient.id, this.notifyMessage).subscribe({
      next: () => {
        this.notifySending = false;
        this.notifySuccess = true;
        setTimeout(() => this.closeNotifyModal(), 1500);
      },
      error: () => {
        this.notifySending = false;
      },
    });
  }

  getStableCount(): number {
    return this.data?.patients.filter(p => p.status === 'STABLE').length ?? 0;
  }

  getAlertCount(): number {
    return this.data?.patients.filter(p => p.status === 'ALERT').length ?? 0;
  }

  getCriticalCount(): number {
    return this.data?.patients.filter(p => p.status === 'CRITICAL').length ?? 0;
  }
}
