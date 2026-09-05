import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { UserService } from './user.service';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private languageSubject = new BehaviorSubject<string>(localStorage.getItem('language') || 'en');
  language$ = this.languageSubject.asObservable();
  private messages: any = {
    en: { app: 'TimeSheet Management System', contracts: 'Contracts', timesheets: 'Time sheets', logout: 'Logout', language: 'Deutsch',
      timeTracking: 'Time tracking', timesheetIntro: 'Review working periods, report time and keep approvals moving.',
      totalPeriods: 'Total periods', inProgress: 'In progress', hoursReported: 'Hours reported', awaitingApproval: 'Awaiting approval',
      allPeriods: 'All periods', reportWork: 'Report work', sign: 'Sign', approve: 'Approve', requestChanges: 'Request changes',
      archive: 'Archive', print: 'Print', edit: 'Edit', delete: 'Delete', newEntry: 'New entry', saveEntry: 'Save entry', updateEntry: 'Update entry',
      contractIntro: 'Create, review and manage the complete contract lifecycle.', activeContracts: 'Active contracts', prepared: 'Prepared',
      weeklyCapacity: 'Weekly capacity', newAgreement: 'New agreement', prepareContract: 'Prepare a contract', statistics: 'Statistics', start: 'Start', terminate: 'Terminate',
      universityWorkforce: 'University workforce management', loginTitle: 'Every hour accounted for. None of the friction.',
      loginIntro: 'Manage contracts, submit work and approve timesheets in one clear workspace.', holidayAware: 'Holiday-aware', secureApprovals: 'Secure approvals',
      welcomeBack: 'Welcome back', signInWorkspace: 'Sign in to your workspace', credentials: 'Use your university credentials to continue.',
      username: 'Username', password: 'Password', enterUsername: 'Enter your username', enterPassword: 'Enter your password',
      usernameRequired: 'Username is required', usernameLength: 'Username must be at least 3 characters', passwordRequired: 'Password is required',
      passwordLength: 'Password must be at least 6 characters', signingIn: 'Signing in...', signIn: 'Sign in',
      privacy: 'Your account data is protected and only visible to authorised participants.', workSimple: 'Work time, made simple',
      workAgreements: 'Work agreements', totalRecords: 'total records', generatingTimesheets: 'Generating timesheets', readyStart: 'Ready to be started',
      acrossActive: 'Across active contracts', preparedContract: 'Prepared contract', editContract: 'Edit contract', contractDates: 'Contract dates must cover complete calendar months. Calculated balances are available after saving.',
      cancelEditing: 'Cancel editing', contractName: 'Contract name', employee: 'Employee', chooseEmployee: 'Choose employee', supervisor: 'Supervisor',
      chooseSupervisor: 'Choose supervisor', assistants: 'Assistants', secretaries: 'Secretaries', startDate: 'Start date', firstMonth: 'first of month',
      endDate: 'End date', lastMonth: 'last of month', frequency: 'Frequency', federalState: 'Federal state', hoursWeek: 'Hours per week',
      workingDaysWeek: 'Working days/week', vacationDaysYear: 'Vacation days/year', archiveMonths: 'Archive retention (months)', saving: 'Saving...',
      saveChanges: 'Save changes', createPrepared: 'Create prepared contract', loadingContracts: 'Loading contracts...', contractRegister: 'Contract register',
      registerIntro: 'Participant access, balances and lifecycle status', participants: 'Participants', schedule: 'Schedule', workingTime: 'Working time',
      status: 'Status', actions: 'Actions', totalHoursDue: 'Total hours due', remainingBalance: 'Remaining balance', vacationEntitlement: 'Vacation entitlement',
      vacationReported: 'Vacation reported', archiveRetention: 'Archive retention', loadingStatistics: 'Loading statistics...', noContracts: 'No contracts found',
      createFirstContract: 'Create the first agreement above to get started.', loadingTimesheets: 'Loading timesheets...', periodsWorkspace: 'timesheets in your workspace',
      currentView: 'Current view', period: 'Period', progress: 'Progress', revoke: 'Revoke', complete: 'Complete', noTimesheets: 'No timesheets yet',
      timesheetsAutomatic: 'Timesheets appear automatically when a contract begins.', entryType: 'Entry type', date: 'Date', startTime: 'Start time',
      endTime: 'End time', description: 'Description', reportedEntries: 'Reported entries', menu: 'Menu', timeEntries: 'Time entries',
      welcome: 'Welcome', roles: 'Your roles', selectMenu: 'Select a menu item to get started.', quickStats: 'Quick statistics',
      reference: 'Reference', to: 'to', hours: 'hours', daysWeek: 'days/week', terminated: 'Terminated', timesheet: 'Timesheet',
      weekly: 'Weekly', monthly: 'Monthly', work: 'Work', vacation: 'Vacation', sickLeave: 'Sick leave' },
    de: { app: 'Arbeitszeiterfassung', contracts: 'Verträge', timesheets: 'Stundenzettel', logout: 'Abmelden', language: 'English',
      timeTracking: 'Zeiterfassung', timesheetIntro: 'Arbeitszeiträume prüfen, Zeiten erfassen und Freigaben bearbeiten.',
      totalPeriods: 'Zeiträume gesamt', inProgress: 'In Bearbeitung', hoursReported: 'Erfasste Stunden', awaitingApproval: 'Wartet auf Freigabe',
      allPeriods: 'Alle Zeiträume', reportWork: 'Arbeit erfassen', sign: 'Unterschreiben', approve: 'Freigeben', requestChanges: 'Änderung anfordern',
      archive: 'Archivieren', print: 'Drucken', edit: 'Bearbeiten', delete: 'Löschen', newEntry: 'Neuer Eintrag', saveEntry: 'Eintrag speichern', updateEntry: 'Eintrag aktualisieren',
      contractIntro: 'Den vollständigen Vertragslebenszyklus erstellen, prüfen und verwalten.', activeContracts: 'Aktive Verträge', prepared: 'Vorbereitet',
      weeklyCapacity: 'Wochenkapazität', newAgreement: 'Neue Vereinbarung', prepareContract: 'Vertrag vorbereiten', statistics: 'Statistik', start: 'Starten', terminate: 'Beenden',
      universityWorkforce: 'Personalverwaltung der Universität', loginTitle: 'Jede Arbeitsstunde erfasst. Ganz ohne Umstände.',
      loginIntro: 'Verträge verwalten, Arbeit erfassen und Stundenzettel übersichtlich freigeben.', holidayAware: 'Feiertage berücksichtigt', secureApprovals: 'Sichere Freigaben',
      welcomeBack: 'Willkommen zurück', signInWorkspace: 'Im Arbeitsbereich anmelden', credentials: 'Mit den Zugangsdaten der Universität fortfahren.',
      username: 'Benutzername', password: 'Passwort', enterUsername: 'Benutzernamen eingeben', enterPassword: 'Passwort eingeben',
      usernameRequired: 'Benutzername ist erforderlich', usernameLength: 'Der Benutzername muss mindestens 3 Zeichen lang sein', passwordRequired: 'Passwort ist erforderlich',
      passwordLength: 'Das Passwort muss mindestens 6 Zeichen lang sein', signingIn: 'Anmeldung läuft...', signIn: 'Anmelden',
      privacy: 'Kontodaten sind geschützt und nur für berechtigte Beteiligte sichtbar.', workSimple: 'Arbeitszeit einfach verwalten',
      workAgreements: 'Arbeitsverträge', totalRecords: 'Einträge gesamt', generatingTimesheets: 'Stundenzettel werden erzeugt', readyStart: 'Bereit zum Start',
      acrossActive: 'Für alle aktiven Verträge', preparedContract: 'Vorbereiteter Vertrag', editContract: 'Vertrag bearbeiten', contractDates: 'Vertragsdaten müssen vollständige Kalendermonate umfassen. Berechnete Salden sind nach dem Speichern verfügbar.',
      cancelEditing: 'Bearbeitung abbrechen', contractName: 'Vertragsname', employee: 'Beschäftigte Person', chooseEmployee: 'Beschäftigte Person auswählen', supervisor: 'Vorgesetzte Person',
      chooseSupervisor: 'Vorgesetzte Person auswählen', assistants: 'Assistenz', secretaries: 'Sekretariat', startDate: 'Startdatum', firstMonth: 'erster Tag des Monats',
      endDate: 'Enddatum', lastMonth: 'letzter Tag des Monats', frequency: 'Intervall', federalState: 'Bundesland', hoursWeek: 'Stunden pro Woche',
      workingDaysWeek: 'Arbeitstage pro Woche', vacationDaysYear: 'Urlaubstage pro Jahr', archiveMonths: 'Archivdauer (Monate)', saving: 'Speichern...',
      saveChanges: 'Änderungen speichern', createPrepared: 'Vorbereiteten Vertrag erstellen', loadingContracts: 'Verträge werden geladen...', contractRegister: 'Vertragsübersicht',
      registerIntro: 'Beteiligte, Salden und Vertragsstatus', participants: 'Beteiligte', schedule: 'Zeitraum', workingTime: 'Arbeitszeit',
      status: 'Status', actions: 'Aktionen', totalHoursDue: 'Sollstunden gesamt', remainingBalance: 'Verbleibender Saldo', vacationEntitlement: 'Urlaubsanspruch',
      vacationReported: 'Erfasster Urlaub', archiveRetention: 'Archivdauer', loadingStatistics: 'Statistik wird geladen...', noContracts: 'Keine Verträge vorhanden',
      createFirstContract: 'Oben kann der erste Vertrag erstellt werden.', loadingTimesheets: 'Stundenzettel werden geladen...', periodsWorkspace: 'Stundenzettel im Arbeitsbereich',
      currentView: 'Aktuelle Ansicht', period: 'Zeitraum', progress: 'Fortschritt', revoke: 'Widerrufen', complete: 'Abgeschlossen', noTimesheets: 'Noch keine Stundenzettel',
      timesheetsAutomatic: 'Stundenzettel erscheinen automatisch beim Vertragsstart.', entryType: 'Eintragsart', date: 'Datum', startTime: 'Startzeit',
      endTime: 'Endzeit', description: 'Beschreibung', reportedEntries: 'Erfasste Einträge', menu: 'Menü', timeEntries: 'Zeiteinträge',
      welcome: 'Willkommen', roles: 'Ihre Rollen', selectMenu: 'Wählen Sie einen Menüpunkt aus.', quickStats: 'Kurzstatistik',
      reference: 'Referenz', to: 'bis', hours: 'Stunden', daysWeek: 'Tage/Woche', terminated: 'Beendet', timesheet: 'Stundenzettel',
      weekly: 'Wöchentlich', monthly: 'Monatlich', work: 'Arbeit', vacation: 'Urlaub', sickLeave: 'Krankheit' }
  };
  constructor(private users: UserService) {}
  get current() { return this.languageSubject.value; }
  toggle() {
    const next = this.current === 'en' ? 'de' : 'en';
    this.set(next);
    if (localStorage.getItem('token')) this.users.updateLanguage(next).subscribe();
  }
  loadPreference() {
    if (localStorage.getItem('token')) this.users.getMe().subscribe(user => this.set(user.preferredLanguage || 'en'));
  }
  private set(language: string) {
    const value = language === 'de' ? 'de' : 'en';
    localStorage.setItem('language', value);
    this.languageSubject.next(value);
  }
  t(key: string): string { return this.messages[this.current][key] || key; }
}
