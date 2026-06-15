import { Injectable, inject, OnDestroy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { AccountService } from 'app/core/auth/account.service';
import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';

@Injectable({ providedIn: 'root' })
export class PatientMonitorWebSocketService implements OnDestroy {
  private accountService = inject(AccountService);

  private stompClient: any = null;
  private connected = false;
  private subscriptions: any[] = [];

  private medicalDataSubject = new Subject<any>();
  private notificationSubject = new Subject<any>();
  private dashboardUpdateSubject = new Subject<any>();

  connect(): void {
    if (this.connected) return;

    const loc = window.location;
    let url = '//' + loc.host + '/websocket/tracker/';
    const authToken = this.getAuthToken();
    if (authToken) {
      url += '?access_token=' + authToken;
    }

    const socket = new SockJS(url);
    this.stompClient = Stomp.over(socket, { debug: false });
    this.stompClient.debug = () => {};

    this.stompClient.connect(
      {},
      () => {
        this.connected = true;
        console.log('WebSocket connected');
      },
      (error: any) => {
        console.log('WebSocket connection error:', error);
        this.connected = false;
        // Retry after 5 seconds
        setTimeout(() => this.connect(), 5000);
      },
    );
  }

  disconnect(): void {
    this.unsubscribeAll();
    if (this.stompClient && this.connected) {
      this.stompClient.disconnect();
      this.connected = false;
    }
  }

  subscribeToMedicalData(patientId: number): void {
    this.waitForConnection(() => {
      const sub = this.stompClient.subscribe('/topic/medical-data/' + patientId, (message: any) => {
        this.medicalDataSubject.next(JSON.parse(message.body));
      });
      this.subscriptions.push(sub);
    });
  }

  subscribeToDoctorUpdates(doctorLogin: string): void {
    this.waitForConnection(() => {
      const sub = this.stompClient.subscribe('/topic/doctor-updates/' + doctorLogin, (message: any) => {
        this.medicalDataSubject.next(JSON.parse(message.body));
      });
      this.subscriptions.push(sub);
    });
  }

  subscribeToNotifications(userLogin: string): void {
    this.waitForConnection(() => {
      const sub = this.stompClient.subscribe('/topic/notifications/' + userLogin, (message: any) => {
        this.notificationSubject.next(JSON.parse(message.body));
      });
      this.subscriptions.push(sub);
    });
  }

  subscribeToDashboardUpdates(): void {
    this.waitForConnection(() => {
      const sub = this.stompClient.subscribe('/topic/dashboard-update', (message: any) => {
        this.dashboardUpdateSubject.next(JSON.parse(message.body));
      });
      this.subscriptions.push(sub);
    });
  }

  get medicalData$(): Observable<any> {
    return this.medicalDataSubject.asObservable();
  }

  get notifications$(): Observable<any> {
    return this.notificationSubject.asObservable();
  }

  get dashboardUpdates$(): Observable<any> {
    return this.dashboardUpdateSubject.asObservable();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  private unsubscribeAll(): void {
    this.subscriptions.forEach(sub => {
      try {
        sub.unsubscribe();
      } catch (e) {}
    });
    this.subscriptions = [];
  }

  private waitForConnection(callback: () => void): void {
    if (this.connected) {
      callback();
    } else {
      setTimeout(() => this.waitForConnection(callback), 500);
    }
  }

  private getAuthToken(): string | null {
    const token = sessionStorage.getItem('jhi-authenticationToken') || localStorage.getItem('jhi-authenticationToken');
    if (token) {
      return token.replace(/^"|"$/g, '');
    }
    return null;
  }
}
