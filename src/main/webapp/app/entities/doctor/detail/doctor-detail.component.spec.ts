import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { DoctorDetailComponent } from './doctor-detail.component';

describe('Doctor Management Detail Component', () => {
  let comp: DoctorDetailComponent;
  let fixture: ComponentFixture<DoctorDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./doctor-detail.component').then(m => m.DoctorDetailComponent),
              resolve: { doctor: () => of({ id: 758 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(DoctorDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DoctorDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load doctor on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', DoctorDetailComponent);

      // THEN
      expect(instance.doctor()).toEqual(expect.objectContaining({ id: 758 }));
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
