import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IMedicalData, NewMedicalData } from '../medical-data.model';

export type PartialUpdateMedicalData = Partial<IMedicalData> & Pick<IMedicalData, 'id'>;

type RestOf<T extends IMedicalData | NewMedicalData> = Omit<T, 'timestamp'> & {
  timestamp?: string | null;
};

export type RestMedicalData = RestOf<IMedicalData>;

export type NewRestMedicalData = RestOf<NewMedicalData>;

export type PartialUpdateRestMedicalData = RestOf<PartialUpdateMedicalData>;

export type EntityResponseType = HttpResponse<IMedicalData>;
export type EntityArrayResponseType = HttpResponse<IMedicalData[]>;

@Injectable({ providedIn: 'root' })
export class MedicalDataService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/medical-data');

  create(medicalData: NewMedicalData): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(medicalData);
    return this.http
      .post<RestMedicalData>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(medicalData: IMedicalData): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(medicalData);
    return this.http
      .put<RestMedicalData>(`${this.resourceUrl}/${this.getMedicalDataIdentifier(medicalData)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(medicalData: PartialUpdateMedicalData): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(medicalData);
    return this.http
      .patch<RestMedicalData>(`${this.resourceUrl}/${this.getMedicalDataIdentifier(medicalData)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestMedicalData>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestMedicalData[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getMedicalDataIdentifier(medicalData: Pick<IMedicalData, 'id'>): number {
    return medicalData.id;
  }

  compareMedicalData(o1: Pick<IMedicalData, 'id'> | null, o2: Pick<IMedicalData, 'id'> | null): boolean {
    return o1 && o2 ? this.getMedicalDataIdentifier(o1) === this.getMedicalDataIdentifier(o2) : o1 === o2;
  }

  addMedicalDataToCollectionIfMissing<Type extends Pick<IMedicalData, 'id'>>(
    medicalDataCollection: Type[],
    ...medicalDataToCheck: (Type | null | undefined)[]
  ): Type[] {
    const medicalData: Type[] = medicalDataToCheck.filter(isPresent);
    if (medicalData.length > 0) {
      const medicalDataCollectionIdentifiers = medicalDataCollection.map(medicalDataItem => this.getMedicalDataIdentifier(medicalDataItem));
      const medicalDataToAdd = medicalData.filter(medicalDataItem => {
        const medicalDataIdentifier = this.getMedicalDataIdentifier(medicalDataItem);
        if (medicalDataCollectionIdentifiers.includes(medicalDataIdentifier)) {
          return false;
        }
        medicalDataCollectionIdentifiers.push(medicalDataIdentifier);
        return true;
      });
      return [...medicalDataToAdd, ...medicalDataCollection];
    }
    return medicalDataCollection;
  }

  protected convertDateFromClient<T extends IMedicalData | NewMedicalData | PartialUpdateMedicalData>(medicalData: T): RestOf<T> {
    return {
      ...medicalData,
      timestamp: medicalData.timestamp?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restMedicalData: RestMedicalData): IMedicalData {
    return {
      ...restMedicalData,
      timestamp: restMedicalData.timestamp ? dayjs(restMedicalData.timestamp) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestMedicalData>): HttpResponse<IMedicalData> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestMedicalData[]>): HttpResponse<IMedicalData[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
