import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IDoctor } from '../doctor.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../doctor.test-samples';

import { DoctorService } from './doctor.service';

const requireRestSample: IDoctor = {
  ...sampleWithRequiredData,
};

describe('Doctor Service', () => {
  let service: DoctorService;
  let httpMock: HttpTestingController;
  let expectedResult: IDoctor | IDoctor[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(DoctorService);
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

    it('should create a Doctor', () => {
      const doctor = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(doctor).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Doctor', () => {
      const doctor = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(doctor).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Doctor', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Doctor', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Doctor', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addDoctorToCollectionIfMissing', () => {
      it('should add a Doctor to an empty array', () => {
        const doctor: IDoctor = sampleWithRequiredData;
        expectedResult = service.addDoctorToCollectionIfMissing([], doctor);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(doctor);
      });

      it('should not add a Doctor to an array that contains it', () => {
        const doctor: IDoctor = sampleWithRequiredData;
        const doctorCollection: IDoctor[] = [
          {
            ...doctor,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addDoctorToCollectionIfMissing(doctorCollection, doctor);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Doctor to an array that doesn't contain it", () => {
        const doctor: IDoctor = sampleWithRequiredData;
        const doctorCollection: IDoctor[] = [sampleWithPartialData];
        expectedResult = service.addDoctorToCollectionIfMissing(doctorCollection, doctor);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(doctor);
      });

      it('should add only unique Doctor to an array', () => {
        const doctorArray: IDoctor[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const doctorCollection: IDoctor[] = [sampleWithRequiredData];
        expectedResult = service.addDoctorToCollectionIfMissing(doctorCollection, ...doctorArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const doctor: IDoctor = sampleWithRequiredData;
        const doctor2: IDoctor = sampleWithPartialData;
        expectedResult = service.addDoctorToCollectionIfMissing([], doctor, doctor2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(doctor);
        expect(expectedResult).toContain(doctor2);
      });

      it('should accept null and undefined values', () => {
        const doctor: IDoctor = sampleWithRequiredData;
        expectedResult = service.addDoctorToCollectionIfMissing([], null, doctor, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(doctor);
      });

      it('should return initial array if no Doctor is added', () => {
        const doctorCollection: IDoctor[] = [sampleWithRequiredData];
        expectedResult = service.addDoctorToCollectionIfMissing(doctorCollection, undefined, null);
        expect(expectedResult).toEqual(doctorCollection);
      });
    });

    describe('compareDoctor', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareDoctor(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 758 };
        const entity2 = null;

        const compareResult1 = service.compareDoctor(entity1, entity2);
        const compareResult2 = service.compareDoctor(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 758 };
        const entity2 = { id: 23078 };

        const compareResult1 = service.compareDoctor(entity1, entity2);
        const compareResult2 = service.compareDoctor(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 758 };
        const entity2 = { id: 758 };

        const compareResult1 = service.compareDoctor(entity1, entity2);
        const compareResult2 = service.compareDoctor(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
