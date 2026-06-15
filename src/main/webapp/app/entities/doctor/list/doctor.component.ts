import { Component, OnInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import SharedModule from 'app/shared/shared.module';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

interface AdminDoctor {
  id: number;
  userId: number;
  login: string;
  firstName: string;
  lastName: string;
  email: string;
  specialization: string;
  phone: string;
  officeLocation: string;
  activated: boolean;
  patientCount: number;
}

@Component({
  selector: 'jhi-doctor',
  templateUrl: './doctor.component.html',
  styleUrl: './doctor.component.scss',
  imports: [RouterModule, FormsModule, SharedModule],
})
export class DoctorComponent implements OnInit {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  adminDoctors: AdminDoctor[] = [];
  isLoading = false;

  // Reset password modal
  showResetModal = false;
  resetTarget: AdminDoctor | null = null;
  newPassword = '';

  // Delete modal
  showDeleteModal = false;
  deleteTarget: AdminDoctor | null = null;
  transferDoctorId: number | null = null;
  deleteError = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.http.get<AdminDoctor[]>(this.appConfig.getEndpointFor('api/admin/doctors-list')).subscribe({
      next: data => {
        this.adminDoctors = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  getSpecClass(spec: string): string {
    const s = spec.toLowerCase();
    if (s.includes('cardio')) return 'cardiac';
    if (s.includes('diabet') || s.includes('endocrin')) return 'diabetes';
    if (s.includes('pneumo') || s.includes('pulmo') || s.includes('respir')) return 'respiratory';
    return 'general';
  }

  toggleActivation(doctor: AdminDoctor): void {
    this.http.put<any>(this.appConfig.getEndpointFor(`api/admin/toggle-user/${doctor.userId}`), {}).subscribe(() => {
      doctor.activated = !doctor.activated;
    });
  }

  // Reset password
  openResetPassword(doctor: AdminDoctor): void {
    this.resetTarget = doctor;
    this.newPassword = '';
    this.showResetModal = true;
  }

  closeResetModal(): void {
    this.showResetModal = false;
    this.resetTarget = null;
  }

  submitResetPassword(): void {
    if (!this.resetTarget || this.newPassword.length < 4) return;
    this.http
      .put<any>(this.appConfig.getEndpointFor(`api/admin/reset-password/${this.resetTarget.userId}`), {
        newPassword: this.newPassword,
      })
      .subscribe(() => {
        this.closeResetModal();
      });
  }

  // Delete with transfer
  confirmDelete(doctor: AdminDoctor): void {
    this.deleteTarget = doctor;
    this.transferDoctorId = null;
    this.deleteError = '';
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.deleteTarget = null;
    this.transferDoctorId = null;
    this.deleteError = '';
  }

  getOtherDoctors(): AdminDoctor[] {
    if (!this.deleteTarget) return [];
    return this.adminDoctors.filter(d => d.id !== this.deleteTarget!.id && d.activated);
  }

  executeDelete(): void {
    if (!this.deleteTarget) return;

    // If doctor has patients, transfer doctor must be selected
    if (this.deleteTarget.patientCount > 0 && !this.transferDoctorId) {
      this.deleteError = 'Selecteaza un doctor pentru transferul pacientilor!';
      return;
    }

    let url = this.appConfig.getEndpointFor(`api/admin/delete-doctor/${this.deleteTarget.id}`);
    if (this.transferDoctorId) {
      url += `?transferToDoctorId=${this.transferDoctorId}`;
    }

    this.http.delete<any>(url).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.load();
      },
      error: err => {
        this.deleteError = err.error?.error || 'Eroare la stergerea doctorului';
      },
    });
  }
}
