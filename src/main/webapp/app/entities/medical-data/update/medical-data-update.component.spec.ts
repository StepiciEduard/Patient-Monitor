import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IPatient } from 'app/entities/patient/patient.model';
import { PatientService } from 'app/entities/patient/service/patient.service';
import { MedicalDataService } from '../service/medical-data.service';
import { IMedicalData } from '../medical-data.model';
import { MedicalDataFormService } from './medical-data-form.service';

import { MedicalDataUpdateComponent } from './medical-data-update.component';

describe('MedicalData Management Update Component', () => {
  let comp: MedicalDataUpdateComponent;
  let fixture: ComponentFixture<MedicalDataUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let medicalDataFormService: MedicalDataFormService;
  let medicalDataService: MedicalDataService;
  let patientService: PatientService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MedicalDataUpdateComponent],
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
      .overrideTemplate(MedicalDataUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(MedicalDataUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    medicalDataFormService = TestBed.inject(MedicalDataFormService);
    medicalDataService = TestBed.inject(MedicalDataService);
    patientService = TestBed.inject(PatientService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Patient query and add missing value', () => {
      const medicalData: IMedicalData = { id: 32533 };
      const patient: IPatient = { id: 16668 };
      medicalData.patient = patient;

      const patientCollection: IPatient[] = [{ id: 16668 }];
      jest.spyOn(patientService, 'query').mockReturnValue(of(new HttpResponse({ body: patientCollection })));
      const additionalPatients = [patient];
      const expectedCollection: IPatient[] = [...additionalPatients, ...patientCollection];
      jest.spyOn(patientService, 'addPatientToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ medicalData });
      comp.ngOnInit();

      expect(patientService.query).toHaveBeenCalled();
      expect(patientService.addPatientToCollectionIfMissing).toHaveBeenCalledWith(
        patientCollection,
        ...additionalPatients.map(expect.objectContaining),
      );
      expect(comp.patientsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const medicalData: IMedicalData = { id: 32533 };
      const patient: IPatient = { id: 16668 };
      medicalData.patient = patient;

      activatedRoute.data = of({ medicalData });
      comp.ngOnInit();

      expect(comp.patientsSharedCollection).toContainEqual(patient);
      expect(comp.medicalData).toEqual(medicalData);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMedicalData>>();
      const medicalData = { id: 18207 };
      jest.spyOn(medicalDataFormService, 'getMedicalData').mockReturnValue(medicalData);
      jest.spyOn(medicalDataService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ medicalData });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: medicalData }));
      saveSubject.complete();

      // THEN
      expect(medicalDataFormService.getMedicalData).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(medicalDataService.update).toHaveBeenCalledWith(expect.objectContaining(medicalData));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMedicalData>>();
      const medicalData = { id: 18207 };
      jest.spyOn(medicalDataFormService, 'getMedicalData').mockReturnValue({ id: null });
      jest.spyOn(medicalDataService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ medicalData: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: medicalData }));
      saveSubject.complete();

      // THEN
      expect(medicalDataFormService.getMedicalData).toHaveBeenCalled();
      expect(medicalDataService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMedicalData>>();
      const medicalData = { id: 18207 };
      jest.spyOn(medicalDataService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ medicalData });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(medicalDataService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('comparePatient', () => {
      it('should forward to patientService', () => {
        const entity = { id: 16668 };
        const entity2 = { id: 16914 };
        jest.spyOn(patientService, 'comparePatient');
        comp.comparePatient(entity, entity2);
        expect(patientService.comparePatient).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
