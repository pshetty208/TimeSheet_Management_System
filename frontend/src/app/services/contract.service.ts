import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Contract, ContractStatistics, ContractTerminationPreview } from '../models';

@Injectable({ providedIn: 'root' })
export class ContractService {
  private apiUrl = `${environment.apiUrl}/contracts`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Contract[]> {
    return this.http.get<Contract[]>(this.apiUrl);
  }

  getById(id: number): Observable<Contract> {
    return this.http.get<Contract>(`${this.apiUrl}/${id}`);
  }

  create(contract: Contract): Observable<Contract> {
    return this.http.post<Contract>(this.apiUrl, contract);
  }

  update(id: number, contract: Contract): Observable<Contract> {
    return this.http.put<Contract>(`${this.apiUrl}/${id}`, contract);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  start(id: number): Observable<Contract> {
    return this.http.post<Contract>(`${this.apiUrl}/${id}/start`, {});
  }

  terminate(id: number): Observable<Contract> {
    return this.http.post<Contract>(`${this.apiUrl}/${id}/terminate`, {});
  }

  getStatistics(id: number): Observable<ContractStatistics> {
    return this.http.get<ContractStatistics>(`${this.apiUrl}/${id}/statistics`);
  }

  getTerminationPreview(id: number): Observable<ContractTerminationPreview> {
    return this.http.get<ContractTerminationPreview>(`${this.apiUrl}/${id}/termination-preview`);
  }

  getPrintable(id: number): Observable<string> {
    return this.http.get(`${environment.apiUrl}/print/contracts/${id}`, { responseType: 'text' });
  }
}
