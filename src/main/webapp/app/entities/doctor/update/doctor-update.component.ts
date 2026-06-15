import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IDoctor } from '../doctor.model';
import { DoctorService } from '../service/doctor.service';
import { DoctorFormGroup, DoctorFormService } from './doctor-form.service';

@Component({
  selector: 'jhi-doctor-update',
  templateUrl: './doctor-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class DoctorUpdateComponent implements OnInit {
  isSaving = false;
  doctor: IDoctor | null = null;

  usersSharedCollection: IUser[] = [];

  protected doctorService = inject(DoctorService);
  protected doctorFormService = inject(DoctorFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: DoctorFormGroup = this.doctorFormService.createDoctorFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ doctor }) => {
      this.doctor = doctor;
      if (doctor) {
        this.updateForm(doctor);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const doctor = this.doctorFormService.getDoctor(this.editForm);
    if (doctor.id !== null) {
      this.subscribeToSaveResponse(this.doctorService.update(doctor));
    } else {
      this.subscribeToSaveResponse(this.doctorService.create(doctor));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IDoctor>>): void {
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

  protected updateForm(doctor: IDoctor): void {
    this.doctor = doctor;
    this.doctorFormService.resetForm(this.editForm, doctor);

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, doctor.user);
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.doctor?.user)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }
}
