import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IMedicalData } from '../medical-data.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../medical-data.test-samples';

import { MedicalDataService, RestMedicalData } from './medical-data.service';

const requireRestSample: RestMedicalData = {
  ...sampleWithRequiredData,
  timestamp: sampleWithRequiredData.timestamp?.toJSON(),
};

describe('MedicalData Service', () => {
  let service: MedicalDataService;
  let httpMock: HttpTestingController;
  let expectedResult: IMedicalData | IMedicalData[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(MedicalDataService);
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

    it('should create a MedicalData', () => {
      const medicalData = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(medicalData).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a MedicalData', () => {
      const medicalData = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(medicalData).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a MedicalData', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of MedicalData', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a MedicalData', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addMedicalDataToCollectionIfMissing', () => {
      it('should add a MedicalData to an empty array', () => {
        const medicalData: IMedicalData = sampleWithRequiredData;
        expectedResult = service.addMedicalDataToCollectionIfMissing([], medicalData);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(medicalData);
      });

      it('should not add a MedicalData to an array that contains it', () => {
        const medicalData: IMedicalData = sampleWithRequiredData;
        const medicalDataCollection: IMedicalData[] = [
          {
            ...medicalData,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addMedicalDataToCollectionIfMissing(medicalDataCollection, medicalData);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a MedicalData to an array that doesn't contain it", () => {
        const medicalData: IMedicalData = sampleWithRequiredData;
        const medicalDataCollection: IMedicalData[] = [sampleWithPartialData];
        expectedResult = service.addMedicalDataToCollectionIfMissing(medicalDataCollection, medicalData);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(medicalData);
      });

      it('should add only unique MedicalData to an array', () => {
        const medicalDataArray: IMedicalData[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const medicalDataCollection: IMedicalData[] = [sampleWithRequiredData];
        expectedResult = service.addMedicalDataToCollectionIfMissing(medicalDataCollection, ...medicalDataArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const medicalData: IMedicalData = sampleWithRequiredData;
        const medicalData2: IMedicalData = sampleWithPartialData;
        expectedResult = service.addMedicalDataToCollectionIfMissing([], medicalData, medicalData2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(medicalData);
        expect(expectedResult).toContain(medicalData2);
      });

      it('should accept null and undefined values', () => {
        const medicalData: IMedicalData = sampleWithRequiredData;
        expectedResult = service.addMedicalDataToCollectionIfMissing([], null, medicalData, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(medicalData);
      });

      it('should return initial array if no MedicalData is added', () => {
        const medicalDataCollection: IMedicalData[] = [sampleWithRequiredData];
        expectedResult = service.addMedicalDataToCollectionIfMissing(medicalDataCollection, undefined, null);
        expect(expectedResult).toEqual(medicalDataCollection);
      });
    });

    describe('compareMedicalData', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareMedicalData(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 18207 };
        const entity2 = null;

        const compareResult1 = service.compareMedicalData(entity1, entity2);
        const compareResult2 = service.compareMedicalData(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 18207 };
        const entity2 = { id: 32533 };

        const compareResult1 = service.compareMedicalData(entity1, entity2);
        const compareResult2 = service.compareMedicalData(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 18207 };
        const entity2 = { id: 18207 };

        const compareResult1 = service.compareMedicalData(entity1, entity2);
        const compareResult2 = service.compareMedicalData(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
