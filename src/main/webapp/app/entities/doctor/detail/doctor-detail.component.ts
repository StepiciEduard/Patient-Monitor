import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IDoctor } from '../doctor.model';

@Component({
  selector: 'jhi-doctor-detail',
  templateUrl: './doctor-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class DoctorDetailComponent {
  doctor = input<IDoctor | null>(null);

  previousState(): void {
    window.history.back();
  }
}
