import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ContractsComponent } from './components/contracts/contracts.component';
import { TimeSheetsComponent } from './components/timesheets/timesheets.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
  },
  {
    path: 'contracts',
    component: ContractsComponent,
    data: { roles: ['SUPERVISOR', 'ASSISTANT', 'ADMINISTRATOR'] }
  },
  {
    path: 'timesheets',
    component: TimeSheetsComponent,
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
