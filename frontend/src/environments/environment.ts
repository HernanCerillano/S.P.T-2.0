// Environment de desarrollo (dev) — usado por `ng serve` y `tauri dev`.
// El build de producción lo reemplaza por `environment.prod.ts` vía fileReplacements en angular.json.
export const environment = {
  production: false,
  apiUrl: 'http://127.0.0.1:8080/api',
};
