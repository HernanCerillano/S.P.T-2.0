use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use std::fs;
use std::fs::OpenOptions;
use std::io::{Read, Write};
use std::net::{SocketAddr, TcpStream};
use std::path::Path;
use std::path::PathBuf;
use std::process::{Command, Stdio};
use std::time::{Duration, Instant};
use tauri::{AppHandle, Manager, Theme};

const MASTER_ACTIVATION_KEY: &str = "DC-MECANICA-2026-ENTREGA";
const LICENSE_FILE_NAME: &str = "license.json";
const BUNDLED_BACKEND_JAR: &str = "SPT-backend.jar";
const DEFAULT_BACKEND_PORT: u16 = 8080;

#[derive(Serialize, Deserialize)]
struct LicenseFile {
  fingerprint: String,
  signature: String,
  activated_at: String,
}

#[tauri::command]
fn activation_status(app: AppHandle) -> Result<bool, String> {
  let current_fingerprint = machine_fingerprint();
  let path = license_path(&app)?;
  if !path.exists() {
    return Ok(false);
  }

  let raw = fs::read_to_string(&path).map_err(|_| "No se pudo leer la licencia".to_string())?;
  let stored: LicenseFile =
    serde_json::from_str(&raw).map_err(|_| "La licencia guardada es invalida".to_string())?;

  Ok(
    stored.fingerprint == current_fingerprint
      && stored.signature == license_signature(MASTER_ACTIVATION_KEY, &current_fingerprint),
  )
}

#[tauri::command]
fn activate_app(app: AppHandle, key: String) -> Result<bool, String> {
  let normalized = key.trim();
  if normalized != MASTER_ACTIVATION_KEY {
    return Err("Clave de activacion invalida".to_string());
  }

  let current_fingerprint = machine_fingerprint();
  let payload = LicenseFile {
    fingerprint: current_fingerprint.clone(),
    signature: license_signature(MASTER_ACTIVATION_KEY, &current_fingerprint),
    activated_at: chrono_like_now(),
  };

  let path = license_path(&app)?;
  if let Some(parent) = path.parent() {
    fs::create_dir_all(parent).map_err(|_| "No se pudo preparar la carpeta de licencia".to_string())?;
  }

  let raw = serde_json::to_string_pretty(&payload).map_err(|_| "No se pudo serializar la licencia".to_string())?;
  fs::write(path, raw).map_err(|_| "No se pudo guardar la licencia".to_string())?;
  Ok(true)
}

fn license_path(app: &AppHandle) -> Result<PathBuf, String> {
  let mut base = app
    .path()
    .app_local_data_dir()
    .map_err(|_| "No se pudo resolver la carpeta local de la aplicacion".to_string())?;
  base.push(LICENSE_FILE_NAME);
  Ok(base)
}

fn machine_fingerprint() -> String {
  let computer = std::env::var("COMPUTERNAME").unwrap_or_else(|_| "UNKNOWN_PC".to_string());
  let user = std::env::var("USERNAME").unwrap_or_else(|_| "UNKNOWN_USER".to_string());
  let os = std::env::consts::OS;
  let arch = std::env::consts::ARCH;
  format!("{computer}|{user}|{os}|{arch}")
}

fn license_signature(key: &str, fingerprint: &str) -> String {
  let mut hasher = Sha256::new();
  hasher.update(format!("SPT::{key}::{fingerprint}::DC"));
  format!("{:x}", hasher.finalize())
}

fn chrono_like_now() -> String {
  use std::time::{SystemTime, UNIX_EPOCH};
  match SystemTime::now().duration_since(UNIX_EPOCH) {
    Ok(duration) => duration.as_secs().to_string(),
    Err(_) => "0".to_string(),
  }
}

fn ensure_backend_running(app: &AppHandle) -> Result<(), String> {
  if backend_is_ready() {
    return Ok(());
  }

  let jar_path = resolve_backend_jar(app)
    .ok_or_else(|| "No se encontro el backend empaquetado dentro de la aplicacion".to_string())?;

  let mut started = false;
  for java_cmd in java_command_candidates() {
    if spawn_backend_process(java_cmd.as_path(), &jar_path).is_ok() {
      started = true;
      break;
    }
  }
  if !started {
    return Err("No se pudo iniciar Java para levantar el backend".to_string());
  }

  let deadline = Instant::now() + Duration::from_secs(20);
  while Instant::now() < deadline {
    if backend_is_ready() {
      return Ok(());
    }
    std::thread::sleep(Duration::from_millis(250));
  }

  Err("El backend no respondio a tiempo".to_string())
}

