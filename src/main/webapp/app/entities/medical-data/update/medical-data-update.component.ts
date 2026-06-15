import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPatient } from 'app/entities/patient/patient.model';
import { PatientService } from 'app/entities/patient/service/patient.service';
import { IMedicalData } from '../medical-data.model';
import { MedicalDataService } from '../service/medical-data.service';
import { MedicalDataFormGroup, MedicalDataFormService } from './medical-data-form.service';

@Component({
  selector: 'jhi-medical-data-update',
  templateUrl: './medical-data-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class MedicalDataUpdateComponent implements OnInit {
  isSaving = false;
  medicalData: IMedicalData | null = null;

  patientsSharedCollection: IPatient[] = [];

  protected medicalDataService = inject(MedicalDataService);
  protected medicalDataFormService = inject(MedicalDataFormService);
  protected patientService = inject(PatientService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: MedicalDataFormGroup = this.medicalDataFormService.createMedicalDataFormGroup();

  comparePatient = (o1: IPatient | null, o2: IPatient | null): boolean => this.patientService.comparePatient(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ medicalData }) => {
      this.medicalData = medicalData;
      if (medicalData) {
        this.updateForm(medicalData);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const medicalData = this.medicalDataFormService.getMedicalData(this.editForm);
    if (medicalData.id !== null) {
      this.subscribeToSaveResponse(this.medicalDataService.update(medicalData));
    } else {
      this.subscribeToSaveResponse(this.medicalDataService.create(medicalData));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IMedicalData>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(medicalData: IMedicalData): void {
    this.medicalData = medicalData;
    this.medicalDataFormService.resetForm(this.editForm, medicalData);

    this.patientsSharedCollection = this.patientService.addPatientToCollectionIfMissing<IPatient>(
      this.patientsSharedCollection,
      medicalData.patient,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.patientService
      .query()
      .pipe(map((res: HttpResponse<IPatient[]>) => res.body ?? []))
      .pipe(
        map((patients: IPatient[]) => this.patientService.addPatientToCollectionIfMissing<IPatient>(patients, this.medicalData?.patient)),
      )
      .subscribe((patients: IPatient[]) => (this.patientsSharedCollection = patients));
  }
}
