import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { IPatient } from 'app/entities/patient/patient.model';
import { NotificationType } from 'app/entities/enumerations/notification-type.model';

export interface INotification {
  id: number;
  type?: keyof typeof NotificationType | null;
  title?: string | null;
  message?: string | null;
  isRead?: boolean | null;
  createdAt?: dayjs.Dayjs | null;
  recipientUser?: Pick<IUser, 'id' | 'login'> | null;
  senderUser?: Pick<IUser, 'id' | 'login'> | null;
  relatedPatient?: Pick<IPatient, 'id'> | null;
}

export type NewNotification = Omit<INotification, 'id'> & { id: null };
