import { Component, OnInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { Contract } from '../../models';
import { User } from '../../models';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-contracts',
  templateUrl: './contracts.component.html',
  styleUrls: ['./contracts.component.css']
})
export class ContractsComponent implements OnInit {
  contracts: Contract[] = [];
  loading = false;
  error = '';
  users: User[] = [];
  draft: any = { name:'', employeeId:null, supervisorId:null, workingHoursPerWeek:10, workingDaysPerWeek:5,
    vacationDaysPerYear:20, archiveDurationMonths:24, frequency:'MONTHLY', startDate:'', endDate:'' };

  constructor(private contractService: ContractService, private userService: UserService, public auth: AuthService) {}

  ngOnInit() {
    this.loadContracts();
    if (this.auth.hasAnyRole(['SUPERVISOR','ASSISTANT','ADMINISTRATOR'])) this.userService.getAll().subscribe(u => this.users = u);
  }

  loadContracts() {
    this.loading = true;
    this.contractService.getAll().subscribe(
      data => {
        this.contracts = data;
        this.loading = false;
      },
      error => {
        this.error = error.error?.message || 'Failed to load contracts';
        this.loading = false;
      }
    );
  }

  deleteContract(id: number) {
    if (confirm('Are you sure?')) {
      this.contractService.delete(id).subscribe(
        () => {
          this.loadContracts();
        },
        error => {
          this.error = error.error?.message || 'Failed to delete contract';
        }
      );
    }
  }

  startContract(id: number) {
    this.contractService.start(id).subscribe(
      () => {
        this.loadContracts();
      },
      error => {
        this.error = error.error?.message || 'Failed to start contract';
      }
    );
  }
  createContract() {
    const employee = this.users.find(u => u.id === Number(this.draft.employeeId));
    const supervisor = this.users.find(u => u.id === Number(this.draft.supervisorId));
    if (!employee || !supervisor) { this.error = 'Select an employee and supervisor'; return; }
    this.contractService.create({ ...this.draft, employee, supervisor, assistants:[], secretaries:[] } as any)
      .subscribe(() => this.loadContracts(), e => this.error = e.error?.message || 'Failed to create contract');
  }
}
