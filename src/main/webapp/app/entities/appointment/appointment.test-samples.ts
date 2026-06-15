import dayjs from 'dayjs/esm';

import { IAppointment, NewAppointment } from './appointment.model';

export const sampleWithRequiredData: IAppointment = {
  id: 13399,
  status: 'COMPLETED',
  createdAt: dayjs('2026-02-19T18:15'),
};

export const sampleWithPartialData: IAppointment = {
  id: 19136,
  status: 'SCHEDULED',
  createdAt: dayjs('2026-02-20T03:18'),
};

export const sampleWithFullData: IAppointment = {
  id: 5963,
  status: 'SCHEDULED',
  notes: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2026-02-20T12:34'),
};

export const sampleWithNewData: NewAppointment = {
  status: 'CANCELLED',
  createdAt: dayjs('2026-02-20T07:33'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
