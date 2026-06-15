import { Component, OnInit, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import SharedModule from 'app/shared/shared.module';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

interface AdminPatient {
  id: number;
  userId: number;
  login: string;
  firstName: string;
  lastName: string;
  email: string;
  cnp: string;
  phoneNumber: string;
  patientType: string;
  patientSubtype: string;
  activated: boolean;
  doctorId: number;
  doctorName: string;
  anomalies24h: number;
}

interface AdminDoctor {
  id: number;
  firstName: string;
  lastName: string;
  specialization: string;
}

@Component({
  selector: 'jhi-patient',
  templateUrl: './patient.component.html',
  styleUrl: './patient.component.scss',
  imports: [RouterModule, FormsModule, SharedModule],
})
export class PatientComponent implements OnInit {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  adminPatients: AdminPatient[] = [];
  doctorsList: AdminDoctor[] = [];
  isLoading = false;
  selectedDoctorFilter: number | null = null;

  // Reassign modal
  showReassignModal = false;
  reassignTarget: AdminPatient | null = null;
  reassignDoctorId: number | null = null;

  // Reset password modal
  showResetModal = false;
  resetTarget: AdminPatient | null = null;
  newPassword = '';

  // Delete modal
  showDeleteModal = false;
  deleteTarget: AdminPatient | null = null;

  ngOnInit(): void {
    this.loadDoctors();
    this.load();
  }

  load(): void {
    this.isLoading = true;
    const url = this.selectedDoctorFilter ? `api/admin/patients-list?doctorId=${this.selectedDoctorFilter}` : 'api/admin/patients-list';
    this.http.get<AdminPatient[]>(this.appConfig.getEndpointFor(url)).subscribe({
      next: data => {
        this.adminPatients = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  loadDoctors(): void {
    this.http.get<AdminDoctor[]>(this.appConfig.getEndpointFor('api/admin/doctors-list')).subscribe({
      next: data => (this.doctorsList = data),
    });
  }

  onDoctorFilterChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedDoctorFilter = value === 'all' ? null : +value;
    this.load();
  }

  toggleActivation(patient: AdminPatient): void {
    this.http.put<any>(this.appConfig.getEndpointFor(`api/admin/toggle-user/${patient.userId}`), {}).subscribe(() => {
      patient.activated = !patient.activated;
    });
  }

  // Reassign
  openReassign(patient: AdminPatient): void {
    this.reassignTarget = patient;
    this.reassignDoctorId = null;
    this.showReassignModal = true;
  }

  closeReassignModal(): void {
    this.showReassignModal = false;
    this.reassignTarget = null;
  }

  submitReassign(): void {
    if (!this.reassignTarget || !this.reassignDoctorId) return;
    this.http
      .put<any>(this.appConfig.getEndpointFor('api/admin/reassign-patient'), {
        patientId: this.reassignTarget.id,
        newDoctorId: this.reassignDoctorId,
      })
      .subscribe(() => {
        this.closeReassignModal();
        this.load();
      });
  }

  // Reset password
  openResetPassword(patient: AdminPatient): void {
    this.resetTarget = patient;
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
      .put<any>(this.appConfig.getEndpointFor(`api/admin/reset-password/${this.resetTarget.userId}`), { newPassword: this.newPassword })
      .subscribe(() => {
        this.closeResetModal();
      });
  }

  // Delete
  confirmDelete(patient: AdminPatient): void {
    this.deleteTarget = patient;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.deleteTarget = null;
  }

  executeDelete(): void {
    if (!this.deleteTarget) return;
    this.http.delete(this.appConfig.getEndpointFor(`api/admin/delete-user/${this.deleteTarget.userId}`)).subscribe(() => {
      this.closeDeleteModal();
      this.load();
    });
  }
}
