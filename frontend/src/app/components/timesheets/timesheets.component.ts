import { Component, OnInit } from '@angular/core';
import { TimeSheetService } from '../../services/timesheet.service';
import { TimeSheet } from '../../models';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-timesheets',
  templateUrl: './timesheets.component.html',
  styleUrls: ['./timesheets.component.css']
})
export class TimeSheetsComponent implements OnInit {
  timesheets: TimeSheet[] = [];
  loading = false;
  error = '';

  constructor(
    private timeSheetService: TimeSheetService,
    private authService: AuthService
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
}
