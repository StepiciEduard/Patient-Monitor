import dayjs from 'dayjs/esm';

import { IPatient, NewPatient } from './patient.model';

export const sampleWithRequiredData: IPatient = {
  id: 2514,
  cnp: 'custody if wh',
  patientType: 'CARDIAC',
  patientSubtype: 'CRITICAL',
  dateOfBirth: dayjs('2026-02-20'),
};

export const sampleWithPartialData: IPatient = {
  id: 5584,
  cnp: 'multicolored ',
  phoneNumber: 'while peninsula',
  address: 'absentmindedly zowie ragged',
  patientType: 'RESPIRATORY',
  patientSubtype: 'CRITICAL',
  dateOfBirth: dayjs('2026-02-19'),
  hba1c: 30098.99,
  bmi: 26777.98,
  fev1Baseline: 18097.04,
};

export const sampleWithFullData: IPatient = {
  id: 9819,
  cnp: 'anXXXXXXXXXXX',
  phoneNumber: 'outdo guilt',
  address: 'across',
  patientType: 'CARDIAC',
  patientSubtype: 'BORDERLINE',
  dateOfBirth: dayjs('2026-02-19'),
  gender: 'downchange',
  hba1c: 16472.28,
  bmi: 638.77,
  fev1Baseline: 14430.89,
};

export const sampleWithNewData: NewPatient = {
  cnp: 'kick labourer',
  patientType: 'RESPIRATORY',
  patientSubtype: 'CRITICAL',
  dateOfBirth: dayjs('2026-02-20'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
