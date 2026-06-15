import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IDoctor, NewDoctor } from '../doctor.model';

export type PartialUpdateDoctor = Partial<IDoctor> & Pick<IDoctor, 'id'>;

export type EntityResponseType = HttpResponse<IDoctor>;
export type EntityArrayResponseType = HttpResponse<IDoctor[]>;

@Injectable({ providedIn: 'root' })
export class DoctorService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/doctors');

  create(doctor: NewDoctor): Observable<EntityResponseType> {
    return this.http.post<IDoctor>(this.resourceUrl, doctor, { observe: 'response' });
  }

  update(doctor: IDoctor): Observable<EntityResponseType> {
    return this.http.put<IDoctor>(`${this.resourceUrl}/${this.getDoctorIdentifier(doctor)}`, doctor, { observe: 'response' });
  }

  partialUpdate(doctor: PartialUpdateDoctor): Observable<EntityResponseType> {
    return this.http.patch<IDoctor>(`${this.resourceUrl}/${this.getDoctorIdentifier(doctor)}`, doctor, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IDoctor>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IDoctor[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getDoctorIdentifier(doctor: Pick<IDoctor, 'id'>): number {
    return doctor.id;
  }

  compareDoctor(o1: Pick<IDoctor, 'id'> | null, o2: Pick<IDoctor, 'id'> | null): boolean {
    return o1 && o2 ? this.getDoctorIdentifier(o1) === this.getDoctorIdentifier(o2) : o1 === o2;
  }

  addDoctorToCollectionIfMissing<Type extends Pick<IDoctor, 'id'>>(
    doctorCollection: Type[],
    ...doctorsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const doctors: Type[] = doctorsToCheck.filter(isPresent);
    if (doctors.length > 0) {
      const doctorCollectionIdentifiers = doctorCollection.map(doctorItem => this.getDoctorIdentifier(doctorItem));
      const doctorsToAdd = doctors.filter(doctorItem => {
        const doctorIdentifier = this.getDoctorIdentifier(doctorItem);
        if (doctorCollectionIdentifiers.includes(doctorIdentifier)) {
          return false;
        }
        doctorCollectionIdentifiers.push(doctorIdentifier);
        return true;
      });
      return [...doctorsToAdd, ...doctorCollection];
    }
    return doctorCollection;
  }
}
