import { Component, OnInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { Contract } from '../../models';

@Component({
  selector: 'app-contracts',
  templateUrl: './contracts.component.html',
  styleUrls: ['./contracts.component.css']
})
export class ContractsComponent implements OnInit {
  contracts: Contract[] = [];
  loading = false;
  error = '';

  constructor(private contractService: ContractService) {}

  ngOnInit() {
    this.loadContracts();
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
}
