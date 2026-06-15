import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IMedicalData } from '../medical-data.model';
import { MedicalDataService } from '../service/medical-data.service';

const medicalDataResolve = (route: ActivatedRouteSnapshot): Observable<null | IMedicalData> => {
  const id = route.params.id;
  if (id) {
    return inject(MedicalDataService)
      .find(id)
      .pipe(
        mergeMap((medicalData: HttpResponse<IMedicalData>) => {
          if (medicalData.body) {
            return of(medicalData.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default medicalDataResolve;
