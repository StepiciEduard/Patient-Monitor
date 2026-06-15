import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IDoctor } from '../doctor.model';
import { DoctorService } from '../service/doctor.service';

@Component({
  templateUrl: './doctor-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class DoctorDeleteDialogComponent {
  doctor?: IDoctor;

  protected doctorService = inject(DoctorService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.doctorService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
