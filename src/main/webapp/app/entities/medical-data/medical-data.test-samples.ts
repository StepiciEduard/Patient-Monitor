import dayjs from 'dayjs/esm';

import { IMedicalData, NewMedicalData } from './medical-data.model';

export const sampleWithRequiredData: IMedicalData = {
  id: 29697,
  timestamp: dayjs('2026-02-19T23:34'),
};

export const sampleWithPartialData: IMedicalData = {
  id: 17921,
  timestamp: dayjs('2026-02-19T16:52'),
  spo2: 9447.45,
  temperature: 11492.22,
  hrv: 5829.65,
  qtInterval: 31968,
  bloodGlucose: 23788.78,
  respiratoryRate: 13913,
  fev1: 14731.44,
  etco2: 31605.19,
};

export const sampleWithFullData: IMedicalData = {
  id: 25464,
  timestamp: dayjs('2026-02-20T08:45'),
  heartRate: 9776,
  spo2: 4818.28,
  temperature: 9732.92,
  systolicBp: 21566,
  diastolicBp: 4424,
  hrv: 28252.94,
  qtInterval: 10179,
  bnp: 24226.84,
  bloodGlucose: 11243.76,
  respiratoryRate: 11435,
  fev1: 19197.72,
  etco2: 30355.06,
  anomalyScore: 10895.82,
  isAnomaly: true,
};

export const sampleWithNewData: NewMedicalData = {
  timestamp: dayjs('2026-02-19T17:43'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
