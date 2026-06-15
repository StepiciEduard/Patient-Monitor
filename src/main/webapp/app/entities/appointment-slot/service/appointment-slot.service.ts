import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IAppointmentSlot, NewAppointmentSlot } from '../appointment-slot.model';

export type PartialUpdateAppointmentSlot = Partial<IAppointmentSlot> & Pick<IAppointmentSlot, 'id'>;

type RestOf<T extends IAppointmentSlot | NewAppointmentSlot> = Omit<T, 'startTime' | 'endTime'> & {
  startTime?: string | null;
  endTime?: string | null;
};

export type RestAppointmentSlot = RestOf<IAppointmentSlot>;

export type NewRestAppointmentSlot = RestOf<NewAppointmentSlot>;

export type PartialUpdateRestAppointmentSlot = RestOf<PartialUpdateAppointmentSlot>;

export type EntityResponseType = HttpResponse<IAppointmentSlot>;
export type EntityArrayResponseType = HttpResponse<IAppointmentSlot[]>;

@Injectable({ providedIn: 'root' })
export class AppointmentSlotService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/appointment-slots');

  create(appointmentSlot: NewAppointmentSlot): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointmentSlot);
    return this.http
      .post<RestAppointmentSlot>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(appointmentSlot: IAppointmentSlot): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointmentSlot);
    return this.http
      .put<RestAppointmentSlot>(`${this.resourceUrl}/${this.getAppointmentSlotIdentifier(appointmentSlot)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(appointmentSlot: PartialUpdateAppointmentSlot): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(appointmentSlot);
    return this.http
      .patch<RestAppointmentSlot>(`${this.resourceUrl}/${this.getAppointmentSlotIdentifier(appointmentSlot)}`, copy, {
        observe: 'response',
      })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestAppointmentSlot>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAppointmentSlot[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getAppointmentSlotIdentifier(appointmentSlot: Pick<IAppointmentSlot, 'id'>): number {
    return appointmentSlot.id;
  }

  compareAppointmentSlot(o1: Pick<IAppointmentSlot, 'id'> | null, o2: Pick<IAppointmentSlot, 'id'> | null): boolean {
    return o1 && o2 ? this.getAppointmentSlotIdentifier(o1) === this.getAppointmentSlotIdentifier(o2) : o1 === o2;
  }

  addAppointmentSlotToCollectionIfMissing<Type extends Pick<IAppointmentSlot, 'id'>>(
    appointmentSlotCollection: Type[],
    ...appointmentSlotsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const appointmentSlots: Type[] = appointmentSlotsToCheck.filter(isPresent);
    if (appointmentSlots.length > 0) {
      const appointmentSlotCollectionIdentifiers = appointmentSlotCollection.map(appointmentSlotItem =>
        this.getAppointmentSlotIdentifier(appointmentSlotItem),
      );
      const appointmentSlotsToAdd = appointmentSlots.filter(appointmentSlotItem => {
        const appointmentSlotIdentifier = this.getAppointmentSlotIdentifier(appointmentSlotItem);
        if (appointmentSlotCollectionIdentifiers.includes(appointmentSlotIdentifier)) {
          return false;
        }
        appointmentSlotCollectionIdentifiers.push(appointmentSlotIdentifier);
        return true;
      });
      return [...appointmentSlotsToAdd, ...appointmentSlotCollection];
    }
    return appointmentSlotCollection;
  }

  protected convertDateFromClient<T extends IAppointmentSlot | NewAppointmentSlot | PartialUpdateAppointmentSlot>(
    appointmentSlot: T,
  ): RestOf<T> {
    return {
      ...appointmentSlot,
      startTime: appointmentSlot.startTime?.toJSON() ?? null,
      endTime: appointmentSlot.endTime?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restAppointmentSlot: RestAppointmentSlot): IAppointmentSlot {
    return {
      ...restAppointmentSlot,
      startTime: restAppointmentSlot.startTime ? dayjs(restAppointmentSlot.startTime) : undefined,
      endTime: restAppointmentSlot.endTime ? dayjs(restAppointmentSlot.endTime) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestAppointmentSlot>): HttpResponse<IAppointmentSlot> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestAppointmentSlot[]>): HttpResponse<IAppointmentSlot[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
