import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPatient, NewPatient } from '../patient.model';

export type PartialUpdatePatient = Partial<IPatient> & Pick<IPatient, 'id'>;

type RestOf<T extends IPatient | NewPatient> = Omit<T, 'dateOfBirth'> & {
  dateOfBirth?: string | null;
};

export type RestPatient = RestOf<IPatient>;

export type NewRestPatient = RestOf<NewPatient>;

export type PartialUpdateRestPatient = RestOf<PartialUpdatePatient>;

export type EntityResponseType = HttpResponse<IPatient>;
export type EntityArrayResponseType = HttpResponse<IPatient[]>;

@Injectable({ providedIn: 'root' })
export class PatientService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/patients');

  create(patient: NewPatient): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(patient);
    return this.http
      .post<RestPatient>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(patient: IPatient): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(patient);
    return this.http
      .put<RestPatient>(`${this.resourceUrl}/${this.getPatientIdentifier(patient)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(patient: PartialUpdatePatient): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(patient);
    return this.http
      .patch<RestPatient>(`${this.resourceUrl}/${this.getPatientIdentifier(patient)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestPatient>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestPatient[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getPatientIdentifier(patient: Pick<IPatient, 'id'>): number {
    return patient.id;
  }

  comparePatient(o1: Pick<IPatient, 'id'> | null, o2: Pick<IPatient, 'id'> | null): boolean {
    return o1 && o2 ? this.getPatientIdentifier(o1) === this.getPatientIdentifier(o2) : o1 === o2;
  }

  addPatientToCollectionIfMissing<Type extends Pick<IPatient, 'id'>>(
    patientCollection: Type[],
    ...patientsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const patients: Type[] = patientsToCheck.filter(isPresent);
    if (patients.length > 0) {
      const patientCollectionIdentifiers = patientCollection.map(patientItem => this.getPatientIdentifier(patientItem));
      const patientsToAdd = patients.filter(patientItem => {
        const patientIdentifier = this.getPatientIdentifier(patientItem);
        if (patientCollectionIdentifiers.includes(patientIdentifier)) {
          return false;
        }
        patientCollectionIdentifiers.push(patientIdentifier);
        return true;
      });
      return [...patientsToAdd, ...patientCollection];
    }
    return patientCollection;
  }

  protected convertDateFromClient<T extends IPatient | NewPatient | PartialUpdatePatient>(patient: T): RestOf<T> {
    return {
      ...patient,
      dateOfBirth: patient.dateOfBirth?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restPatient: RestPatient): IPatient {
    return {
      ...restPatient,
      dateOfBirth: restPatient.dateOfBirth ? dayjs(restPatient.dateOfBirth) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestPatient>): HttpResponse<IPatient> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestPatient[]>): HttpResponse<IPatient[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
