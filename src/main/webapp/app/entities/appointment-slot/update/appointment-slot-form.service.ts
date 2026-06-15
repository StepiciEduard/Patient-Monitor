import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAppointmentSlot, NewAppointmentSlot } from '../appointment-slot.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAppointmentSlot for edit and NewAppointmentSlotFormGroupInput for create.
 */
type AppointmentSlotFormGroupInput = IAppointmentSlot | PartialWithRequiredKeyOf<NewAppointmentSlot>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAppointmentSlot | NewAppointmentSlot> = Omit<T, 'startTime' | 'endTime'> & {
  startTime?: string | null;
  endTime?: string | null;
};

type AppointmentSlotFormRawValue = FormValueOf<IAppointmentSlot>;

type NewAppointmentSlotFormRawValue = FormValueOf<NewAppointmentSlot>;

type AppointmentSlotFormDefaults = Pick<NewAppointmentSlot, 'id' | 'startTime' | 'endTime' | 'isAvailable'>;

type AppointmentSlotFormGroupContent = {
  id: FormControl<AppointmentSlotFormRawValue['id'] | NewAppointmentSlot['id']>;
  startTime: FormControl<AppointmentSlotFormRawValue['startTime']>;
  endTime: FormControl<AppointmentSlotFormRawValue['endTime']>;
  isAvailable: FormControl<AppointmentSlotFormRawValue['isAvailable']>;
  doctor: FormControl<AppointmentSlotFormRawValue['doctor']>;
};

export type AppointmentSlotFormGroup = FormGroup<AppointmentSlotFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AppointmentSlotFormService {
  createAppointmentSlotFormGroup(appointmentSlot: AppointmentSlotFormGroupInput = { id: null }): AppointmentSlotFormGroup {
    const appointmentSlotRawValue = this.convertAppointmentSlotToAppointmentSlotRawValue({
      ...this.getFormDefaults(),
      ...appointmentSlot,
    });
    return new FormGroup<AppointmentSlotFormGroupContent>({
      id: new FormControl(
        { value: appointmentSlotRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      startTime: new FormControl(appointmentSlotRawValue.startTime, {
        validators: [Validators.required],
      }),
      endTime: new FormControl(appointmentSlotRawValue.endTime, {
        validators: [Validators.required],
      }),
      isAvailable: new FormControl(appointmentSlotRawValue.isAvailable, {
        validators: [Validators.required],
      }),
      doctor: new FormControl(appointmentSlotRawValue.doctor, {
        validators: [Validators.required],
      }),
    });
  }

  getAppointmentSlot(form: AppointmentSlotFormGroup): IAppointmentSlot | NewAppointmentSlot {
    return this.convertAppointmentSlotRawValueToAppointmentSlot(
      form.getRawValue() as AppointmentSlotFormRawValue | NewAppointmentSlotFormRawValue,
    );
  }

  resetForm(form: AppointmentSlotFormGroup, appointmentSlot: AppointmentSlotFormGroupInput): void {
    const appointmentSlotRawValue = this.convertAppointmentSlotToAppointmentSlotRawValue({ ...this.getFormDefaults(), ...appointmentSlot });
    form.reset(
      {
        ...appointmentSlotRawValue,
        id: { value: appointmentSlotRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): AppointmentSlotFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      startTime: currentTime,
      endTime: currentTime,
      isAvailable: false,
    };
  }

  private convertAppointmentSlotRawValueToAppointmentSlot(
    rawAppointmentSlot: AppointmentSlotFormRawValue | NewAppointmentSlotFormRawValue,
  ): IAppointmentSlot | NewAppointmentSlot {
    return {
      ...rawAppointmentSlot,
      startTime: dayjs(rawAppointmentSlot.startTime, DATE_TIME_FORMAT),
      endTime: dayjs(rawAppointmentSlot.endTime, DATE_TIME_FORMAT),
    };
  }

  private convertAppointmentSlotToAppointmentSlotRawValue(
    appointmentSlot: IAppointmentSlot | (Partial<NewAppointmentSlot> & AppointmentSlotFormDefaults),
  ): AppointmentSlotFormRawValue | PartialWithRequiredKeyOf<NewAppointmentSlotFormRawValue> {
    return {
      ...appointmentSlot,
      startTime: appointmentSlot.startTime ? appointmentSlot.startTime.format(DATE_TIME_FORMAT) : undefined,
      endTime: appointmentSlot.endTime ? appointmentSlot.endTime.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
