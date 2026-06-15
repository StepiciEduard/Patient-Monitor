import { Routes } from '@angular/router';
import { Authority } from 'app/config/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component'),
    title: 'home.title',
  },
  {
    path: '',
    loadComponent: () => import('./layouts/navbar/navbar.component'),
    outlet: 'navbar',
  },
  {
    path: 'patient/dashboard',
    loadComponent: () => import('./patient-dashboard/patient-dashboard.component'),
    data: {
      authorities: [Authority.PATIENT],
    },
    canActivate: [UserRouteAccessService],
    title: 'Dashboard Pacient',
  },
  {
    path: 'patient/programari',
    loadComponent: () => import('./patient-dashboard/patient-appointments.component'),
    data: {
      authorities: [Authority.PATIENT],
    },
    canActivate: [UserRouteAccessService],
    title: 'Programari Pacient',
  },
  {
    path: 'doctor/dashboard',
    loadComponent: () => import('./doctor-dashboard/doctor-dashboard.component'),
    data: {
      authorities: [Authority.DOCTOR],
    },
    canActivate: [UserRouteAccessService],
    title: 'Dashboard Doctor',
  },
  {
    path: 'doctor/programari',
    loadComponent: () => import('./doctor-dashboard/doctor-appointments.component'),
    data: {
      authorities: [Authority.DOCTOR],
    },
    canActivate: [UserRouteAccessService],
    title: 'Programari Doctor',
  },
  {
    path: 'doctor/patient/:id',
    loadComponent: () => import('./doctor-dashboard/doctor-patient-view.component'),
    data: {
      authorities: [Authority.DOCTOR],
    },
    canActivate: [UserRouteAccessService],
    title: 'Vizualizare Pacient',
  },
  {
    path: 'admin/dashboard',
    loadComponent: () => import('./admin-dashboard/admin-dashboard.component'),
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    title: 'Admin Dashboard',
  },
  {
    path: 'admin',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component'),
    title: 'login.title',
  },
  {
    path: '',
    loadChildren: () => import(`./entities/entity.routes`),
  },
  ...errorRoute,
];

export default routes;
