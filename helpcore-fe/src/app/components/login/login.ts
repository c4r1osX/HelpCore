import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
 loginForm: FormGroup;
  mensaje: string | null = null;
  isLoading = false;

  constructor(
    private formBuilder: FormBuilder, 
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      nombreUsuario: ['', [Validators.required, Validators.minLength(3)]],
      contrasena: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  login() {
    this.mensaje = null;
    
    if (this.loginForm.valid) {
      this.isLoading = true;
      
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          this.isLoading = false;
          
          console.log('Login response:', response);
          console.log('Cookies después del login:', document.cookie);
          
          this.mensaje = "Login exitoso - Redirigiendo...";
          
          setTimeout(() => {
            this.router.navigate(['/inicio']);
          }, 1000);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Error en login:', error);
          
          if (error.status === 401) {
            this.mensaje = "Usuario o contraseña inválidos";
          } else if (error.status === 0) {
            this.mensaje = "Error de conexión. Verifica que el servidor esté corriendo.";
          } else {
            this.mensaje = "Ha ocurrido un error inesperado.";
          }
        }
      });
    } else {
      this.mensaje = "Por favor completa todos los campos correctamente.";
    }
  }
}