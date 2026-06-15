import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import AppointmentResolve from './route/appointment-routing-resolve.service';

const appointmentRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/appointment.component').then(m => m.AppointmentComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/appointment-detail.component').then(m => m.AppointmentDetailComponent),
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/appointment-update.component').then(m => m.AppointmentUpdateComponent),
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/appointment-update.component').then(m => m.AppointmentUpdateComponent),
    resolve: {
      appointment: AppointmentResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default appointmentRoute;
