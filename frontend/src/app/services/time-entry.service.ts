import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TimeEntry, TimeEntryRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class TimeEntryService {
  private apiUrl = `${environment.apiUrl}/time-entries`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<TimeEntry[]> {
    return this.http.get<TimeEntry[]>(this.apiUrl);
  }

  getById(id: number): Observable<TimeEntry> {
    return this.http.get<TimeEntry>(`${this.apiUrl}/${id}`);
  }

  create(entry: TimeEntryRequest): Observable<TimeEntry> {
    return this.http.post<TimeEntry>(this.apiUrl, entry);
  }

  update(id: number, entry: TimeEntryRequest): Observable<TimeEntry> {
    return this.http.put<TimeEntry>(`${this.apiUrl}/${id}`, entry);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
