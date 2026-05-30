import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-stock',
  imports: [RouterLink],
  template: `
    <section class="page">
      <header class="page-head"><div><h1>Stock</h1><p>El modulo de stock fue eliminado en esta version.</p></div></header>
      <article class="panel"><p class="hint">Este modulo ya no esta disponible. El manejo de piezas se realiza directamente desde Presupuestos y Ordenes de Trabajo.</p><a class="btn btn-soft" routerLink="/piezas">Ir a Piezas</a></article>
    </section>
  `,
})
export class Stock {}
