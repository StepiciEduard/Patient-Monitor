import { IUser } from 'app/entities/user/user.model';

export interface IDoctor {
  id: number;
  specialization?: string | null;
  phone?: string | null;
  officeLocation?: string | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewDoctor = Omit<IDoctor, 'id'> & { id: null };
