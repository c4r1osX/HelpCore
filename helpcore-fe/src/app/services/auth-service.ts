import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequest } from '../dto/login-request';
import { TokenResponse } from '../dto/token-response';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = environment.apiUrl;
  private path = environment.authService;

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl + this.path}/login`, request, {
      withCredentials: true
    });
  }

  register(request: any): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl + this.path}/register`, request, {
      withCredentials: true
    });
  }

  refreshToken(): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl + this.path}/refresh`, {}, {
      withCredentials: true
    });
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl + this.path}/logout`, {}, {
      withCredentials: true
    });
  }
}