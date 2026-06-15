import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { IDoctor } from 'app/entities/doctor/doctor.model';
import { PatientType } from 'app/entities/enumerations/patient-type.model';
import { PatientSubtype } from 'app/entities/enumerations/patient-subtype.model';

export interface IPatient {
  id: number;
  cnp?: string | null;
  phoneNumber?: string | null;
  address?: string | null;
  patientType?: keyof typeof PatientType | null;
  patientSubtype?: keyof typeof PatientSubtype | null;
  dateOfBirth?: dayjs.Dayjs | null;
  gender?: string | null;
  hba1c?: number | null;
  bmi?: number | null;
  fev1Baseline?: number | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
  doctor?: Pick<IDoctor, 'id' | 'specialization'> | null;
}

export type NewPatient = Omit<IPatient, 'id'> & { id: null };
