import { Component } from '@angular/core';

@Component({
  selector: 'app-nav-bar',
  standalone: false,
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css'
})
export class NavBar {
  activeLink: string = 'inicio'; // valor por defecto

  setActive(link: string) {
    this.activeLink = link;
  }

  login() {
    console.log('Iniciar sesión');
  }

  logout() {
    console.log('Cerrar sesión');
  }
}
