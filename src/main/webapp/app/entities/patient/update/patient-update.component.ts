import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IDoctor } from 'app/entities/doctor/doctor.model';
import { DoctorService } from 'app/entities/doctor/service/doctor.service';
import { PatientType } from 'app/entities/enumerations/patient-type.model';
import { PatientSubtype } from 'app/entities/enumerations/patient-subtype.model';
import { PatientService } from '../service/patient.service';
import { IPatient } from '../patient.model';
import { PatientFormGroup, PatientFormService } from './patient-form.service';

@Component({
  selector: 'jhi-patient-update',
  templateUrl: './patient-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PatientUpdateComponent implements OnInit {
  isSaving = false;
  patient: IPatient | null = null;
  patientTypeValues = Object.keys(PatientType);
  patientSubtypeValues = Object.keys(PatientSubtype);

  usersSharedCollection: IUser[] = [];
  doctorsSharedCollection: IDoctor[] = [];

  protected patientService = inject(PatientService);
  protected patientFormService = inject(PatientFormService);
  protected userService = inject(UserService);
  protected doctorService = inject(DoctorService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PatientFormGroup = this.patientFormService.createPatientFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  compareDoctor = (o1: IDoctor | null, o2: IDoctor | null): boolean => this.doctorService.compareDoctor(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ patient }) => {
      this.patient = patient;
      if (patient) {
        this.updateForm(patient);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const patient = this.patientFormService.getPatient(this.editForm);
    if (patient.id !== null) {
      this.subscribeToSaveResponse(this.patientService.update(patient));
    } else {
      this.subscribeToSaveResponse(this.patientService.create(patient));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPatient>>): void {
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

  protected updateForm(patient: IPatient): void {
    this.patient = patient;
    this.patientFormService.resetForm(this.editForm, patient);

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, patient.user);
    this.doctorsSharedCollection = this.doctorService.addDoctorToCollectionIfMissing<IDoctor>(this.doctorsSharedCollection, patient.doctor);
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.patient?.user)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));

    this.doctorService
      .query()
      .pipe(map((res: HttpResponse<IDoctor[]>) => res.body ?? []))
      .pipe(map((doctors: IDoctor[]) => this.doctorService.addDoctorToCollectionIfMissing<IDoctor>(doctors, this.patient?.doctor)))
      .subscribe((doctors: IDoctor[]) => (this.doctorsSharedCollection = doctors));
  }
}
