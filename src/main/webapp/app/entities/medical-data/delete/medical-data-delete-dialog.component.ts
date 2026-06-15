import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IMedicalData } from '../medical-data.model';
import { MedicalDataService } from '../service/medical-data.service';

@Component({
  templateUrl: './medical-data-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class MedicalDataDeleteDialogComponent {
  medicalData?: IMedicalData;

  protected medicalDataService = inject(MedicalDataService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.medicalDataService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
