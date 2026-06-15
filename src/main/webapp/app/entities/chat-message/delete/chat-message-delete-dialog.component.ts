import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IChatMessage } from '../chat-message.model';
import { ChatMessageService } from '../service/chat-message.service';

@Component({
  templateUrl: './chat-message-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class ChatMessageDeleteDialogComponent {
  chatMessage?: IChatMessage;

  protected chatMessageService = inject(ChatMessageService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.chatMessageService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
