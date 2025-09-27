import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
    private baseUrl = environment.apiUrl;
    private path = environment.ticketService;

    
  constructor(private http: HttpClient) {}

    crearInvitado(ticketData: any): Observable<any> {
    return this.http.post(`${this.baseUrl + this.path}/crear-invitado`, ticketData);
  }

}
