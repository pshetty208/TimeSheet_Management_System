import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { User } from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}
  getAll(): Observable<User[]> { return this.http.get<User[]>(`${environment.apiUrl}/users`); }
  getMe(): Observable<User> { return this.http.get<User>(`${environment.apiUrl}/users/me`); }
  updateLanguage(preferredLanguage: string): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/users/me/preferences`, { preferredLanguage });
  }
}
