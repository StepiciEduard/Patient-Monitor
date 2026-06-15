import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface AdminStats {
  totalUsers: number;
  totalDoctors: number;
  totalPatients: number;
  cardiacPatients: number;
  diabetesPatients: number;
  respiratoryPatients: number;
  anomaliesToday: number;
}

export interface AdminDoctor {
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

export interface AdminPatient {
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

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(this.appConfig.getEndpointFor('api/admin/dashboard-stats'));
  }

  getDoctors(): Observable<AdminDoctor[]> {
    return this.http.get<AdminDoctor[]>(this.appConfig.getEndpointFor('api/admin/doctors-list'));
  }

  getPatients(doctorId?: number): Observable<AdminPatient[]> {
    const url = doctorId ? `api/admin/patients-list?doctorId=${doctorId}` : 'api/admin/patients-list';
    return this.http.get<AdminPatient[]>(this.appConfig.getEndpointFor(url));
  }

  createDoctor(data: any): Observable<any> {
    return this.http.post(this.appConfig.getEndpointFor('api/admin/create-doctor'), data);
  }

  toggleUser(userId: number): Observable<any> {
    return this.http.put(this.appConfig.getEndpointFor(`api/admin/toggle-user/${userId}`), {});
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(this.appConfig.getEndpointFor(`api/admin/delete-user/${userId}`));
  }

  reassignPatient(patientId: number, newDoctorId: number): Observable<any> {
    return this.http.put(this.appConfig.getEndpointFor('api/admin/reassign-patient'), { patientId, newDoctorId });
  }

  resetPassword(userId: number, newPassword: string): Observable<any> {
    return this.http.put(this.appConfig.getEndpointFor(`api/admin/reset-password/${userId}`), { newPassword });
  }
}
