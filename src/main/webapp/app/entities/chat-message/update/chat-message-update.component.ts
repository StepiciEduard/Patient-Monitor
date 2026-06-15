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
import { IPatient } from 'app/entities/patient/patient.model';
import { PatientService } from 'app/entities/patient/service/patient.service';
import { ChatRole } from 'app/entities/enumerations/chat-role.model';
import { ChatMessageService } from '../service/chat-message.service';
import { IChatMessage } from '../chat-message.model';
import { ChatMessageFormGroup, ChatMessageFormService } from './chat-message-form.service';

@Component({
  selector: 'jhi-chat-message-update',
  templateUrl: './chat-message-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ChatMessageUpdateComponent implements OnInit {
  isSaving = false;
  chatMessage: IChatMessage | null = null;
  chatRoleValues = Object.keys(ChatRole);

  patientsSharedCollection: IPatient[] = [];

  protected dataUtils = inject(DataUtils);
  protected eventManager = inject(EventManager);
  protected chatMessageService = inject(ChatMessageService);
  protected chatMessageFormService = inject(ChatMessageFormService);
  protected patientService = inject(PatientService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ChatMessageFormGroup = this.chatMessageFormService.createChatMessageFormGroup();

  comparePatient = (o1: IPatient | null, o2: IPatient | null): boolean => this.patientService.comparePatient(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ chatMessage }) => {
      this.chatMessage = chatMessage;
      if (chatMessage) {
        this.updateForm(chatMessage);
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
    const chatMessage = this.chatMessageFormService.getChatMessage(this.editForm);
    if (chatMessage.id !== null) {
      this.subscribeToSaveResponse(this.chatMessageService.update(chatMessage));
    } else {
      this.subscribeToSaveResponse(this.chatMessageService.create(chatMessage));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IChatMessage>>): void {
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

  protected updateForm(chatMessage: IChatMessage): void {
    this.chatMessage = chatMessage;
    this.chatMessageFormService.resetForm(this.editForm, chatMessage);

    this.patientsSharedCollection = this.patientService.addPatientToCollectionIfMissing<IPatient>(
      this.patientsSharedCollection,
      chatMessage.patient,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.patientService
      .query()
      .pipe(map((res: HttpResponse<IPatient[]>) => res.body ?? []))
      .pipe(
        map((patients: IPatient[]) => this.patientService.addPatientToCollectionIfMissing<IPatient>(patients, this.chatMessage?.patient)),
      )
      .subscribe((patients: IPatient[]) => (this.patientsSharedCollection = patients));
  }
}
