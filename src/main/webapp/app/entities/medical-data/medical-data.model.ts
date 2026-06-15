import dayjs from 'dayjs/esm';
import { IPatient } from 'app/entities/patient/patient.model';

export interface IMedicalData {
  id: number;
  timestamp?: dayjs.Dayjs | null;
  heartRate?: number | null;
  spo2?: number | null;
  temperature?: number | null;
  systolicBp?: number | null;
  diastolicBp?: number | null;
  hrv?: number | null;
  qtInterval?: number | null;
  bnp?: number | null;
  bloodGlucose?: number | null;
  respiratoryRate?: number | null;
  fev1?: number | null;
  etco2?: number | null;
  anomalyScore?: number | null;
  isAnomaly?: boolean | null;
  patient?: Pick<IPatient, 'id'> | null;
}

export type NewMedicalData = Omit<IMedicalData, 'id'> & { id: null };
