import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import AppointmentSlotResolve from './route/appointment-slot-routing-resolve.service';

const appointmentSlotRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/appointment-slot.component').then(m => m.AppointmentSlotComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/appointment-slot-detail.component').then(m => m.AppointmentSlotDetailComponent),
    resolve: {
      appointmentSlot: AppointmentSlotResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/appointment-slot-update.component').then(m => m.AppointmentSlotUpdateComponent),
    resolve: {
      appointmentSlot: AppointmentSlotResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/appointment-slot-update.component').then(m => m.AppointmentSlotUpdateComponent),
    resolve: {
      appointmentSlot: AppointmentSlotResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default appointmentSlotRoute;
