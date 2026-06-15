import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IAppointmentSlot } from 'app/entities/appointment-slot/appointment-slot.model';
import { AppointmentSlotService } from 'app/entities/appointment-slot/service/appointment-slot.service';
import { IPatient } from 'app/entities/patient/patient.model';
import { PatientService } from 'app/entities/patient/service/patient.service';
import { IDoctor } from 'app/entities/doctor/doctor.model';
import { DoctorService } from 'app/entities/doctor/service/doctor.service';
import { IAppointment } from '../appointment.model';
import { AppointmentService } from '../service/appointment.service';
import { AppointmentFormService } from './appointment-form.service';

import { AppointmentUpdateComponent } from './appointment-update.component';

describe('Appointment Management Update Component', () => {
  let comp: AppointmentUpdateComponent;
  let fixture: ComponentFixture<AppointmentUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let appointmentFormService: AppointmentFormService;
  let appointmentService: AppointmentService;
  let appointmentSlotService: AppointmentSlotService;
  let patientService: PatientService;
  let doctorService: DoctorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppointmentUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(AppointmentUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AppointmentUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    appointmentFormService = TestBed.inject(AppointmentFormService);
    appointmentService = TestBed.inject(AppointmentService);
    appointmentSlotService = TestBed.inject(AppointmentSlotService);
    patientService = TestBed.inject(PatientService);
    doctorService = TestBed.inject(DoctorService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call AppointmentSlot query and add missing value', () => {
      const appointment: IAppointment = { id: 584 };
      const slot: IAppointmentSlot = { id: 3453 };
      appointment.slot = slot;

      const appointmentSlotCollection: IAppointmentSlot[] = [{ id: 3453 }];
      jest.spyOn(appointmentSlotService, 'query').mockReturnValue(of(new HttpResponse({ body: appointmentSlotCollection })));
      const additionalAppointmentSlots = [slot];
      const expectedCollection: IAppointmentSlot[] = [...additionalAppointmentSlots, ...appointmentSlotCollection];
      jest.spyOn(appointmentSlotService, 'addAppointmentSlotToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ appointment });
      comp.ngOnInit();

      expect(appointmentSlotService.query).toHaveBeenCalled();
      expect(appointmentSlotService.addAppointmentSlotToCollectionIfMissing).toHaveBeenCalledWith(
        appointmentSlotCollection,
        ...additionalAppointmentSlots.map(expect.objectContaining),
      );
      expect(comp.appointmentSlotsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Patient query and add missing value', () => {
      const appointment: IAppointment = { id: 584 };
      const patient: IPatient = { id: 16668 };
      appointment.patient = patient;

      const patientCollection: IPatient[] = [{ id: 16668 }];
      jest.spyOn(patientService, 'query').mockReturnValue(of(new HttpResponse({ body: patientCollection })));
      const additionalPatients = [patient];
      const expectedCollection: IPatient[] = [...additionalPatients, ...patientCollection];
      jest.spyOn(patientService, 'addPatientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ appointment });
      comp.ngOnInit();

      expect(patientService.query).toHaveBeenCalled();
      expect(patientService.addPatientToCollectionIfMissing).toHaveBeenCalledWith(
        patientCollection,
        ...additionalPatients.map(expect.objectContaining),
      );
      expect(comp.patientsSharedCollection).toEqual(expectedCollection);
    });

    it('should call Doctor query and add missing value', () => {
      const appointment: IAppointment = { id: 584 };
      const doctor: IDoctor = { id: 758 };
      appointment.doctor = doctor;

      const doctorCollection: IDoctor[] = [{ id: 758 }];
      jest.spyOn(doctorService, 'query').mockReturnValue(of(new HttpResponse({ body: doctorCollection })));
      const additionalDoctors = [doctor];
      const expectedCollection: IDoctor[] = [...additionalDoctors, ...doctorCollection];
      jest.spyOn(doctorService, 'addDoctorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ appointment });
      comp.ngOnInit();

      expect(doctorService.query).toHaveBeenCalled();
      expect(doctorService.addDoctorToCollectionIfMissing).toHaveBeenCalledWith(
        doctorCollection,
        ...additionalDoctors.map(expect.objectContaining),
      );
      expect(comp.doctorsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const appointment: IAppointment = { id: 584 };
      const slot: IAppointmentSlot = { id: 3453 };
      appointment.slot = slot;
      const patient: IPatient = { id: 16668 };
      appointment.patient = patient;
      const doctor: IDoctor = { id: 758 };
      appointment.doctor = doctor;

      activatedRoute.data = of({ appointment });
      comp.ngOnInit();

      expect(comp.appointmentSlotsSharedCollection).toContainEqual(slot);
      expect(comp.patientsSharedCollection).toContainEqual(patient);
      expect(comp.doctorsSharedCollection).toContainEqual(doctor);
      expect(comp.appointment).toEqual(appointment);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppointment>>();
      const appointment = { id: 3011 };
      jest.spyOn(appointmentFormService, 'getAppointment').mockReturnValue(appointment);
      jest.spyOn(appointmentService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appointment });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: appointment }));
      saveSubject.complete();

      // THEN
      expect(appointmentFormService.getAppointment).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(appointmentService.update).toHaveBeenCalledWith(expect.objectContaining(appointment));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppointment>>();
      const appointment = { id: 3011 };
      jest.spyOn(appointmentFormService, 'getAppointment').mockReturnValue({ id: null });
      jest.spyOn(appointmentService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appointment: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: appointment }));
      saveSubject.complete();

      // THEN
      expect(appointmentFormService.getAppointment).toHaveBeenCalled();
      expect(appointmentService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppointment>>();
      const appointment = { id: 3011 };
      jest.spyOn(appointmentService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appointment });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(appointmentService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareAppointmentSlot', () => {
      it('should forward to appointmentSlotService', () => {
        const entity = { id: 3453 };
        const entity2 = { id: 15494 };
        jest.spyOn(appointmentSlotService, 'compareAppointmentSlot');
        comp.compareAppointmentSlot(entity, entity2);
        expect(appointmentSlotService.compareAppointmentSlot).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('comparePatient', () => {
      it('should forward to patientService', () => {
        const entity = { id: 16668 };
        const entity2 = { id: 16914 };
        jest.spyOn(patientService, 'comparePatient');
        comp.comparePatient(entity, entity2);
        expect(patientService.comparePatient).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareDoctor', () => {
      it('should forward to doctorService', () => {
        const entity = { id: 758 };
        const entity2 = { id: 23078 };
        jest.spyOn(doctorService, 'compareDoctor');
        comp.compareDoctor(entity, entity2);
        expect(doctorService.compareDoctor).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
