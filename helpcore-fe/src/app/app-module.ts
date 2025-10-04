import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { HttpClientModule } from '@angular/common/http';
import { Login } from './components/login/login';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { Inicio } from './components/inicio/inicio';
import { NavBar } from './components/common/nav-bar/nav-bar';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { Ticket } from './components/ticket/ticket';
import { ConsultarEstado } from './components/consultar-estado/consultar-estado';

@NgModule({
  declarations: [
    App,
    Login,
    Inicio,
    NavBar,
    Ticket,
    ConsultarEstado
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    RouterModule,
    ReactiveFormsModule
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [App]
})
export class AppModule { }
