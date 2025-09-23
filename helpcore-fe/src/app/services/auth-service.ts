import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest } from '../dto/login-request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = environment.apiUrl;
  private path = environment.authService;

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<any>{
    return this.http.post<any>(`${this.baseUrl + this.path}/login`, request)
  }
}
