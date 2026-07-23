export interface User {
  id: number;
  username: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  roles: string[];
}

export interface Contract {
  id: number;
  employee: User;
  supervisor: User;
  workingHoursPerWeek: number;
  startDate: string;
  endDate?: string;
  frequency: string;
  vacationEntitlement: number;
  status: string;
}

export interface TimeSheet {
  id: number;
  contract: Contract;
  periodStart: string;
  periodEnd: string;
  status: string;
  entries: TimeEntry[];
}

export interface TimeEntry {
  id: number;
  date: string;
  startTime?: string;
  endTime?: string;
  description?: string;
  reportType: string;
  hours: number;
}

export interface TimeEntryRequest {
  date: string;
  startTime?: string;
  endTime?: string;
  description?: string;
  reportType: string;
}
