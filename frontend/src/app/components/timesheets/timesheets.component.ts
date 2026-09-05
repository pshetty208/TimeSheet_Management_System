import { Component, OnInit } from '@angular/core';
import { TimeSheetService } from '../../services/timesheet.service';
import { TimeSheet } from '../../models';
import { AuthService } from '../../services/auth.service';
import { TimeEntryService } from '../../services/time-entry.service';
import { LanguageService } from '../../services/language.service';

@Component({
  selector: 'app-timesheets',
  templateUrl: './timesheets.component.html',
  styleUrls: ['./timesheets.component.css']
})
export class TimeSheetsComponent implements OnInit {
  timesheets: TimeSheet[] = [];
  loading = false;
  error = '';
  selected: TimeSheet | null = null;
  editingEntryId: number | null = null;
  entry = { date: '', startTime: '', endTime: '', description: '', reportType: 'WORK' };

  constructor(
    private timeSheetService: TimeSheetService,
    public authService: AuthService,
    private timeEntryService: TimeEntryService,
    public language: LanguageService
  ) {}

  ngOnInit() {
    this.loadTimeSheets();
  }

  loadTimeSheets() {
    this.loading = true;
    this.timeSheetService.getAll().subscribe(
      data => {
        this.timesheets = data;
        this.loading = false;
      },
      error => {
        this.error = error.error?.message || 'Failed to load timesheets';
        this.loading = false;
      }
    );
  }

  canSign(ts: TimeSheet): boolean {
    if (ts.status === 'IN_PROGRESS') {
      return this.authService.hasRole('EMPLOYEE');
    }
    if (ts.status === 'SIGNED_BY_EMPLOYEE') {
      return this.authService.hasRole('SUPERVISOR') || this.authService.hasRole('ADMINISTRATOR');
    }
    return false;
  }

  formatStatus(status: string): string {
    if (this.language.current === 'de') {
      return ({ IN_PROGRESS: 'IN BEARBEITUNG', SIGNED_BY_EMPLOYEE: 'VON BESCHÄFTIGTEN UNTERSCHRIEBEN',
        SIGNED_BY_SUPERVISOR: 'VON VORGESETZTEN UNTERSCHRIEBEN', ARCHIVED: 'ARCHIVIERT' } as any)[status] || status;
    }
    return status.replace(/_/g, ' ');
  }
  formatReportType(type: string): string {
    if (this.language.current === 'de') return ({ WORK: 'ARBEIT', VACATION: 'URLAUB', SICK_LEAVE: 'KRANKHEIT' } as any)[type] || type;
    return type.replace('_', ' ');
  }
  reported(ts: TimeSheet): number { return ts.entries.reduce((sum, entry) => sum + entry.hours, 0); }
  progress(ts: TimeSheet): number { return ts.hoursDue ? Math.min(100, (this.reported(ts) / ts.hoursDue) * 100) : 0; }
  get openCount(): number { return this.timesheets.filter(ts => ts.status === 'IN_PROGRESS').length; }
  get pendingCount(): number { return this.timesheets.filter(ts => ts.status === 'SIGNED_BY_EMPLOYEE').length; }
  get totalReported(): number { return this.timesheets.reduce((sum, ts) => sum + this.reported(ts), 0); }

  signByEmployee(id: number) {
    this.timeSheetService.signByEmployee(id).subscribe(
      () => this.loadTimeSheets(),
      error => this.error = error.error?.message || 'Failed to sign'
    );
  }

  signBySupervisor(id: number) {
    this.timeSheetService.signBySupervisor(id).subscribe(
      () => this.loadTimeSheets(),
      error => this.error = error.error?.message || 'Failed to sign'
    );
  }

  archive(id: number) {
    this.timeSheetService.archive(id).subscribe(
      () => this.loadTimeSheets(),
      error => this.error = error.error?.message || 'Failed to archive'
    );
  }

  select(ts: TimeSheet) { this.selected = ts; this.entry.date = ts.periodStart; }
  addEntry() {
    if (!this.selected) return;
    const value: any = { ...this.entry, timesheetId: this.selected.id };
    if (value.reportType !== 'WORK') { delete value.startTime; delete value.endTime; }
    const request = this.editingEntryId === null
      ? this.timeEntryService.create(value)
      : this.timeEntryService.update(this.editingEntryId, value);
    request.subscribe(() => {
      this.editingEntryId = null;
      this.entry = { date: this.selected?.periodStart || '', startTime: '', endTime: '', description: '', reportType: 'WORK' };
      this.loadTimeSheets();
    }, e => this.error = e.error?.message || 'Failed to save entry');
  }
  editEntry(item: any) {
    this.editingEntryId = item.id;
    this.entry = { date: item.date, startTime: item.startTime || '', endTime: item.endTime || '',
      description: item.description || '', reportType: item.reportType };
  }
  deleteEntry(id: number) { this.timeEntryService.delete(id).subscribe(() => this.loadTimeSheets()); }
  revoke(id: number) { this.timeSheetService.revoke(id).subscribe(() => this.loadTimeSheets()); }
  requestChanges(id: number) { this.timeSheetService.requestChanges(id).subscribe(() => this.loadTimeSheets()); }
  canPrint(ts: TimeSheet): boolean {
    const user = this.authService.getCurrentUser();
    return !!user && (this.authService.hasRole('ADMINISTRATOR')
      || (ts.contract.secretaries || []).some(secretary => secretary.username === user.username));
  }
  print(ts: TimeSheet) {
    this.timeSheetService.getPrintable(ts.id).subscribe(html => {
      const blob = new Blob([html], { type: 'text/html' });
      window.open(URL.createObjectURL(blob), '_blank');
    }, error => this.error = error.error?.message || 'Could not prepare printout');
  }
}
