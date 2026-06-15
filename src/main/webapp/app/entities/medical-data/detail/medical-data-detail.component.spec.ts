import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { MedicalDataDetailComponent } from './medical-data-detail.component';

describe('MedicalData Management Detail Component', () => {
  let comp: MedicalDataDetailComponent;
  let fixture: ComponentFixture<MedicalDataDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MedicalDataDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./medical-data-detail.component').then(m => m.MedicalDataDetailComponent),
              resolve: { medicalData: () => of({ id: 18207 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(MedicalDataDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MedicalDataDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load medicalData on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', MedicalDataDetailComponent);

      // THEN
      expect(instance.medicalData()).toEqual(expect.objectContaining({ id: 18207 }));
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
