import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IMedicalData } from '../medical-data.model';

@Component({
  selector: 'jhi-medical-data-detail',
  templateUrl: './medical-data-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class MedicalDataDetailComponent {
  medicalData = input<IMedicalData | null>(null);

  previousState(): void {
    window.history.back();
  }
}
