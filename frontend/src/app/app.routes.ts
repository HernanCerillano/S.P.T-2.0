import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { Clientes } from './pages/clientes/clientes';
import { Vehiculos } from './pages/vehiculos/vehiculos';
import { Piezas } from './pages/piezas/piezas';
import { Servicios } from './pages/servicios/servicios';
import { Presupuestos } from './pages/presupuestos/presupuestos';
import { OrdenesTrabajo } from './pages/ordenes-trabajo/ordenes-trabajo';
import { Pagos } from './pages/pagos/pagos';
import { Empleados } from './pages/empleados/empleados';
import { Facturas } from './pages/facturas/facturas';
import { Calendario } from './pages/calendario/calendario';
import { Reportes } from './pages/reportes/reportes';
import { Configuracion } from './pages/configuracion/configuracion';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: Dashboard },
  { path: 'clientes', component: Clientes },
  { path: 'vehiculos', component: Vehiculos },
  { path: 'piezas', component: Piezas },
  { path: 'servicios', component: Servicios },
  { path: 'presupuestos', component: Presupuestos },
  { path: 'ordenes-trabajo', component: OrdenesTrabajo },
  { path: 'pagos', component: Pagos },
  { path: 'empleados', component: Empleados },
  { path: 'facturas', component: Facturas },
  { path: 'calendario', component: Calendario },
  { path: 'reportes', component: Reportes },
  { path: 'configuracion', component: Configuracion },
  { path: '**', redirectTo: 'dashboard' }
];
