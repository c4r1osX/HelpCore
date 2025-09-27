import { Component, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App {
   protected readonly title = signal('helpcore-fe');
  showNavBar = true;

  constructor(private router: Router) {
    // Detectar cambios de ruta
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      // Ocultar navbar en /login
      this.showNavBar = !(event.url === '/login');
    });
  }
}
