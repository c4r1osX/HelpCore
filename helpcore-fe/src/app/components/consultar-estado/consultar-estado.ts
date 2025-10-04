import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-consultar-estado',
  standalone: false,
  templateUrl: './consultar-estado.html',
  styleUrl: './consultar-estado.css'
})
export class ConsultarEstado {
  consultaForm: FormGroup;
  isLoading = false;

  constructor(private fb: FormBuilder) {
    this.consultaForm = this.fb.group({
      ticketId: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.consultaForm.valid) {
      this.isLoading = true;
      console.log('Consultando ticket:', this.consultaForm.value);
    } else {
      this.consultaForm.markAllAsTouched();
      alert('Por favor, complete todos los campos correctamente');
    }
  }

  onReset(): void {
    this.consultaForm.reset();
  }

  onCancel(): void {
    console.log('Acción de cancelar');
  }
}