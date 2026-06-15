import dayjs from 'dayjs/esm';

import { IChatMessage, NewChatMessage } from './chat-message.model';

export const sampleWithRequiredData: IChatMessage = {
  id: 1166,
  role: 'USER',
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2026-02-19T19:52'),
};

export const sampleWithPartialData: IChatMessage = {
  id: 3363,
  role: 'USER',
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2026-02-19T22:43'),
  contextStartDate: dayjs('2026-02-20'),
};

export const sampleWithFullData: IChatMessage = {
  id: 20495,
  role: 'ASSISTANT',
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2026-02-20T01:16'),
  contextStartDate: dayjs('2026-02-20'),
  contextEndDate: dayjs('2026-02-20'),
};

export const sampleWithNewData: NewChatMessage = {
  role: 'USER',
  content: '../fake-data/blob/hipster.txt',
  createdAt: dayjs('2026-02-19T16:04'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
