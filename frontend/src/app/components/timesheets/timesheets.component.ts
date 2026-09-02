import { Component, OnInit } from '@angular/core';
import { TimeSheetService } from '../../services/timesheet.service';
import { TimeSheet } from '../../models';
import { AuthService } from '../../services/auth.service';
import { TimeEntryService } from '../../services/time-entry.service';

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
  entry = { date: '', startTime: '', endTime: '', description: '', reportType: 'WORK' };

  constructor(
    private timeSheetService: TimeSheetService,
    public authService: AuthService,
    private timeEntryService: TimeEntryService
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
    return status.replace(/_/g, ' ');
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
    this.timeEntryService.create(value).subscribe(() => this.loadTimeSheets(), e => this.error = e.error?.message || 'Failed to add entry');
  }
  deleteEntry(id: number) { this.timeEntryService.delete(id).subscribe(() => this.loadTimeSheets()); }
  revoke(id: number) { this.timeSheetService.revoke(id).subscribe(() => this.loadTimeSheets()); }
  requestChanges(id: number) { this.timeSheetService.requestChanges(id).subscribe(() => this.loadTimeSheets()); }
}
