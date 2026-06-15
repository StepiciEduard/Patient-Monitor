import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IAppointment, NewAppointment } from '../appointment.model';

export type PartialUpdateAppointment = Partial<IAppointment> & Pick<IAppointment, 'id'>;

type RestOf<T extends IAppointment | NewAppointment> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestAppointment = RestOf<IAppointment>;

export type NewRestAppointment = RestOf<NewAppointment>;

export type PartialUpdateRestAppointment = RestOf<PartialUpdateAppointment>;

export type EntityResponseType = HttpResponse<IAppointment>;
export type EntityArrayResponseType = HttpResponse<IAppointment[]>;

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/appointments');

  create(appointment: NewAppointment): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointment);
    return this.http
      .post<RestAppointment>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(appointment: IAppointment): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointment);
    return this.http
      .put<RestAppointment>(`${this.resourceUrl}/${this.getAppointmentIdentifier(appointment)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(appointment: PartialUpdateAppointment): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointment);
    return this.http
      .patch<RestAppointment>(`${this.resourceUrl}/${this.getAppointmentIdentifier(appointment)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestAppointment>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAppointment[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getAppointmentIdentifier(appointment: Pick<IAppointment, 'id'>): number {
    return appointment.id;
  }

  compareAppointment(o1: Pick<IAppointment, 'id'> | null, o2: Pick<IAppointment, 'id'> | null): boolean {
    return o1 && o2 ? this.getAppointmentIdentifier(o1) === this.getAppointmentIdentifier(o2) : o1 === o2;
  }

  addAppointmentToCollectionIfMissing<Type extends Pick<IAppointment, 'id'>>(
    appointmentCollection: Type[],
    ...appointmentsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const appointments: Type[] = appointmentsToCheck.filter(isPresent);
    if (appointments.length > 0) {
      const appointmentCollectionIdentifiers = appointmentCollection.map(appointmentItem => this.getAppointmentIdentifier(appointmentItem));
      const appointmentsToAdd = appointments.filter(appointmentItem => {
        const appointmentIdentifier = this.getAppointmentIdentifier(appointmentItem);
        if (appointmentCollectionIdentifiers.includes(appointmentIdentifier)) {
          return false;
        }
        appointmentCollectionIdentifiers.push(appointmentIdentifier);
        return true;
      });
      return [...appointmentsToAdd, ...appointmentCollection];
    }
    return appointmentCollection;
  }

  protected convertDateFromClient<T extends IAppointment | NewAppointment | PartialUpdateAppointment>(appointment: T): RestOf<T> {
    return {
      ...appointment,
      createdAt: appointment.createdAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restAppointment: RestAppointment): IAppointment {
    return {
      ...restAppointment,
      createdAt: restAppointment.createdAt ? dayjs(restAppointment.createdAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestAppointment>): HttpResponse<IAppointment> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestAppointment[]>): HttpResponse<IAppointment[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
