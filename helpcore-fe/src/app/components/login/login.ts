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

  constructor(private formBuilder: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.formBuilder.group({
      nombreUsuario: ['', Validators.required],
      contrasena: ['', Validators.required]
    });
  }

  login(){
    this.mensaje = null;
    if(this.loginForm.valid){
      this.authService.login(this.loginForm.value).subscribe({
        next: (login) =>{
          localStorage.setItem('token', login.access_token);
          this.router.navigate(['/inicio'])
        },
        error: (error) => {
          if (error.status === 401) {
            this.mensaje = "Usuario o contraseña inválidos";
            return;
          } else {
            this.mensaje = "Ha ocurrido un error inesperado.";
            return;
          }
        }
      });
    }
  }
}
