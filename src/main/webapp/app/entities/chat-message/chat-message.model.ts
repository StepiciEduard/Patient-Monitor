import dayjs from 'dayjs/esm';
import { IPatient } from 'app/entities/patient/patient.model';
import { ChatRole } from 'app/entities/enumerations/chat-role.model';

export interface IChatMessage {
  id: number;
  role?: keyof typeof ChatRole | null;
  content?: string | null;
  createdAt?: dayjs.Dayjs | null;
  contextStartDate?: dayjs.Dayjs | null;
  contextEndDate?: dayjs.Dayjs | null;
  patient?: Pick<IPatient, 'id'> | null;
}

export type NewChatMessage = Omit<IChatMessage, 'id'> & { id: null };
