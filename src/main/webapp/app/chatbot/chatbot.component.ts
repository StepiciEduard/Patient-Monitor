import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { AccountService } from 'app/core/auth/account.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  html: string;
  timestamp: Date;
}

@Component({
  selector: 'jhi-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrl: './chatbot.component.scss',
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class ChatbotComponent {
  private http = inject(HttpClient);
  private appConfig = inject(ApplicationConfigService);
  private accountService = inject(AccountService);

  account = this.accountService.trackCurrentAccount();

  isOpen = false;
  isLoading = false;
  userMessage = '';
  messages: ChatMessage[] = [];

  get isVisible(): boolean {
    const acc = this.account();
    if (!acc) return false;
    const authorities = acc.authorities || [];
    return authorities.includes('ROLE_PATIENT') || authorities.includes('ROLE_DOCTOR');
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen && this.messages.length === 0) {
      const welcome =
        'Buna! 👋 Sunt asistentul tau medical AI. Cum te pot ajuta astazi?\n\nPot sa:\n• Explic valorile tale medicale\n• Ofer sfaturi generale de sanatate\n• Explic alertele primite\n• Raspund la intrebari despre starea ta';
      this.messages.push({
        role: 'assistant',
        content: welcome,
        html: this.formatMarkdown(welcome),
        timestamp: new Date(),
      });
    }
  }

  sendMessage(): void {
    if (!this.userMessage.trim() || this.isLoading) return;

    const msg = this.userMessage.trim();
    this.messages.push({
      role: 'user',
      content: msg,
      html: this.escapeHtml(msg),
      timestamp: new Date(),
    });
    this.userMessage = '';
    this.isLoading = true;

    const history = this.messages.slice(-11, -1).map(m => ({ role: m.role, content: m.content }));

    this.http
      .post<{ response: string }>(this.appConfig.getEndpointFor('api/chatbot/message'), {
        message: msg,
        history: history,
      })
      .subscribe({
        next: res => {
          this.messages.push({
            role: 'assistant',
            content: res.response,
            html: this.formatMarkdown(res.response),
            timestamp: new Date(),
          });
          this.isLoading = false;
          this.scrollToBottom();
        },
        error: () => {
          const errMsg = 'Scuze, a aparut o eroare. Incearca din nou.';
          this.messages.push({
            role: 'assistant',
            content: errMsg,
            html: this.formatMarkdown(errMsg),
            timestamp: new Date(),
          });
          this.isLoading = false;
        },
      });

    setTimeout(() => this.scrollToBottom(), 100);
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  clearChat(): void {
    this.messages = [];
    const resetMsg = 'Conversatie resetata. Cu ce te pot ajuta?';
    this.messages.push({
      role: 'assistant',
      content: resetMsg,
      html: this.formatMarkdown(resetMsg),
      timestamp: new Date(),
    });
  }

  /**
   * Convert basic Markdown to HTML for chat display.
   */
  formatMarkdown(text: string): string {
    let html = this.escapeHtml(text);

    // Bold: **text** or __text__
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/__(.+?)__/g, '<strong>$1</strong>');

    // Italic: *text* or _text_
    html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
    html = html.replace(/_(.+?)_/g, '<em>$1</em>');

    // Bullet lists: lines starting with - or •
    html = html.replace(/^[-•]\s+(.+)$/gm, '<li>$1</li>');
    html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>');

    // Numbered lists: lines starting with 1. 2. etc
    html = html.replace(/^\d+\.\s+(.+)$/gm, '<li>$1</li>');

    // Headers: ### text
    html = html.replace(/^###\s+(.+)$/gm, '<strong class="md-h3">$1</strong>');
    html = html.replace(/^##\s+(.+)$/gm, '<strong class="md-h2">$1</strong>');

    // Code inline: `text`
    html = html.replace(/`(.+?)`/g, '<code>$1</code>');

    // Line breaks
    html = html.replace(/\n/g, '<br>');

    // Clean up double <br> inside lists
    html = html.replace(/<\/li><br><li>/g, '</li><li>');
    html = html.replace(/<\/ul><br>/g, '</ul>');
    html = html.replace(/<br><ul>/g, '<ul>');

    return html;
  }

  private escapeHtml(text: string): string {
    return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const chatBody = document.querySelector('.chat-messages');
      if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight;
      }
    }, 50);
  }
}
