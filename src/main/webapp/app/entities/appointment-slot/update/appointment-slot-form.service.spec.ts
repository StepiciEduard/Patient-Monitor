import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../appointment-slot.test-samples';

import { AppointmentSlotFormService } from './appointment-slot-form.service';

describe('AppointmentSlot Form Service', () => {
  let service: AppointmentSlotFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AppointmentSlotFormService);
  });

  describe('Service methods', () => {
    describe('createAppointmentSlotFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createAppointmentSlotFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            startTime: expect.any(Object),
            endTime: expect.any(Object),
            isAvailable: expect.any(Object),
            doctor: expect.any(Object),
          }),
        );
      });

      it('passing IAppointmentSlot should create a new form with FormGroup', () => {
        const formGroup = service.createAppointmentSlotFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            startTime: expect.any(Object),
            endTime: expect.any(Object),
            isAvailable: expect.any(Object),
            doctor: expect.any(Object),
          }),
        );
      });
    });

    describe('getAppointmentSlot', () => {
      it('should return NewAppointmentSlot for default AppointmentSlot initial value', () => {
        const formGroup = service.createAppointmentSlotFormGroup(sampleWithNewData);

        const appointmentSlot = service.getAppointmentSlot(formGroup) as any;

        expect(appointmentSlot).toMatchObject(sampleWithNewData);
      });

      it('should return NewAppointmentSlot for empty AppointmentSlot initial value', () => {
        const formGroup = service.createAppointmentSlotFormGroup();

        const appointmentSlot = service.getAppointmentSlot(formGroup) as any;

        expect(appointmentSlot).toMatchObject({});
      });

      it('should return IAppointmentSlot', () => {
        const formGroup = service.createAppointmentSlotFormGroup(sampleWithRequiredData);

        const appointmentSlot = service.getAppointmentSlot(formGroup) as any;

        expect(appointmentSlot).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IAppointmentSlot should not enable id FormControl', () => {
        const formGroup = service.createAppointmentSlotFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewAppointmentSlot should disable id FormControl', () => {
        const formGroup = service.createAppointmentSlotFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
