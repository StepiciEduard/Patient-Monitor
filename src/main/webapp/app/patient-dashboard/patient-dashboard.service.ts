import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface DashboardData {
  patientType: string;
  patientSubtype: string;
  patientName: string;
  latest: Reading | null;
  series: Reading[];
  totalReadings: number;
}

export interface Reading {
  timestamp: string;
  heartRate: number | null;
  spo2: number | null;
  temperature: number | null;
  systolicBp: number | null;
  diastolicBp: number | null;
  respiratoryRate: number | null;
  hrv: number | null;
  qtInterval: number | null;
  bnp: number | null;
  bloodGlucose: number | null;
  fev1: number | null;
  etco2: number | null;
  isAnomaly: boolean;
  anomalyScore: number | null;
}

@Injectable({ providedIn: 'root' })
export class PatientDashboardService {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);

  getDashboardData(interval: string): Observable<DashboardData> {
    return this.http.get<DashboardData>(this.appConfig.getEndpointFor(`api/patient/dashboard-data?interval=${interval}`));
  }
}
