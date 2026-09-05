import { Component, OnInit } from '@angular/core';
import { ContractService } from '../../services/contract.service';
import { Contract, ContractStatistics, User } from '../../models';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { LanguageService } from '../../services/language.service';

@Component({ selector: 'app-contracts', templateUrl: './contracts.component.html', styleUrls: ['./contracts.component.css'] })
export class ContractsComponent implements OnInit {
  contracts: Contract[] = []; loading = false; saving = false; error = ''; users: User[] = [];
  editingId: number | null = null; expandedId: number | null = null;
  statistics: {[id: number]: ContractStatistics} = {};
  readonly states = ['BW','BY','BE','BB','HB','HH','HE','MV','NI','NW','RP','SL','SN','ST','SH','TH'];
  draft: any = this.emptyDraft();

  constructor(private contractService: ContractService, private userService: UserService, public auth: AuthService, public language: LanguageService) {}
  ngOnInit() { this.loadContracts(); if (this.isManager()) this.userService.getAll().subscribe(u => this.users = u); }

  emptyDraft() { return { name:'', employeeId:null, supervisorId:null, assistantIds:[], secretaryIds:[], workingHoursPerWeek:10,
    workingDaysPerWeek:5, vacationDaysPerYear:20, archiveDurationMonths:24, federalState:'RP', frequency:'MONTHLY', startDate:'', endDate:'' }; }
  isManager() { return this.auth.hasAnyRole(['SUPERVISOR','ASSISTANT','ADMINISTRATOR']); }
  isSecretary() { return this.auth.hasAnyRole(['SECRETARY','ADMINISTRATOR']); }
  canManage(c: Contract) { const u = this.auth.getCurrentUser(); return !!u && (this.auth.hasRole('ADMINISTRATOR') || c.supervisor.username === u.username || (c.assistants || []).some(a => a.username === u.username)); }
  canPrint(c: Contract) { const u = this.auth.getCurrentUser(); return !!u && (this.auth.hasRole('ADMINISTRATOR') || (c.secretaries || []).some(s => s.username === u.username)); }

  loadContracts() { this.loading = true; this.error = ''; this.contractService.getAll().subscribe(data => {
    this.contracts = data; this.loading = false; data.forEach(c => this.contractService.getStatistics(c.id).subscribe(s => this.statistics[c.id] = s));
  }, e => { this.error = e.error?.message || 'Failed to load contracts'; this.loading = false; }); }

  payload() { const find = (id: any) => this.users.find(u => u.id === Number(id)); return { ...this.draft,
    employee: find(this.draft.employeeId), supervisor: find(this.draft.supervisorId),
    assistants: (this.draft.assistantIds || []).map((id: any) => find(id)).filter(Boolean),
    secretaries: (this.draft.secretaryIds || []).map((id: any) => find(id)).filter(Boolean) }; }
  validateDraft(): boolean { const p = this.payload(); if (!p.employee || !p.supervisor) return this.fail('Select an employee and supervisor');
    if (!this.draft.startDate || !this.draft.endDate || Number(this.draft.startDate.slice(8,10)) !== 1) return this.fail('Start date must be the first day of a month');
    const end = new Date(this.draft.endDate + 'T12:00:00'); const last = new Date(end.getFullYear(), end.getMonth()+1, 0).getDate();
    if (end.getDate() !== last) return this.fail('End date must be the last day of a month'); return true; }
  fail(message: string) { this.error = message; return false; }
  saveContract() { if (!this.validateDraft()) return; this.saving = true; const request = this.editingId
    ? this.contractService.update(this.editingId, this.payload() as Contract) : this.contractService.create(this.payload() as Contract);
    request.subscribe(() => { this.saving=false; this.cancelEdit(); this.loadContracts(); }, e => { this.saving=false; this.error=e.error?.message || 'Failed to save contract'; }); }
  edit(c: Contract) { this.editingId = c.id; this.draft = { ...c, employeeId:c.employee.id, supervisorId:c.supervisor.id,
    assistantIds:(c.assistants||[]).map(a=>a.id), secretaryIds:(c.secretaries||[]).map(s=>s.id) }; window.scrollTo({top:0,behavior:'smooth'}); }
  cancelEdit() { this.editingId=null; this.draft=this.emptyDraft(); }
  deleteContract(id: number) { if (confirm('Delete this prepared contract permanently?')) this.contractService.delete(id).subscribe(()=>this.loadContracts(),e=>this.error=e.error?.message||'Failed to delete contract'); }
  startContract(id: number) { this.contractService.start(id).subscribe(()=>this.loadContracts(),e=>this.error=e.error?.message||'Failed to start contract'); }
  terminateContract(c: Contract) { this.contractService.getTerminationPreview(c.id).subscribe(preview => {
    if (!preview.allowed) { this.error=preview.message; return; }
    if (confirm(`${preview.message}. Continue and terminate this contract?`)) this.contractService.terminate(c.id).subscribe(()=>this.loadContracts(),e=>this.error=e.error?.message||'Failed to terminate contract');
  },e=>this.error=e.error?.message||'Could not check termination'); }
  printContract(c: Contract) { this.contractService.getPrintable(c.id).subscribe(html => { const blob=new Blob([html],{type:'text/html'}); window.open(URL.createObjectURL(blob),'_blank'); },e=>this.error=e.error?.message||'Could not prepare printout'); }
  toggleStatistics(c: Contract) { this.expandedId=this.expandedId===c.id?null:c.id; }
  get activeCount() { return this.contracts.filter(c=>c.status==='STARTED').length; }
  get preparedCount() { return this.contracts.filter(c=>c.status==='PREPARED').length; }
  get weeklyHours() { return this.contracts.filter(c=>c.status==='STARTED').reduce((s,c)=>s+Number(c.workingHoursPerWeek||0),0); }
}
