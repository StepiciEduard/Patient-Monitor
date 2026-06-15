import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AlertError } from 'app/shared/alert/alert-error.model';
import { EventManager, EventWithContent } from 'app/core/util/event-manager.service';
import { DataUtils, FileLoadError } from 'app/core/util/data-util.service';
import { IAppointmentSlot } from 'app/entities/appointment-slot/appointment-slot.model';
import { AppointmentSlotService } from 'app/entities/appointment-slot/service/appointment-slot.service';
import { IPatient } from 'app/entities/patient/patient.model';
import { PatientService } from 'app/entities/patient/service/patient.service';
import { IDoctor } from 'app/entities/doctor/doctor.model';
import { DoctorService } from 'app/entities/doctor/service/doctor.service';
import { AppointmentStatus } from 'app/entities/enumerations/appointment-status.model';
import { AppointmentService } from '../service/appointment.service';
import { IAppointment } from '../appointment.model';
import { AppointmentFormGroup, AppointmentFormService } from './appointment-form.service';

@Component({
  selector: 'jhi-appointment-update',
  templateUrl: './appointment-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class AppointmentUpdateComponent implements OnInit {
  isSaving = false;
  appointment: IAppointment | null = null;
  appointmentStatusValues = Object.keys(AppointmentStatus);

  appointmentSlotsSharedCollection: IAppointmentSlot[] = [];
  patientsSharedCollection: IPatient[] = [];
  doctorsSharedCollection: IDoctor[] = [];

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected appointmentService = inject(AppointmentService);
  protected appointmentFormService = inject(AppointmentFormService);
  protected appointmentSlotService = inject(AppointmentSlotService);
  protected patientService = inject(PatientService);
  protected doctorService = inject(DoctorService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: AppointmentFormGroup = this.appointmentFormService.createAppointmentFormGroup();

  compareAppointmentSlot = (o1: IAppointmentSlot | null, o2: IAppointmentSlot | null): boolean =>
    this.appointmentSlotService.compareAppointmentSlot(o1, o2);

  comparePatient = (o1: IPatient | null, o2: IPatient | null): boolean => this.patientService.comparePatient(o1, o2);

  compareDoctor = (o1: IDoctor | null, o2: IDoctor | null): boolean => this.doctorService.compareDoctor(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ appointment }) => {
      this.appointment = appointment;
      if (appointment) {
        this.updateForm(appointment);
      }

      this.loadRelationshipsOptions();
    });
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(base64String: string, contentType: string | null | undefined): void {
    this.dataUtils.openFile(base64String, contentType);
  }

  setFileData(event: Event, field: string, isImage: boolean): void {
    this.dataUtils.loadFileToForm(event, this.editForm, field, isImage).subscribe({
      error: (err: FileLoadError) =>
        this.eventManager.broadcast(new EventWithContent<AlertError>('patientMonitorApp.error', { ...err, key: `error.file.${err.key}` })),
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const appointment = this.appointmentFormService.getAppointment(this.editForm);
    if (appointment.id !== null) {
      this.subscribeToSaveResponse(this.appointmentService.update(appointment));
    } else {
      this.subscribeToSaveResponse(this.appointmentService.create(appointment));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAppointment>>): void {
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

  protected updateForm(appointment: IAppointment): void {
    this.appointment = appointment;
    this.appointmentFormService.resetForm(this.editForm, appointment);

    this.appointmentSlotsSharedCollection = this.appointmentSlotService.addAppointmentSlotToCollectionIfMissing<IAppointmentSlot>(
      this.appointmentSlotsSharedCollection,
      appointment.slot,
    );
    this.patientsSharedCollection = this.patientService.addPatientToCollectionIfMissing<IPatient>(
      this.patientsSharedCollection,
      appointment.patient,
    );
    this.doctorsSharedCollection = this.doctorService.addDoctorToCollectionIfMissing<IDoctor>(
      this.doctorsSharedCollection,
      appointment.doctor,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.appointmentSlotService
      .query()
      .pipe(map((res: HttpResponse<IAppointmentSlot[]>) => res.body ?? []))
      .pipe(
        map((appointmentSlots: IAppointmentSlot[]) =>
          this.appointmentSlotService.addAppointmentSlotToCollectionIfMissing<IAppointmentSlot>(appointmentSlots, this.appointment?.slot),
        ),
      )
      .subscribe((appointmentSlots: IAppointmentSlot[]) => (this.appointmentSlotsSharedCollection = appointmentSlots));

    this.patientService
      .query()
      .pipe(map((res: HttpResponse<IPatient[]>) => res.body ?? []))
      .pipe(
        map((patients: IPatient[]) => this.patientService.addPatientToCollectionIfMissing<IPatient>(patients, this.appointment?.patient)),
      )
      .subscribe((patients: IPatient[]) => (this.patientsSharedCollection = patients));

    this.doctorService
      .query()
      .pipe(map((res: HttpResponse<IDoctor[]>) => res.body ?? []))
      .pipe(map((doctors: IDoctor[]) => this.doctorService.addDoctorToCollectionIfMissing<IDoctor>(doctors, this.appointment?.doctor)))
      .subscribe((doctors: IDoctor[]) => (this.doctorsSharedCollection = doctors));
  }
}
