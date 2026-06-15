import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

interface AvailableSlot {
  id: number;
  startTime: string;
  endTime: string;
  doctorName: string;
}

interface MyAppointment {
  id: number;
  status: string;
  notes: string;
  createdAt: string;
  startTime: string;
  endTime: string;
  doctorName: string;
}

@Component({
  selector: 'jhi-patient-appointments',
  templateUrl: './patient-appointments.component.html',
  styleUrl: './patient-appointments.component.scss',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
})
export default class PatientAppointmentsComponent implements OnInit {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  availableSlots: AvailableSlot[] = [];
  myAppointments: MyAppointment[] = [];
  isLoading = false;
  activeTab = 'available';

  // Book modal
  showBookModal = false;
  bookingSlot: AvailableSlot | null = null;
  bookingNotes = '';
  bookSuccess = '';
  bookError = '';

  ngOnInit(): void {
    this.loadAvailableSlots();
    this.loadMyAppointments();
  }

  loadAvailableSlots(): void {
    this.isLoading = true;
    this.http.get<AvailableSlot[]>(this.appConfig.getEndpointFor('api/patient/available-slots')).subscribe({
      next: data => {
        this.availableSlots = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  loadMyAppointments(): void {
    this.http.get<MyAppointment[]>(this.appConfig.getEndpointFor('api/patient/my-appointments')).subscribe({
      next: data => {
        this.myAppointments = data;
      },
    });
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

  get scheduledAppointments(): MyAppointment[] {
    return this.myAppointments.filter(a => a.status === 'SCHEDULED');
  }

  get pastAppointments(): MyAppointment[] {
    return this.myAppointments.filter(a => a.status !== 'SCHEDULED');
  }

  // Group slots by date
  get slotsByDate(): Map<string, AvailableSlot[]> {
    const map = new Map<string, AvailableSlot[]>();
    for (const slot of this.availableSlots) {
      const dateKey = new Date(slot.startTime).toLocaleDateString('ro-RO', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric',
      });
      if (!map.has(dateKey)) {
        map.set(dateKey, []);
      }
      map.get(dateKey)!.push(slot);
    }
    return map;
  }

  // Book
  openBookModal(slot: AvailableSlot): void {
    this.bookingSlot = slot;
    this.bookingNotes = '';
    this.bookSuccess = '';
    this.bookError = '';
    this.showBookModal = true;
  }

  closeBookModal(): void {
    this.showBookModal = false;
    this.bookingSlot = null;
  }

  confirmBooking(): void {
    if (!this.bookingSlot) return;

    this.http
      .post<any>(this.appConfig.getEndpointFor('api/patient/book-appointment'), {
        slotId: this.bookingSlot.id,
        notes: this.bookingNotes,
      })
      .subscribe({
        next: () => {
          this.bookSuccess = 'Programare realizata cu succes!';
          this.loadAvailableSlots();
          this.loadMyAppointments();
          setTimeout(() => this.closeBookModal(), 1500);
        },
        error: err => {
          this.bookError = err.error?.error || 'Eroare la rezervare';
        },
      });
  }

  cancelAppointment(appointmentId: number): void {
    if (!confirm('Esti sigur ca vrei sa anulezi aceasta programare?')) return;

    this.http.put<any>(this.appConfig.getEndpointFor(`api/patient/cancel-appointment/${appointmentId}`), {}).subscribe({
      next: () => {
        this.loadAvailableSlots();
        this.loadMyAppointments();
      },
    });
  }

  formatTime(dateStr: string): string {
    const d = new Date(dateStr);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ro-RO', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  formatFullDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ro-RO', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'SCHEDULED':
        return 'Programat';
      case 'COMPLETED':
        return 'Finalizat';
      case 'CANCELLED':
        return 'Anulat';
      default:
        return status;
    }
  }
}
