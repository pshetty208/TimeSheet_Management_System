import { Component } from '@angular/core';
import { AuthService } from './services/auth.service';
import { LanguageService } from './services/language.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'TimeSheet Management System';
  constructor(public auth: AuthService, public language: LanguageService) {}
}
