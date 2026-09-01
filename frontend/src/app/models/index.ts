export interface User {
  id: number;
  username: string;
  roles: string[];
  firstName?: string;
  lastName?: string;
  emailAddress?: string;
  consent?: boolean;
  universityStaff?: boolean;
  preferredLanguage?: string;
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
  name?: string;
  workingDaysPerWeek: number;
  vacationDaysPerYear: number;
  archiveDurationMonths: number;
  terminationDate?: string;
}

export interface TimeSheet {
  id: number;
  contract: Contract;
  periodStart: string;
  periodEnd: string;
  status: string;
  entries: TimeEntry[];
  hoursDue: number;
  signedByEmployee?: string;
  signedBySupervisor?: string;
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
  timesheetId: number;
  date: string;
  startTime?: string;
  endTime?: string;
  description?: string;
  reportType: string;
}
