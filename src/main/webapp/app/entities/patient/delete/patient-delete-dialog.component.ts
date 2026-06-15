import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPatient } from '../patient.model';
import { PatientService } from '../service/patient.service';

@Component({
  templateUrl: './patient-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PatientDeleteDialogComponent {
  patient?: IPatient;

  protected patientService = inject(PatientService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.patientService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
