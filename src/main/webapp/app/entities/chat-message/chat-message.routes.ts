import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ChatMessageResolve from './route/chat-message-routing-resolve.service';

const chatMessageRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/chat-message.component').then(m => m.ChatMessageComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/chat-message-detail.component').then(m => m.ChatMessageDetailComponent),
    resolve: {
      chatMessage: ChatMessageResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/chat-message-update.component').then(m => m.ChatMessageUpdateComponent),
    resolve: {
      chatMessage: ChatMessageResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/chat-message-update.component').then(m => m.ChatMessageUpdateComponent),
    resolve: {
      chatMessage: ChatMessageResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default chatMessageRoute;
