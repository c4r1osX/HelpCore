import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoriaTicketService {
    private baseUrl = environment.apiUrl;
    private path = environment.categoriaTicketService;

    constructor(private http: HttpClient) {}

    listarCategoriaTicket(): Observable<any> {
      return this.http.get(`${this.baseUrl + this.path}/listar`);
    }
}
