import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TimeSheet } from '../models';

@Injectable({ providedIn: 'root' })
export class TimeSheetService {
  private apiUrl = `${environment.apiUrl}/timesheets`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<TimeSheet[]> {
    return this.http.get<TimeSheet[]>(this.apiUrl);
  }

  getById(id: number): Observable<TimeSheet> {
    return this.http.get<TimeSheet>(`${this.apiUrl}/${id}`);
  }

  create(timesheet: TimeSheet): Observable<TimeSheet> {
    return this.http.post<TimeSheet>(this.apiUrl, timesheet);
  }

  update(id: number, timesheet: TimeSheet): Observable<TimeSheet> {
    return this.http.put<TimeSheet>(`${this.apiUrl}/${id}`, timesheet);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  signByEmployee(id: number): Observable<TimeSheet> {
    return this.http.post<TimeSheet>(`${this.apiUrl}/${id}/sign-employee`, {});
  }

  signBySupervisor(id: number): Observable<TimeSheet> {
    return this.http.post<TimeSheet>(`${this.apiUrl}/${id}/sign-supervisor`, {});
  }

  archive(id: number): Observable<TimeSheet> {
    return this.http.post<TimeSheet>(`${this.apiUrl}/${id}/archive`, {});
  }
}
