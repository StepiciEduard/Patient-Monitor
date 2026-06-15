import { Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import DoctorResolve from './route/doctor-routing-resolve.service';

const doctorRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/doctor.component').then(m => m.DoctorComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/doctor-detail.component').then(m => m.DoctorDetailComponent),
    resolve: {
      doctor: DoctorResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/doctor-update.component').then(m => m.DoctorUpdateComponent),
    resolve: {
      doctor: DoctorResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/doctor-update.component').then(m => m.DoctorUpdateComponent),
    resolve: {
      doctor: DoctorResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default doctorRoute;
