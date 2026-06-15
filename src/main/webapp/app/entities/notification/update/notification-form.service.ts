import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { INotification, NewNotification } from '../notification.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts INotification for edit and NewNotificationFormGroupInput for create.
 */
type NotificationFormGroupInput = INotification | PartialWithRequiredKeyOf<NewNotification>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends INotification | NewNotification> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type NotificationFormRawValue = FormValueOf<INotification>;

type NewNotificationFormRawValue = FormValueOf<NewNotification>;

type NotificationFormDefaults = Pick<NewNotification, 'id' | 'isRead' | 'createdAt'>;

type NotificationFormGroupContent = {
  id: FormControl<NotificationFormRawValue['id'] | NewNotification['id']>;
  type: FormControl<NotificationFormRawValue['type']>;
  title: FormControl<NotificationFormRawValue['title']>;
  message: FormControl<NotificationFormRawValue['message']>;
  isRead: FormControl<NotificationFormRawValue['isRead']>;
  createdAt: FormControl<NotificationFormRawValue['createdAt']>;
  recipientUser: FormControl<NotificationFormRawValue['recipientUser']>;
  senderUser: FormControl<NotificationFormRawValue['senderUser']>;
  relatedPatient: FormControl<NotificationFormRawValue['relatedPatient']>;
};

export type NotificationFormGroup = FormGroup<NotificationFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class NotificationFormService {
  createNotificationFormGroup(notification: NotificationFormGroupInput = { id: null }): NotificationFormGroup {
    const notificationRawValue = this.convertNotificationToNotificationRawValue({
      ...this.getFormDefaults(),
      ...notification,
    });
    return new FormGroup<NotificationFormGroupContent>({
      id: new FormControl(
        { value: notificationRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      type: new FormControl(notificationRawValue.type, {
        validators: [Validators.required],
      }),
      title: new FormControl(notificationRawValue.title, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      message: new FormControl(notificationRawValue.message, {
        validators: [Validators.required],
      }),
      isRead: new FormControl(notificationRawValue.isRead, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(notificationRawValue.createdAt, {
        validators: [Validators.required],
      }),
      recipientUser: new FormControl(notificationRawValue.recipientUser, {
        validators: [Validators.required],
      }),
      senderUser: new FormControl(notificationRawValue.senderUser),
      relatedPatient: new FormControl(notificationRawValue.relatedPatient),
    });
  }

  getNotification(form: NotificationFormGroup): INotification | NewNotification {
    return this.convertNotificationRawValueToNotification(form.getRawValue() as NotificationFormRawValue | NewNotificationFormRawValue);
  }

  resetForm(form: NotificationFormGroup, notification: NotificationFormGroupInput): void {
    const notificationRawValue = this.convertNotificationToNotificationRawValue({ ...this.getFormDefaults(), ...notification });
    form.reset(
      {
        ...notificationRawValue,
        id: { value: notificationRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): NotificationFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      isRead: false,
      createdAt: currentTime,
    };
  }

  private convertNotificationRawValueToNotification(
    rawNotification: NotificationFormRawValue | NewNotificationFormRawValue,
  ): INotification | NewNotification {
    return {
      ...rawNotification,
      createdAt: dayjs(rawNotification.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertNotificationToNotificationRawValue(
    notification: INotification | (Partial<NewNotification> & NotificationFormDefaults),
  ): NotificationFormRawValue | PartialWithRequiredKeyOf<NewNotificationFormRawValue> {
    return {
      ...notification,
      createdAt: notification.createdAt ? notification.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
