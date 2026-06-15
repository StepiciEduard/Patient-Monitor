import dayjs from 'dayjs/esm';

import { IAppointmentSlot, NewAppointmentSlot } from './appointment-slot.model';

export const sampleWithRequiredData: IAppointmentSlot = {
  id: 20376,
  startTime: dayjs('2026-02-20T02:22'),
  endTime: dayjs('2026-02-20T12:13'),
  isAvailable: false,
};

export const sampleWithPartialData: IAppointmentSlot = {
  id: 18164,
  startTime: dayjs('2026-02-20T00:30'),
  endTime: dayjs('2026-02-20T03:17'),
  isAvailable: false,
};

export const sampleWithFullData: IAppointmentSlot = {
  id: 31696,
  startTime: dayjs('2026-02-19T23:00'),
  endTime: dayjs('2026-02-19T17:43'),
  isAvailable: false,
};

export const sampleWithNewData: NewAppointmentSlot = {
  startTime: dayjs('2026-02-19T19:12'),
  endTime: dayjs('2026-02-20T11:59'),
  isAvailable: false,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
