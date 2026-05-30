const fs = require('node:fs');
const path = require('node:path');

const frontendRoot = path.resolve(__dirname, '..');
const projectRoot = path.resolve(frontendRoot, '..');
const sourceJar = path.join(projectRoot, 'target', 'SPT-0.0.1-SNAPSHOT.jar');
const resourcesDir = path.join(frontendRoot, 'src-tauri', 'resources');
const targetJar = path.join(resourcesDir, 'SPT-backend.jar');

if (!fs.existsSync(sourceJar)) {
  console.error(`No se encontro el backend empaquetado en: ${sourceJar}`);
  process.exit(1);
}

fs.mkdirSync(resourcesDir, { recursive: true });
fs.copyFileSync(sourceJar, targetJar);
console.log(`Backend copiado para el bundle: ${targetJar}`);
