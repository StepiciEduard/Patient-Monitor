import dayjs from 'dayjs/esm';
import { IAppointmentSlot } from 'app/entities/appointment-slot/appointment-slot.model';
import { IPatient } from 'app/entities/patient/patient.model';
import { IDoctor } from 'app/entities/doctor/doctor.model';
import { AppointmentStatus } from 'app/entities/enumerations/appointment-status.model';

export interface IAppointment {
  id: number;
  status?: keyof typeof AppointmentStatus | null;
  notes?: string | null;
  createdAt?: dayjs.Dayjs | null;
  slot?: Pick<IAppointmentSlot, 'id'> | null;
  patient?: Pick<IPatient, 'id'> | null;
  doctor?: Pick<IDoctor, 'id' | 'specialization'> | null;
}

export type NewAppointment = Omit<IAppointment, 'id'> & { id: null };