fn backend_port_open() -> bool {
  let address: SocketAddr = format!("127.0.0.1:{DEFAULT_BACKEND_PORT}")
    .parse()
    .expect("backend socket address");
  TcpStream::connect_timeout(&address, Duration::from_millis(300)).is_ok()
}

fn backend_is_ready() -> bool {
  if !backend_port_open() {
    return false;
  }

  let address: SocketAddr = format!("127.0.0.1:{DEFAULT_BACKEND_PORT}")
    .parse()
    .expect("backend socket address");

  let mut stream = match TcpStream::connect_timeout(&address, Duration::from_millis(500)) {
    Ok(stream) => stream,
    Err(_) => return false,
  };

  let _ = stream.set_read_timeout(Some(Duration::from_millis(700)));
  let _ = stream.set_write_timeout(Some(Duration::from_millis(700)));

  let request = b"GET /api/clientes HTTP/1.1\r\nHost: 127.0.0.1\r\nConnection: close\r\n\r\n";
  if stream.write_all(request).is_err() {
    return false;
  }

  let mut buffer = [0_u8; 256];
  let bytes_read = match stream.read(&mut buffer) {
    Ok(n) => n,
    Err(_) => return false,
  };

  if bytes_read == 0 {
    return false;
  }

  let response_head = String::from_utf8_lossy(&buffer[..bytes_read]);
  response_head.starts_with("HTTP/1.1 200") || response_head.starts_with("HTTP/1.0 200")
}

fn resolve_backend_jar(app: &AppHandle) -> Option<PathBuf> {
  if let Ok(resource_dir) = app.path().resource_dir() {
    let bundled = resource_dir.join(BUNDLED_BACKEND_JAR);
    if bundled.exists() {
      return Some(bundled);
    }
  }

  if cfg!(debug_assertions) {
    let dev_path = PathBuf::from(env!("CARGO_MANIFEST_DIR"))
      .join("..")
      .join("..")
      .join("target")
      .join("SPT-0.0.1-SNAPSHOT.jar");
    if dev_path.exists() {
      return Some(dev_path);
    }
  }

  None
}

fn java_command_candidates() -> Vec<PathBuf> {
  let mut candidates = Vec::new();

  if let Ok(java_home) = std::env::var("JAVA_HOME") {
    let bin = PathBuf::from(java_home).join("bin");
    candidates.push(bin.join("javaw.exe"));
    candidates.push(bin.join("java.exe"));
  }

  // Fallbacks comunes en Windows para evitar depender del PATH.
  let common_windows_candidates = [
    r"C:\Program Files\Java\jdk-24\bin\javaw.exe",
    r"C:\Program Files\Java\jdk-24\bin\java.exe",
    r"C:\Program Files\Java\jdk-21\bin\javaw.exe",
    r"C:\Program Files\Java\jdk-21\bin\java.exe",
    r"C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot\bin\javaw.exe",
    r"C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot\bin\java.exe",
  ];
  for candidate in common_windows_candidates {
    candidates.push(PathBuf::from(candidate));
  }

  candidates.push(PathBuf::from("javaw"));
  candidates.push(PathBuf::from("java"));
  candidates
}

fn spawn_backend_process(java_cmd: &Path, jar_path: &PathBuf) -> std::io::Result<()> {
  let log_path = std::env::temp_dir().join("spt-backend.log");
  let backend_log = OpenOptions::new()
    .create(true)
    .append(true)
    .open(log_path)?;
  let backend_log_err = backend_log.try_clone()?;

  let mut command = Command::new(java_cmd);
  command
    .arg("-jar")
    .arg(jar_path)
    .stdin(Stdio::null())
    .stdout(Stdio::from(backend_log))
    .stderr(Stdio::from(backend_log_err));

  #[cfg(target_os = "windows")]
  {
    use std::os::windows::process::CommandExt;
    command.creation_flags(0x08000000);
  }

  command.spawn().map(|_| ())
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
  tauri::Builder::default()
    .plugin(tauri_plugin_dialog::init())
    .plugin(tauri_plugin_fs::init())
    .invoke_handler(tauri::generate_handler![activation_status, activate_app])
    .setup(|app| {
      if let Err(error) = ensure_backend_running(&app.handle()) {
        eprintln!("No se pudo iniciar el backend automaticamente: {error}");
      }

      if let Some(window) = app.get_webview_window("main") {
        let _ = window.set_theme(Some(Theme::Dark));
        let _ = window.set_title("S.P.T. — Suspensión Tito");
      }

      if cfg!(debug_assertions) {
        app.handle().plugin(
          tauri_plugin_log::Builder::default()
            .level(log::LevelFilter::Info)
            .build(),
        )?;
      }
      Ok(())
    })
    .run(tauri::generate_context!())
    .expect("error while running tauri application");
}
