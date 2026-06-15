import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IDoctor } from 'app/entities/doctor/doctor.model';
import { DoctorService } from 'app/entities/doctor/service/doctor.service';
import { AppointmentSlotService } from '../service/appointment-slot.service';
import { IAppointmentSlot } from '../appointment-slot.model';
import { AppointmentSlotFormService } from './appointment-slot-form.service';

import { AppointmentSlotUpdateComponent } from './appointment-slot-update.component';

describe('AppointmentSlot Management Update Component', () => {
  let comp: AppointmentSlotUpdateComponent;
  let fixture: ComponentFixture<AppointmentSlotUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let appointmentSlotFormService: AppointmentSlotFormService;
  let appointmentSlotService: AppointmentSlotService;
  let doctorService: DoctorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppointmentSlotUpdateComponent],
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
      .overrideTemplate(AppointmentSlotUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(AppointmentSlotUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    appointmentSlotFormService = TestBed.inject(AppointmentSlotFormService);
    appointmentSlotService = TestBed.inject(AppointmentSlotService);
    doctorService = TestBed.inject(DoctorService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Doctor query and add missing value', () => {
      const appointmentSlot: IAppointmentSlot = { id: 15494 };
      const doctor: IDoctor = { id: 758 };
      appointmentSlot.doctor = doctor;

      const doctorCollection: IDoctor[] = [{ id: 758 }];
      jest.spyOn(doctorService, 'query').mockReturnValue(of(new HttpResponse({ body: doctorCollection })));
      const additionalDoctors = [doctor];
      const expectedCollection: IDoctor[] = [...additionalDoctors, ...doctorCollection];
      jest.spyOn(doctorService, 'addDoctorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ appointmentSlot });
      comp.ngOnInit();

      expect(doctorService.query).toHaveBeenCalled();
      expect(doctorService.addDoctorToCollectionIfMissing).toHaveBeenCalledWith(
        doctorCollection,
        ...additionalDoctors.map(expect.objectContaining),
      );
      expect(comp.doctorsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const appointmentSlot: IAppointmentSlot = { id: 15494 };
      const doctor: IDoctor = { id: 758 };
      appointmentSlot.doctor = doctor;

      activatedRoute.data = of({ appointmentSlot });
      comp.ngOnInit();

      expect(comp.doctorsSharedCollection).toContainEqual(doctor);
      expect(comp.appointmentSlot).toEqual(appointmentSlot);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppointmentSlot>>();
      const appointmentSlot = { id: 3453 };
      jest.spyOn(appointmentSlotFormService, 'getAppointmentSlot').mockReturnValue(appointmentSlot);
      jest.spyOn(appointmentSlotService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appointmentSlot });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: appointmentSlot }));
      saveSubject.complete();

      // THEN
      expect(appointmentSlotFormService.getAppointmentSlot).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(appointmentSlotService.update).toHaveBeenCalledWith(expect.objectContaining(appointmentSlot));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppointmentSlot>>();
      const appointmentSlot = { id: 3453 };
      jest.spyOn(appointmentSlotFormService, 'getAppointmentSlot').mockReturnValue({ id: null });
      jest.spyOn(appointmentSlotService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appointmentSlot: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: appointmentSlot }));
      saveSubject.complete();

      // THEN
      expect(appointmentSlotFormService.getAppointmentSlot).toHaveBeenCalled();
      expect(appointmentSlotService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IAppointmentSlot>>();
      const appointmentSlot = { id: 3453 };
      jest.spyOn(appointmentSlotService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ appointmentSlot });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(appointmentSlotService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
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
