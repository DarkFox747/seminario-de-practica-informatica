-- =========================================================
-- Esquema: proyecto_db  (MySQL 8+)
-- =========================================================
CREATE DATABASE IF NOT EXISTS proyecto_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE proyecto_db;

-- Sugerido para consistencia
SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- =========================================================
-- Tablas base
-- =========================================================

CREATE TABLE IF NOT EXISTS usuario (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  rol VARCHAR(40) NOT NULL COMMENT 'ADMIN/DEVOPS/REVISOR (catálogo de aplicación)',
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS repo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  proveedor ENUM('GITHUB','GITLAB','BITBUCKET') NOT NULL,
  nombre VARCHAR(160) NOT NULL,
  slug VARCHAR(160),
  remoto_url VARCHAR(300) NOT NULL,
  external_id VARCHAR(80),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_repo_provider_url (proveedor, remoto_url),
  KEY idx_repo_external (proveedor, external_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS politica (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  version VARCHAR(40) NOT NULL,
  activa BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_politica_nombre_version (nombre, version)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS plantilla_notificacion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  codigo VARCHAR(80) NOT NULL UNIQUE,
  asunto_template VARCHAR(200) NOT NULL,
  cuerpo_template TEXT NOT NULL,
  descripcion VARCHAR(200)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reporte (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(160) NOT NULL,
  descripcion TEXT,
  formato VARCHAR(10) NOT NULL DEFAULT 'CSV' COMMENT 'CSV|PDF|JSON',
  definicion_sql TEXT NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_by BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_reporte_created_by (created_by),
  CONSTRAINT fk_reporte_created_by
    FOREIGN KEY (created_by) REFERENCES usuario(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- PRs y actividad de VCS
-- =========================================================

CREATE TABLE IF NOT EXISTS pull_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repo_id BIGINT NOT NULL,
  numero INT NOT NULL,
  titulo VARCHAR(200),
  autor_id BIGINT NULL,
  estado ENUM('OPEN','CLOSED','MERGED') NOT NULL DEFAULT 'OPEN',
  base_sha VARCHAR(40) NOT NULL,
  head_sha VARCHAR(40) NOT NULL,
  link_url VARCHAR(300),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL,
  UNIQUE KEY uq_pr_repo_num (repo_id, numero),
  KEY idx_pr_estado (estado),
  KEY idx_pr_updated (updated_at),
  CONSTRAINT fk_pr_repo
    FOREIGN KEY (repo_id) REFERENCES repo(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pr_autor
    FOREIGN KEY (autor_id) REFERENCES usuario(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pr_commit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pr_id BIGINT NOT NULL,
  sha VARCHAR(40) NOT NULL,
  parent_sha VARCHAR(40),
  autor_nombre VARCHAR(120),
  autor_email VARCHAR(160),
  authored_at TIMESTAMP NULL,
  committed_at TIMESTAMP NULL,
  mensaje TEXT,
  stats_add INT,
  stats_del INT,
  stats_files INT,
  UNIQUE KEY uq_commit_sha (sha),
  KEY idx_commit_pr (pr_id),
  CONSTRAINT fk_commit_pr
    FOREIGN KEY (pr_id) REFERENCES pull_request(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pr_file_change (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pr_id BIGINT NOT NULL,
  path VARCHAR(400) NOT NULL,
  status VARCHAR(16) NOT NULL COMMENT 'added|modified|deleted|renamed',
  additions INT,
  deletions INT,
  changes INT,
  module VARCHAR(160),
  language VARCHAR(40),
  blob_id VARCHAR(64),
  UNIQUE KEY uq_change_pr_path (pr_id, path),
  KEY idx_change_module (module),
  CONSTRAINT fk_change_pr
    FOREIGN KEY (pr_id) REFERENCES pull_request(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pr_status_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pr_id BIGINT NOT NULL,
  contexto VARCHAR(80) NOT NULL DEFAULT 'risk-check',
  estado VARCHAR(32) NOT NULL COMMENT 'success|failure|warning|pending',
  descripcion VARCHAR(300),
  target_url VARCHAR(300),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_status_ctx_time (pr_id, contexto, created_at),
  CONSTRAINT fk_status_pr
    FOREIGN KEY (pr_id) REFERENCES pull_request(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Políticas y reglas
-- =========================================================

CREATE TABLE IF NOT EXISTS umbral (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  politica_id BIGINT NOT NULL,
  warn ENUM('BAJO','MEDIO','ALTO','CRITICO') NOT NULL DEFAULT 'MEDIO',
  block ENUM('BAJO','MEDIO','ALTO','CRITICO') NOT NULL DEFAULT 'ALTO',
  max_tiempo_seg INT NOT NULL DEFAULT 300,
  KEY idx_umbral_politica (politica_id),
  CONSTRAINT fk_umbral_politica
    FOREIGN KEY (politica_id) REFERENCES politica(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS regla (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  politica_id BIGINT NOT NULL,
  codigo VARCHAR(80) NOT NULL,
  tipo VARCHAR(40) NOT NULL COMMENT 'DEPENDENCY|SECURITY|METRIC|STYLE',
  severidad ENUM('BAJO','MEDIO','ALTO','CRITICO') NOT NULL,
  parametros_json TEXT,
  activa BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE KEY uq_regla_codigo (politica_id, codigo),
  CONSTRAINT fk_regla_politica
    FOREIGN KEY (politica_id) REFERENCES politica(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS modulo_critico (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  politica_id BIGINT NOT NULL,
  patron VARCHAR(200) NOT NULL,
  UNIQUE KEY uq_modcrit_patron (politica_id, patron),
  CONSTRAINT fk_modcrit_politica
    FOREIGN KEY (politica_id) REFERENCES politica(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ruta_sensible (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  politica_id BIGINT NOT NULL,
  patron VARCHAR(300) NOT NULL,
  UNIQUE KEY uq_ruta_patron (politica_id, patron),
  CONSTRAINT fk_ruta_politica
    FOREIGN KEY (politica_id) REFERENCES politica(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exclusion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  politica_id BIGINT NOT NULL,
  patron VARCHAR(300) NOT NULL,
  motivo VARCHAR(200),
  UNIQUE KEY uq_exclusion_patron (politica_id, patron),
  CONSTRAINT fk_exclusion_politica
    FOREIGN KEY (politica_id) REFERENCES politica(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Resultados de análisis por PR
-- =========================================================

CREATE TABLE IF NOT EXISTS analisis_pr (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repo_id BIGINT NOT NULL,
  pr_id BIGINT NOT NULL,
  base_sha VARCHAR(40) NOT NULL,
  head_sha VARCHAR(40) NOT NULL,
  politica_id BIGINT NOT NULL,
  politica_version VARCHAR(40) NOT NULL,
  riesgo ENUM('BAJO','MEDIO','ALTO','CRITICO') NOT NULL,
  puntaje DECIMAL(5,2),
  parcial BOOLEAN NOT NULL DEFAULT FALSE,
  tiempo_ms INT,
  artefacto_json VARCHAR(300),
  artefacto_pdf VARCHAR(300),
  resumen TEXT,
  started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at TIMESTAMP NULL DEFAULT NULL,
  KEY idx_analisis_pr_time (pr_id, ended_at),
  KEY idx_analisis_repo_time (repo_id, started_at),
  CONSTRAINT fk_analisis_repo
    FOREIGN KEY (repo_id) REFERENCES repo(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_analisis_pr
    FOREIGN KEY (pr_id) REFERENCES pull_request(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_analisis_politica
    FOREIGN KEY (politica_id) REFERENCES politica(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS analisis_pr_impacto (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  analisis_id BIGINT NOT NULL,
  categoria VARCHAR(40) NOT NULL COMMENT 'DEPENDENCY|SECURITY|METRIC|STYLE',
  severidad ENUM('BAJO','MEDIO','ALTO','CRITICO') NOT NULL,
  archivo VARCHAR(400),
  modulo VARCHAR(160),
  evidencia_json TEXT,
  KEY idx_imp_analisis (analisis_id),
  KEY idx_imp_sev (severidad),
  CONSTRAINT fk_imp_analisis
    FOREIGN KEY (analisis_id) REFERENCES analisis_pr(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS analisis_pr_modulo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  analisis_id BIGINT NOT NULL,
  modulo VARCHAR(160) NOT NULL,
  razon VARCHAR(100) NOT NULL COMMENT 'import|call|contract|query|route',
  evidencia_json TEXT,
  UNIQUE KEY uq_mod_razon (analisis_id, modulo, razon),
  CONSTRAINT fk_mod_analisis
    FOREIGN KEY (analisis_id) REFERENCES analisis_pr(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS silenciamiento_pr (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repo_id BIGINT NOT NULL,
  pr_number INT NOT NULL,
  hasta TIMESTAMP NOT NULL,
  motivo VARCHAR(200),
  UNIQUE KEY uq_silencio_pr (repo_id, pr_number),
  CONSTRAINT fk_silencio_repo
    FOREIGN KEY (repo_id) REFERENCES repo(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Notificaciones
-- =========================================================

CREATE TABLE IF NOT EXISTS notificacion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  canal ENUM('CLI','LOG','SLACK') NOT NULL,
  asunto VARCHAR(200) NOT NULL,
  cuerpo TEXT NOT NULL,
  usuario_id BIGINT NULL,
  relacionada_tipo VARCHAR(30),
  relacionada_id BIGINT,
  estado ENUM('PENDIENTE','ENVIADA','ERROR') NOT NULL DEFAULT 'PENDIENTE',
  enviada_at TIMESTAMP NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notif_usuario (usuario_id),
  KEY idx_notif_estado (estado),
  KEY idx_notif_relacion (relacionada_tipo, relacionada_id),
  CONSTRAINT fk_notif_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Reportes & ejecuciones
-- =========================================================

CREATE TABLE IF NOT EXISTS reporte_filtro (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporte_id BIGINT NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  tipo VARCHAR(30) NOT NULL COMMENT 'date|int|string',
  valor_default VARCHAR(160),
  requerido BOOLEAN NOT NULL DEFAULT FALSE,
  KEY idx_reporte_filtro_reporte (reporte_id),
  CONSTRAINT fk_rep_filtro_reporte
    FOREIGN KEY (reporte_id) REFERENCES reporte(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reporte_programacion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporte_id BIGINT NOT NULL,
  cron_expr VARCHAR(120) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  ultimo_disparo TIMESTAMP NULL DEFAULT NULL,
  proximo_disparo TIMESTAMP NULL DEFAULT NULL,
  KEY idx_reporte_prog_reporte (reporte_id),
  CONSTRAINT fk_rep_prog_reporte
    FOREIGN KEY (reporte_id) REFERENCES reporte(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reporte_ejecucion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporte_id BIGINT NULL,
  ejecutado_por BIGINT NULL,
  estado ENUM('PENDIENTE','EN_PROGRESO','OK','ERROR') NOT NULL DEFAULT 'PENDIENTE',
  parametros_json TEXT,
  resultado_path VARCHAR(300),
  started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at TIMESTAMP NULL DEFAULT NULL,
  mensaje_error TEXT,
  KEY idx_ejec_reporte (reporte_id),
  KEY idx_ejec_usuario (ejecutado_por),
  KEY idx_ejec_estado (estado),
  CONSTRAINT fk_ejec_reporte
    FOREIGN KEY (reporte_id) REFERENCES reporte(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_ejec_usuario
    FOREIGN KEY (ejecutado_por) REFERENCES usuario(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- Auditoría & Observabilidad
-- =========================================================

CREATE TABLE IF NOT EXISTS auditoria (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entidad_tipo VARCHAR(30) NOT NULL COMMENT 'PR, ANALISIS, POLITICA, REGLA, REPORTE, etc.',
  entidad_id BIGINT NOT NULL,
  accion VARCHAR(30) NOT NULL COMMENT 'CREATE, UPDATE, DELETE, EXECUTE, ASSIGN, STATUS',
  actor_id BIGINT NULL,
  detalle_json TEXT,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_entidad (entidad_tipo, entidad_id),
  KEY idx_audit_actor (actor_id),
  CONSTRAINT fk_audit_actor
    FOREIGN KEY (actor_id) REFERENCES usuario(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS metric_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  name VARCHAR(80) NOT NULL COMMENT 'p.ej. t_analisis_ms, error_rate, blocked_rate',
  valor DECIMAL(12,4) NOT NULL,
  dimensiones_json TEXT,
  trace_id VARCHAR(64),
  KEY idx_metric_name_ts (name, ts)
) ENGINE=InnoDB;
