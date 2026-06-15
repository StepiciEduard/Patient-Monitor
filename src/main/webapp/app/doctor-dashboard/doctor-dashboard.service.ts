import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { DashboardData } from '../patient-dashboard/patient-dashboard.service';

export interface DoctorDashboardData {
  doctorName: string;
  specialization: string;
  totalPatients: number;
  patients: DoctorPatient[];
}

export interface DoctorPatient {
  id: number;
  firstName: string;
  lastName: string;
  patientType: string;
  patientSubtype: string;
  cnp: string;
  phoneNumber: string;
  gender: string;
  dateOfBirth: string;
  latestReading: any;
  anomalies24h: number;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class DoctorDashboardService {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  getMyPatients(): Observable<DoctorDashboardData> {
    return this.http.get<DoctorDashboardData>(this.appConfig.getEndpointFor('api/doctor/my-patients'));
  }

  getPatientDashboardData(patientId: number, interval: string): Observable<DashboardData> {
    return this.http.get<DashboardData>(
      this.appConfig.getEndpointFor(`api/doctor/patient/${patientId}/dashboard-data?interval=${interval}`),
    );
  }

  sendNotification(patientId: number, message: string): Observable<void> {
    return this.http.post<void>(this.appConfig.getEndpointFor('api/doctor/send-notification'), { patientId, message });
  }
}
