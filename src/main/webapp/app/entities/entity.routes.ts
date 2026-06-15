import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'patientMonitorApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'doctor',
    data: { pageTitle: 'patientMonitorApp.doctor.home.title' },
    loadChildren: () => import('./doctor/doctor.routes'),
  },
  {
    path: 'patient',
    data: { pageTitle: 'patientMonitorApp.patient.home.title' },
    loadChildren: () => import('./patient/patient.routes'),
  },
  {
    path: 'medical-data',
    data: { pageTitle: 'patientMonitorApp.medicalData.home.title' },
    loadChildren: () => import('./medical-data/medical-data.routes'),
  },
  {
    path: 'notification',
    data: { pageTitle: 'patientMonitorApp.notification.home.title' },
    loadChildren: () => import('./notification/notification.routes'),
  },
  {
    path: 'appointment-slot',
    data: { pageTitle: 'patientMonitorApp.appointmentSlot.home.title' },
    loadChildren: () => import('./appointment-slot/appointment-slot.routes'),
  },
  {
    path: 'appointment',
    data: { pageTitle: 'patientMonitorApp.appointment.home.title' },
    loadChildren: () => import('./appointment/appointment.routes'),
  },
  {
    path: 'chat-message',
    data: { pageTitle: 'patientMonitorApp.chatMessage.home.title' },
    loadChildren: () => import('./chat-message/chat-message.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
