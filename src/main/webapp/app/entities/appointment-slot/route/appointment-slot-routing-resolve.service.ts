import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IAppointmentSlot } from '../appointment-slot.model';
import { AppointmentSlotService } from '../service/appointment-slot.service';

const appointmentSlotResolve = (route: ActivatedRouteSnapshot): Observable<null | IAppointmentSlot> => {
  const id = route.params.id;
  if (id) {
    return inject(AppointmentSlotService)
      .find(id)
      .pipe(
        mergeMap((appointmentSlot: HttpResponse<IAppointmentSlot>) => {
          if (appointmentSlot.body) {
            return of(appointmentSlot.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default appointmentSlotResolve;
