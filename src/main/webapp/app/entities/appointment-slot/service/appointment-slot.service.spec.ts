import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IAppointmentSlot } from '../appointment-slot.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../appointment-slot.test-samples';

import { AppointmentSlotService, RestAppointmentSlot } from './appointment-slot.service';

const requireRestSample: RestAppointmentSlot = {
  ...sampleWithRequiredData,
  startTime: sampleWithRequiredData.startTime?.toJSON(),
  endTime: sampleWithRequiredData.endTime?.toJSON(),
};

describe('AppointmentSlot Service', () => {
  let service: AppointmentSlotService;
  let httpMock: HttpTestingController;
  let expectedResult: IAppointmentSlot | IAppointmentSlot[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(AppointmentSlotService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a AppointmentSlot', () => {
      const appointmentSlot = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(appointmentSlot).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a AppointmentSlot', () => {
      const appointmentSlot = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(appointmentSlot).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a AppointmentSlot', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of AppointmentSlot', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a AppointmentSlot', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addAppointmentSlotToCollectionIfMissing', () => {
      it('should add a AppointmentSlot to an empty array', () => {
        const appointmentSlot: IAppointmentSlot = sampleWithRequiredData;
        expectedResult = service.addAppointmentSlotToCollectionIfMissing([], appointmentSlot);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(appointmentSlot);
      });

      it('should not add a AppointmentSlot to an array that contains it', () => {
        const appointmentSlot: IAppointmentSlot = sampleWithRequiredData;
        const appointmentSlotCollection: IAppointmentSlot[] = [
          {
            ...appointmentSlot,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAppointmentSlotToCollectionIfMissing(appointmentSlotCollection, appointmentSlot);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a AppointmentSlot to an array that doesn't contain it", () => {
        const appointmentSlot: IAppointmentSlot = sampleWithRequiredData;
        const appointmentSlotCollection: IAppointmentSlot[] = [sampleWithPartialData];
        expectedResult = service.addAppointmentSlotToCollectionIfMissing(appointmentSlotCollection, appointmentSlot);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(appointmentSlot);
      });

      it('should add only unique AppointmentSlot to an array', () => {
        const appointmentSlotArray: IAppointmentSlot[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const appointmentSlotCollection: IAppointmentSlot[] = [sampleWithRequiredData];
        expectedResult = service.addAppointmentSlotToCollectionIfMissing(appointmentSlotCollection, ...appointmentSlotArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const appointmentSlot: IAppointmentSlot = sampleWithRequiredData;
        const appointmentSlot2: IAppointmentSlot = sampleWithPartialData;
        expectedResult = service.addAppointmentSlotToCollectionIfMissing([], appointmentSlot, appointmentSlot2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(appointmentSlot);
        expect(expectedResult).toContain(appointmentSlot2);
      });

      it('should accept null and undefined values', () => {
        const appointmentSlot: IAppointmentSlot = sampleWithRequiredData;
        expectedResult = service.addAppointmentSlotToCollectionIfMissing([], null, appointmentSlot, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(appointmentSlot);
      });

      it('should return initial array if no AppointmentSlot is added', () => {
        const appointmentSlotCollection: IAppointmentSlot[] = [sampleWithRequiredData];
        expectedResult = service.addAppointmentSlotToCollectionIfMissing(appointmentSlotCollection, undefined, null);
        expect(expectedResult).toEqual(appointmentSlotCollection);
      });
    });

    describe('compareAppointmentSlot', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAppointmentSlot(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 3453 };
        const entity2 = null;

        const compareResult1 = service.compareAppointmentSlot(entity1, entity2);
        const compareResult2 = service.compareAppointmentSlot(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 3453 };
        const entity2 = { id: 15494 };

        const compareResult1 = service.compareAppointmentSlot(entity1, entity2);
        const compareResult2 = service.compareAppointmentSlot(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 3453 };
        const entity2 = { id: 3453 };

        const compareResult1 = service.compareAppointmentSlot(entity1, entity2);
        const compareResult2 = service.compareAppointmentSlot(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
