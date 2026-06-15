import { IDoctor, NewDoctor } from './doctor.model';

export const sampleWithRequiredData: IDoctor = {
  id: 670,
  specialization: 'toothbrush righteously boo',
};

export const sampleWithPartialData: IDoctor = {
  id: 25275,
  specialization: 'through',
  phone: '201.493.9721 x673',
};

export const sampleWithFullData: IDoctor = {
  id: 20131,
  specialization: 'wildly ick',
  phone: '1-836-830-3682 x980',
  officeLocation: 'cappelletti expansion',
};

export const sampleWithNewData: NewDoctor = {
  specialization: 'yum',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
