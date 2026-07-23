import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse, User } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;
  private tokenSubject: BehaviorSubject<string | null>;
  public token$: Observable<string | null>;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User | null>(
      this.getUserFromStorage()
    );
    this.currentUser$ = this.currentUserSubject.asObservable();

    this.tokenSubject = new BehaviorSubject<string | null>(
      this.getTokenFromStorage()
    );
    this.token$ = this.tokenSubject.asObservable();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          const user: User = {
            id: 0,
            username: response.username,
            roles: response.roles
          };
          localStorage.setItem('user', JSON.stringify(user));
          this.tokenSubject.next(response.token);
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.tokenSubject.next(null);
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.getTokenFromStorage();
  }

  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.roles.includes(role) : false;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserSubject.value;
    return user ? roles.some(r => user.roles.includes(r)) : false;
  }

  getToken(): string | null {
    return this.getTokenFromStorage();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private getTokenFromStorage(): string | null {
    return localStorage.getItem('token');
  }

  private getUserFromStorage(): User | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }
}
