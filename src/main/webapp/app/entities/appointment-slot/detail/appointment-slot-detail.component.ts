import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IAppointmentSlot } from '../appointment-slot.model';

@Component({
  selector: 'jhi-appointment-slot-detail',
  templateUrl: './appointment-slot-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class AppointmentSlotDetailComponent {
  appointmentSlot = input<IAppointmentSlot | null>(null);

  previousState(): void {
    window.history.back();
  }
}
