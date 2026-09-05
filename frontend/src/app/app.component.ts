import { Component, OnInit } from '@angular/core';
import { AuthService } from './services/auth.service';
import { LanguageService } from './services/language.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'TimeSheet Management System';
  constructor(public auth: AuthService, public language: LanguageService, private router: Router) {}
  ngOnInit() { this.language.loadPreference(); }
  logout() { this.auth.logout(); this.router.navigate(['/login']); }
}
