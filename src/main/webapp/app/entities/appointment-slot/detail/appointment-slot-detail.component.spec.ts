import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { AppointmentSlotDetailComponent } from './appointment-slot-detail.component';

describe('AppointmentSlot Management Detail Component', () => {
  let comp: AppointmentSlotDetailComponent;
  let fixture: ComponentFixture<AppointmentSlotDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppointmentSlotDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./appointment-slot-detail.component').then(m => m.AppointmentSlotDetailComponent),
              resolve: { appointmentSlot: () => of({ id: 3453 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(AppointmentSlotDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppointmentSlotDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load appointmentSlot on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', AppointmentSlotDetailComponent);

      // THEN
      expect(instance.appointmentSlot()).toEqual(expect.objectContaining({ id: 3453 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
