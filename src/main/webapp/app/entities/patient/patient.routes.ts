import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import PatientResolve from './route/patient-routing-resolve.service';

const patientRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/patient.component').then(m => m.PatientComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/patient-detail.component').then(m => m.PatientDetailComponent),
    resolve: {
      patient: PatientResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/patient-update.component').then(m => m.PatientUpdateComponent),
    resolve: {
      patient: PatientResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/patient-update.component').then(m => m.PatientUpdateComponent),
    resolve: {
      patient: PatientResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default patientRoute;
