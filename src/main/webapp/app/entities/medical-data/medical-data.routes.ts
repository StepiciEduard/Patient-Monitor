import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import MedicalDataResolve from './route/medical-data-routing-resolve.service';

const medicalDataRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/medical-data.component').then(m => m.MedicalDataComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/medical-data-detail.component').then(m => m.MedicalDataDetailComponent),
    resolve: {
      medicalData: MedicalDataResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/medical-data-update.component').then(m => m.MedicalDataUpdateComponent),
    resolve: {
      medicalData: MedicalDataResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/medical-data-update.component').then(m => m.MedicalDataUpdateComponent),
    resolve: {
      medicalData: MedicalDataResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default medicalDataRoute;
