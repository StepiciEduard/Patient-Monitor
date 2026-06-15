import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IDoctor } from 'app/entities/doctor/doctor.model';
import { DoctorService } from 'app/entities/doctor/service/doctor.service';
import { IAppointmentSlot } from '../appointment-slot.model';
import { AppointmentSlotService } from '../service/appointment-slot.service';
import { AppointmentSlotFormGroup, AppointmentSlotFormService } from './appointment-slot-form.service';

@Component({
  selector: 'jhi-appointment-slot-update',
  templateUrl: './appointment-slot-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class AppointmentSlotUpdateComponent implements OnInit {
  isSaving = false;
  appointmentSlot: IAppointmentSlot | null = null;

  doctorsSharedCollection: IDoctor[] = [];

  protected appointmentSlotService = inject(AppointmentSlotService);
  protected appointmentSlotFormService = inject(AppointmentSlotFormService);
  protected doctorService = inject(DoctorService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AppointmentSlotFormGroup = this.appointmentSlotFormService.createAppointmentSlotFormGroup();

  compareDoctor = (o1: IDoctor | null, o2: IDoctor | null): boolean => this.doctorService.compareDoctor(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appointmentSlot }) => {
      this.appointmentSlot = appointmentSlot;
      if (appointmentSlot) {
        this.updateForm(appointmentSlot);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const appointmentSlot = this.appointmentSlotFormService.getAppointmentSlot(this.editForm);
    if (appointmentSlot.id !== null) {
      this.subscribeToSaveResponse(this.appointmentSlotService.update(appointmentSlot));
    } else {
      this.subscribeToSaveResponse(this.appointmentSlotService.create(appointmentSlot));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppointmentSlot>>): void {
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

  protected updateForm(appointmentSlot: IAppointmentSlot): void {
    this.appointmentSlot = appointmentSlot;
    this.appointmentSlotFormService.resetForm(this.editForm, appointmentSlot);

    this.doctorsSharedCollection = this.doctorService.addDoctorToCollectionIfMissing<IDoctor>(
      this.doctorsSharedCollection,
      appointmentSlot.doctor,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.doctorService
      .query()
      .pipe(map((res: HttpResponse<IDoctor[]>) => res.body ?? []))
      .pipe(map((doctors: IDoctor[]) => this.doctorService.addDoctorToCollectionIfMissing<IDoctor>(doctors, this.appointmentSlot?.doctor)))
      .subscribe((doctors: IDoctor[]) => (this.doctorsSharedCollection = doctors));
  }
}
