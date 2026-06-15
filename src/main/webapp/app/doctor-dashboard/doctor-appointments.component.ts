import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

interface SlotItem {
  id: number;
  startTime: string;
  endTime: string;
  isAvailable: boolean;
  appointment?: {
    id: number;
    status: string;
    patientName: string;
    patientId: number;
    notes: string;
  };
}

interface DayCell {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  slots: SlotItem[];
}

@Component({
  selector: 'jhi-doctor-appointments',
  templateUrl: './doctor-appointments.component.html',
  styleUrl: './doctor-appointments.component.scss',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
})
export default class DoctorAppointmentsComponent implements OnInit {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  currentDate = new Date();
  calendarDays: DayCell[] = [];
  allSlots: SlotItem[] = [];
  isLoading = false;

  // Selected day
  selectedDay: DayCell | null = null;

  // Create slot modal
  showCreateModal = false;
  newSlotDate = '';
  newSlotStartTime = '09:00';
  newSlotEndTime = '09:30';
  createError = '';
  createSuccess = '';

  // Detail modal
  showDetailModal = false;
  detailSlot: SlotItem | null = null;

  get currentMonthLabel(): string {
    return this.currentDate.toLocaleDateString('ro-RO', { month: 'long', year: 'numeric' });
  }

  get weekDays(): string[] {
    return ['Lun', 'Mar', 'Mie', 'Joi', 'Vin', 'Sam', 'Dum'];
  }

  ngOnInit(): void {
    this.buildCalendar();
    this.loadSlots();
  }

  prevMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() - 1, 1);
    this.buildCalendar();
    this.mapSlotsToCalendar();
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, 1);
    this.buildCalendar();
    this.mapSlotsToCalendar();
  }

  buildCalendar(): void {
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    // Monday = 0, Sunday = 6
    let startDayOfWeek = firstDay.getDay() - 1;
    if (startDayOfWeek < 0) startDayOfWeek = 6;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    this.calendarDays = [];

    // Previous month days
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      const d = new Date(year, month, -i);
      this.calendarDays.push({ date: d, isCurrentMonth: false, isToday: false, slots: [] });
    }

    // Current month days
    for (let d = 1; d <= lastDay.getDate(); d++) {
      const date = new Date(year, month, d);
      date.setHours(0, 0, 0, 0);
      this.calendarDays.push({
        date,
        isCurrentMonth: true,
        isToday: date.getTime() === today.getTime(),
        slots: [],
      });
    }

    // Next month days to fill grid
    const remaining = 42 - this.calendarDays.length;
    for (let i = 1; i <= remaining; i++) {
      const d = new Date(year, month + 1, i);
      this.calendarDays.push({ date: d, isCurrentMonth: false, isToday: false, slots: [] });
    }
  }

  loadSlots(): void {
    this.isLoading = true;
    this.http.get<SlotItem[]>(this.appConfig.getEndpointFor('api/doctor/my-slots')).subscribe({
      next: data => {
        this.allSlots = data;
        this.mapSlotsToCalendar();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  mapSlotsToCalendar(): void {
    // Clear
    this.calendarDays.forEach(day => (day.slots = []));

    for (const slot of this.allSlots) {
      const slotDate = new Date(slot.startTime);
      slotDate.setHours(0, 0, 0, 0);

      const dayCell = this.calendarDays.find(d => d.date.getTime() === slotDate.getTime());
      if (dayCell) {
        dayCell.slots.push(slot);
      }
    }

    // Update selected day if open
    if (this.selectedDay) {
      const updated = this.calendarDays.find(d => d.date.getTime() === this.selectedDay!.date.getTime());
      if (updated) this.selectedDay = updated;
    }
  }

  selectDay(day: DayCell): void {
    if (!day.isCurrentMonth) return;
    this.selectedDay = this.selectedDay?.date.getTime() === day.date.getTime() ? null : day;
  }

  getDaySlotCount(day: DayCell): number {
    return day.slots.length;
  }

  getDayBookedCount(day: DayCell): number {
    return day.slots.filter(s => !s.isAvailable && s.appointment).length;
  }

  // ========== CREATE SLOT ==========

  openCreateModal(day?: DayCell): void {
    this.showCreateModal = true;
    this.createError = '';
    this.createSuccess = '';
    if (day) {
      const d = day.date;
      this.newSlotDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    } else {
      const today = new Date();
      this.newSlotDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    }
    this.newSlotStartTime = '09:00';
    this.newSlotEndTime = '09:30';
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  createSlot(): void {
    if (!this.newSlotDate || !this.newSlotStartTime || !this.newSlotEndTime) {
      this.createError = 'Completeaza toate campurile';
      return;
    }

    const startTime = new Date(`${this.newSlotDate}T${this.newSlotStartTime}:00`).toISOString();
    const endTime = new Date(`${this.newSlotDate}T${this.newSlotEndTime}:00`).toISOString();

    this.http.post<any>(this.appConfig.getEndpointFor('api/doctor/create-slot'), { startTime, endTime }).subscribe({
      next: () => {
        this.createSuccess = 'Slot creat cu succes!';
        this.loadSlots();
        setTimeout(() => this.closeCreateModal(), 1000);
      },
      error: err => {
        this.createError = err.error?.error || 'Eroare la crearea slotului';
      },
    });
  }

  // ========== SLOT ACTIONS ==========

  openSlotDetail(slot: SlotItem): void {
    this.detailSlot = slot;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.detailSlot = null;
  }

  deleteSlot(slotId: number): void {
    if (!confirm('Esti sigur ca vrei sa stergi acest slot? Programarile asociate vor fi anulate.')) return;

    this.http.delete<any>(this.appConfig.getEndpointFor(`api/doctor/delete-slot/${slotId}`)).subscribe({
      next: () => {
        this.loadSlots();
        this.closeDetailModal();
      },
    });
  }

  completeAppointment(appointmentId: number): void {
    this.http.put<any>(this.appConfig.getEndpointFor(`api/doctor/complete-appointment/${appointmentId}`), {}).subscribe({
      next: () => {
        this.loadSlots();
        this.closeDetailModal();
      },
    });
  }

  cancelAppointment(appointmentId: number): void {
    if (!confirm('Esti sigur ca vrei sa anulezi aceasta programare?')) return;

    this.http.put<any>(this.appConfig.getEndpointFor(`api/doctor/cancel-appointment/${appointmentId}`), {}).subscribe({
      next: () => {
        this.loadSlots();
        this.closeDetailModal();
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

  isPast(dateStr: string): boolean {
    return new Date(dateStr) < new Date();
  }
}
