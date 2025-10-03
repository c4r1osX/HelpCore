import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, switchMap } from 'rxjs/operators';
import { throwError, EMPTY } from 'rxjs';
import { AuthService } from '../services/auth-service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const authReq = req.clone({
      setHeaders: {},
      withCredentials: true
    });

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          if (!req.url.includes('/auth/refresh') && !req.url.includes('/auth/login')) {
            return this.authService.refreshToken().pipe(
              switchMap(() => {
                const retryReq = req.clone({ withCredentials: true });
                return next.handle(retryReq);
              }),
              catchError((refreshError) => {
                this.router.navigate(['/login']);
                return throwError(() => refreshError);
              })
            );
          } else {
            this.router.navigate(['/login']);
          }
        }
        return throwError(() => error);
      })
    );
  }
}