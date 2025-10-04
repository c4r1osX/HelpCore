import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Inicio } from './components/inicio/inicio';
import { Ticket } from './components/ticket/ticket';
import { ConsultarEstado } from './components/consultar-estado/consultar-estado';

const routes: Routes = [
  {path: 'inicio', component: Inicio},
  {path: '',  redirectTo:'inicio' , pathMatch:'full'},
  {path: 'login', component: Login},
  {path: 'ticket', component: Ticket},
  {path: 'consultar-estado', component: ConsultarEstado}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
