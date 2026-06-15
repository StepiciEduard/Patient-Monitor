import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IChatMessage, NewChatMessage } from '../chat-message.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IChatMessage for edit and NewChatMessageFormGroupInput for create.
 */
type ChatMessageFormGroupInput = IChatMessage | PartialWithRequiredKeyOf<NewChatMessage>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IChatMessage | NewChatMessage> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type ChatMessageFormRawValue = FormValueOf<IChatMessage>;

type NewChatMessageFormRawValue = FormValueOf<NewChatMessage>;

type ChatMessageFormDefaults = Pick<NewChatMessage, 'id' | 'createdAt'>;

type ChatMessageFormGroupContent = {
  id: FormControl<ChatMessageFormRawValue['id'] | NewChatMessage['id']>;
  role: FormControl<ChatMessageFormRawValue['role']>;
  content: FormControl<ChatMessageFormRawValue['content']>;
  createdAt: FormControl<ChatMessageFormRawValue['createdAt']>;
  contextStartDate: FormControl<ChatMessageFormRawValue['contextStartDate']>;
  contextEndDate: FormControl<ChatMessageFormRawValue['contextEndDate']>;
  patient: FormControl<ChatMessageFormRawValue['patient']>;
};

export type ChatMessageFormGroup = FormGroup<ChatMessageFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ChatMessageFormService {
  createChatMessageFormGroup(chatMessage: ChatMessageFormGroupInput = { id: null }): ChatMessageFormGroup {
    const chatMessageRawValue = this.convertChatMessageToChatMessageRawValue({
      ...this.getFormDefaults(),
      ...chatMessage,
    });
    return new FormGroup<ChatMessageFormGroupContent>({
      id: new FormControl(
        { value: chatMessageRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      role: new FormControl(chatMessageRawValue.role, {
        validators: [Validators.required],
      }),
      content: new FormControl(chatMessageRawValue.content, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(chatMessageRawValue.createdAt, {
        validators: [Validators.required],
      }),
      contextStartDate: new FormControl(chatMessageRawValue.contextStartDate),
      contextEndDate: new FormControl(chatMessageRawValue.contextEndDate),
      patient: new FormControl(chatMessageRawValue.patient, {
        validators: [Validators.required],
      }),
    });
  }

  getChatMessage(form: ChatMessageFormGroup): IChatMessage | NewChatMessage {
    return this.convertChatMessageRawValueToChatMessage(form.getRawValue() as ChatMessageFormRawValue | NewChatMessageFormRawValue);
  }

  resetForm(form: ChatMessageFormGroup, chatMessage: ChatMessageFormGroupInput): void {
    const chatMessageRawValue = this.convertChatMessageToChatMessageRawValue({ ...this.getFormDefaults(), ...chatMessage });
    form.reset(
      {
        ...chatMessageRawValue,
        id: { value: chatMessageRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ChatMessageFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
    };
  }

  private convertChatMessageRawValueToChatMessage(
    rawChatMessage: ChatMessageFormRawValue | NewChatMessageFormRawValue,
  ): IChatMessage | NewChatMessage {
    return {
      ...rawChatMessage,
      createdAt: dayjs(rawChatMessage.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertChatMessageToChatMessageRawValue(
    chatMessage: IChatMessage | (Partial<NewChatMessage> & ChatMessageFormDefaults),
  ): ChatMessageFormRawValue | PartialWithRequiredKeyOf<NewChatMessageFormRawValue> {
    return {
      ...chatMessage,
      createdAt: chatMessage.createdAt ? chatMessage.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
