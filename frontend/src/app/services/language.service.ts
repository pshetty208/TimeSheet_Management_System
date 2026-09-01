import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private languageSubject = new BehaviorSubject<string>(localStorage.getItem('language') || 'en');
  language$ = this.languageSubject.asObservable();
  private messages: any = {
    en: { app: 'TimeSheet Management System', contracts: 'Contracts', timesheets: 'Time sheets', logout: 'Logout', language: 'Deutsch' },
    de: { app: 'Arbeitszeiterfassung', contracts: 'Verträge', timesheets: 'Stundenzettel', logout: 'Abmelden', language: 'English' }
  };
  get current() { return this.languageSubject.value; }
  toggle() { const next = this.current === 'en' ? 'de' : 'en'; localStorage.setItem('language', next); this.languageSubject.next(next); }
  t(key: string): string { return this.messages[this.current][key] || key; }
}
