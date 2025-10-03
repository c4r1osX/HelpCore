import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TicketService } from '../../services/ticket-service';
import { CategoriaTicketService } from '../../services/categoria-ticket-service';

@Component({
  selector: 'app-ticket',
  standalone: false,
  templateUrl: './ticket.html',
  styleUrl: './ticket.css'
})
export class Ticket implements OnInit{
  ticketForm: FormGroup;
  isLoading = false;
  categoriaTickets: any;


  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService,
    private categoriaTicketService: CategoriaTicketService
  ) {
    this.ticketForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]+$/)]],
      lastName: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]+$/)]],
      dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      phone: ['', [Validators.required, Validators.pattern(/^\+51\s\d{3}\s\d{3}\s\d{3}$/)]],
      studentCode: ['', [Validators.required, Validators.pattern(/^A\d{8}$/)]],
      sede: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      helpTopic: ['', Validators.required],
      subject: ['', Validators.required],
      body: ['', Validators.required]
    });
  }

  listarCategoriaTickets(): void {
  this.categoriaTicketService.listarCategoriaTicket().subscribe({
    next: (data) => {
      this.categoriaTickets = data;
    },
    error: (err) => {
      console.error("❌ Error al listar categorías:", err);
    }
  });
}

ngOnInit(): void {
  this.listarCategoriaTickets();
}


  onSubmit(): void {
    if (this.ticketForm.valid) {
      this.isLoading = true;

      const ticketData = {
        nombres: this.ticketForm.get('fullName')?.value,
        apellidos: this.ticketForm.get('lastName')?.value,
        dni: this.ticketForm.get('dni')?.value,
        email: this.ticketForm.get('email')?.value,
        telefono: this.ticketForm.get('phone')?.value,
        codigoAlumno: this.ticketForm.get('studentCode')?.value,
        sede: this.ticketForm.get('sede')?.value,
        temaAyuda: this.ticketForm.get('helpTopic')?.value,
        asunto: this.ticketForm.get('subject')?.value,
        comentarios: this.ticketForm.get('body')?.value
      };

      this.ticketService.crearInvitado(ticketData).subscribe({
        next: (response) => {
          console.log('✅ Ticket creado:', response);
          alert(`¡Ticket creado exitosamente! ID: ${response.ticketId}`);
          this.onReset();
          this.isLoading = false;
        },
        error: (err) => {
          console.error('❌ Error al crear ticket:', err);
          alert(`Error: ${err}`);
          this.isLoading = false;
        }
      });
    } else {
      this.ticketForm.markAllAsTouched();
      alert('Por favor, complete todos los campos obligatorios correctamente');
    }
  }

  onReset() {
    this.ticketForm.reset();
  }

  onCancel() {
    console.log('Acción de cancelar');
  }
}
