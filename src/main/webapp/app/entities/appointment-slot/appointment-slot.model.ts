import dayjs from 'dayjs/esm';
import { IDoctor } from 'app/entities/doctor/doctor.model';

export interface IAppointmentSlot {
  id: number;
  startTime?: dayjs.Dayjs | null;
  endTime?: dayjs.Dayjs | null;
  isAvailable?: boolean | null;
  doctor?: Pick<IDoctor, 'id' | 'specialization'> | null;
}

export type NewAppointmentSlot = Omit<IAppointmentSlot, 'id'> & { id: null };
