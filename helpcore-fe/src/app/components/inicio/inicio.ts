import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-inicio',
  standalone: false,
  templateUrl: './inicio.html',
  styleUrl: './inicio.css'
})
export class Inicio {

  constructor(
    private router: Router,
    private authService: AuthService,
  ) {}

  createTicket(type: 'logged' | 'guest') {
    console.log(`Crear ticket como: ${type}`);
    if (type === 'logged') {
      this.router.navigate(['/login']);
    } else {
      alert('Funcionalidad de invitado no implementada aún');
    }
  }

  consultarEstado() {
    console.log('Consultar estado de ticket');
    alert('Funcionalidad de consulta no implementada aún');
  }

  testLogout() {
    console.log('Probando logout desde inicio...');
    this.authService.logout().subscribe({
      next: (response) => {
        console.log('Logout desde inicio exitoso:', response);
        alert('Logout exitoso - revisa las cookies en DevTools');
      },
      error: (error) => {
        console.error('Error en logout desde inicio:', error);
        alert('Error en logout - revisa la consola');
      }
    });
  }
}