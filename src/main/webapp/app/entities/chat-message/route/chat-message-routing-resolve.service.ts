import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IChatMessage } from '../chat-message.model';
import { ChatMessageService } from '../service/chat-message.service';

const chatMessageResolve = (route: ActivatedRouteSnapshot): Observable<null | IChatMessage> => {
  const id = route.params.id;
  if (id) {
    return inject(ChatMessageService)
      .find(id)
      .pipe(
        mergeMap((chatMessage: HttpResponse<IChatMessage>) => {
          if (chatMessage.body) {
            return of(chatMessage.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default chatMessageResolve;
