import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Inicio } from './components/inicio/inicio';

const routes: Routes = [
  {path: 'inicio', component: Inicio},
  {path: '',  redirectTo:'inicio' , pathMatch:'full'},

  {path: 'login', component: Login}

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
