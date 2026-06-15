import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../medical-data.test-samples';

import { MedicalDataFormService } from './medical-data-form.service';

describe('MedicalData Form Service', () => {
  let service: MedicalDataFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MedicalDataFormService);
  });

  describe('Service methods', () => {
    describe('createMedicalDataFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createMedicalDataFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            timestamp: expect.any(Object),
            heartRate: expect.any(Object),
            spo2: expect.any(Object),
            temperature: expect.any(Object),
            systolicBp: expect.any(Object),
            diastolicBp: expect.any(Object),
            hrv: expect.any(Object),
            qtInterval: expect.any(Object),
            bnp: expect.any(Object),
            bloodGlucose: expect.any(Object),
            respiratoryRate: expect.any(Object),
            fev1: expect.any(Object),
            etco2: expect.any(Object),
            anomalyScore: expect.any(Object),
            isAnomaly: expect.any(Object),
            patient: expect.any(Object),
          }),
        );
      });

      it('passing IMedicalData should create a new form with FormGroup', () => {
        const formGroup = service.createMedicalDataFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            timestamp: expect.any(Object),
            heartRate: expect.any(Object),
            spo2: expect.any(Object),
            temperature: expect.any(Object),
            systolicBp: expect.any(Object),
            diastolicBp: expect.any(Object),
            hrv: expect.any(Object),
            qtInterval: expect.any(Object),
            bnp: expect.any(Object),
            bloodGlucose: expect.any(Object),
            respiratoryRate: expect.any(Object),
            fev1: expect.any(Object),
            etco2: expect.any(Object),
            anomalyScore: expect.any(Object),
            isAnomaly: expect.any(Object),
            patient: expect.any(Object),
          }),
        );
      });
    });

    describe('getMedicalData', () => {
      it('should return NewMedicalData for default MedicalData initial value', () => {
        const formGroup = service.createMedicalDataFormGroup(sampleWithNewData);

        const medicalData = service.getMedicalData(formGroup) as any;

        expect(medicalData).toMatchObject(sampleWithNewData);
      });

      it('should return NewMedicalData for empty MedicalData initial value', () => {
        const formGroup = service.createMedicalDataFormGroup();

        const medicalData = service.getMedicalData(formGroup) as any;

        expect(medicalData).toMatchObject({});
      });

      it('should return IMedicalData', () => {
        const formGroup = service.createMedicalDataFormGroup(sampleWithRequiredData);

        const medicalData = service.getMedicalData(formGroup) as any;

        expect(medicalData).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IMedicalData should not enable id FormControl', () => {
        const formGroup = service.createMedicalDataFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewMedicalData should disable id FormControl', () => {
        const formGroup = service.createMedicalDataFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
