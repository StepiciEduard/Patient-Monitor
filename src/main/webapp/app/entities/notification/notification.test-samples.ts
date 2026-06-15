import dayjs from 'dayjs/esm';

import { INotification, NewNotification } from './notification.model';

export const sampleWithRequiredData: INotification = {
  id: 10110,
  type: 'APPOINTMENT_REMINDER',
  title: 'neighboring',
  message: '../fake-data/blob/hipster.txt',
  isRead: true,
  createdAt: dayjs('2026-02-19T18:28'),
};

export const sampleWithPartialData: INotification = {
  id: 27987,
  type: 'GENERAL',
  title: 'ha',
  message: '../fake-data/blob/hipster.txt',
  isRead: true,
  createdAt: dayjs('2026-02-19T22:42'),
};

export const sampleWithFullData: INotification = {
  id: 5787,
  type: 'ANOMALY_DETECTED',
  title: 'inferior boo bruised',
  message: '../fake-data/blob/hipster.txt',
  isRead: false,
  createdAt: dayjs('2026-02-19T17:05'),
};

export const sampleWithNewData: NewNotification = {
  type: 'GENERAL',
  title: 'usually casement vein',
  message: '../fake-data/blob/hipster.txt',
  isRead: true,
  createdAt: dayjs('2026-02-20T10:45'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
