import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../doctor.test-samples';

import { DoctorFormService } from './doctor-form.service';

describe('Doctor Form Service', () => {
  let service: DoctorFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DoctorFormService);
  });

  describe('Service methods', () => {
    describe('createDoctorFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createDoctorFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            specialization: expect.any(Object),
            phone: expect.any(Object),
            officeLocation: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing IDoctor should create a new form with FormGroup', () => {
        const formGroup = service.createDoctorFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            specialization: expect.any(Object),
            phone: expect.any(Object),
            officeLocation: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getDoctor', () => {
      it('should return NewDoctor for default Doctor initial value', () => {
        const formGroup = service.createDoctorFormGroup(sampleWithNewData);

        const doctor = service.getDoctor(formGroup) as any;

        expect(doctor).toMatchObject(sampleWithNewData);
      });

      it('should return NewDoctor for empty Doctor initial value', () => {
        const formGroup = service.createDoctorFormGroup();

        const doctor = service.getDoctor(formGroup) as any;

        expect(doctor).toMatchObject({});
      });

      it('should return IDoctor', () => {
        const formGroup = service.createDoctorFormGroup(sampleWithRequiredData);

        const doctor = service.getDoctor(formGroup) as any;

        expect(doctor).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IDoctor should not enable id FormControl', () => {
        const formGroup = service.createDoctorFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewDoctor should disable id FormControl', () => {
        const formGroup = service.createDoctorFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
