-- =========================================================
-- Local Code Review Assistant - Esquema MySQL
-- =========================================================
-- Recomendado: ejecutar con MySQL 8.x
-- =========================================================

-- (Opcional) Crear base de datos
CREATE DATABASE IF NOT EXISTS code_review_local
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE code_review_local;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- Tablas de Catálogo / Lookup
-- =========================================================

DROP TABLE IF EXISTS user_role_type;
CREATE TABLE user_role_type (
  code        VARCHAR(20) PRIMARY KEY,        -- DEVELOPER, TECH_LEAD, QA, ADMIN
  description VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS run_status_type;
CREATE TABLE run_status_type (
  code        VARCHAR(20) PRIMARY KEY,        -- SUCCESS, ERROR, EMPTY_DIFF
  description VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS file_change_type;
CREATE TABLE file_change_type (
  code        VARCHAR(20) PRIMARY KEY,        -- ADDED, MODIFIED, DELETED, RENAMED
  description VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS severity_type;
CREATE TABLE severity_type (
  code        VARCHAR(20) PRIMARY KEY,        -- CRITICAL, HIGH, MEDIUM, LOW
  description VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Entidades Core
-- =========================================================

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(120) NOT NULL,
  email       VARCHAR(180) NOT NULL,
  role_code   VARCHAR(20)  NOT NULL,
  active      TINYINT(1)   NOT NULL DEFAULT 1,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uq_users_email UNIQUE (email),
  CONSTRAINT fk_users_role FOREIGN KEY (role_code)
    REFERENCES user_role_type(code)
    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS repositories;
CREATE TABLE repositories (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  local_path  VARCHAR(512)  NOT NULL,
  vcs         VARCHAR(20)   NOT NULL,   -- 'git'
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_repositories PRIMARY KEY (id),
  KEY idx_repositories_local_path (local_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS severity_policies;
CREATE TABLE severity_policies (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  name           VARCHAR(120)  NOT NULL,
  description    VARCHAR(512)  NULL,
  rules_json     TEXT          NOT NULL,   -- mapeo de categorías -> severidades
  version        INT           NOT NULL DEFAULT 1,
  active         TINYINT(1)    NOT NULL DEFAULT 1,
  effective_from DATE          NULL,
  created_by     BIGINT        NULL,
  created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME      NULL,
  CONSTRAINT pk_severity_policies PRIMARY KEY (id),
  CONSTRAINT fk_policies_created_by FOREIGN KEY (created_by)
    REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  KEY idx_policies_version (version),
  KEY idx_policies_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS endpoint_mocks;
CREATE TABLE endpoint_mocks (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  name        VARCHAR(120)  NOT NULL,
  version     VARCHAR(40)   NOT NULL,
  spec        TEXT          NOT NULL,     -- contrato JSON simulado
  active      TINYINT(1)    NOT NULL DEFAULT 1,
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_endpoint_mocks PRIMARY KEY (id),
  KEY idx_endpoint_mocks_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS analysis_runs;
CREATE TABLE analysis_runs (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  user_id         BIGINT        NOT NULL,
  repo_id         BIGINT        NOT NULL,
  policy_id       BIGINT        NOT NULL,
  endpoint_id     BIGINT            NULL,   -- puede ser NULL si el mock está embebido
  base_branch     VARCHAR(100)  NOT NULL,
  target_branch   VARCHAR(100)  NOT NULL,
  status_code     VARCHAR(20)   NOT NULL,
  total_files     INT               NULL,
  total_findings  INT               NULL,
  critical_count  INT               NULL DEFAULT 0,
  high_count      INT               NULL DEFAULT 0,
  medium_count    INT               NULL DEFAULT 0,
  low_count       INT               NULL DEFAULT 0,
  info_count      INT               NULL DEFAULT 0,
  error_message   TEXT              NULL,
  started_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at    DATETIME          NULL,
  duration_ms     BIGINT            NULL,
  created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_analysis_runs PRIMARY KEY (id),
  CONSTRAINT fk_runs_user FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_runs_repo FOREIGN KEY (repo_id)
    REFERENCES repositories(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_runs_policy FOREIGN KEY (policy_id)
    REFERENCES severity_policies(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT fk_runs_endpoint FOREIGN KEY (endpoint_id)
    REFERENCES endpoint_mocks(id)
    ON UPDATE RESTRICT ON DELETE SET NULL,
  CONSTRAINT fk_runs_status FOREIGN KEY (status_code)
    REFERENCES run_status_type(code)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  KEY idx_runs_user_started (user_id, started_at),
  KEY idx_runs_repo_started (repo_id, started_at),
  KEY idx_runs_status (status_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS diff_files;
CREATE TABLE diff_files (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  run_id             BIGINT       NOT NULL,
  path               VARCHAR(512) NOT NULL,
  change_type_code   VARCHAR(20)  NOT NULL,
  additions          INT          NOT NULL DEFAULT 0,
  deletions          INT          NOT NULL DEFAULT 0,
  CONSTRAINT pk_diff_files PRIMARY KEY (id),
  CONSTRAINT fk_diff_run FOREIGN KEY (run_id)
    REFERENCES analysis_runs(id)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT fk_diff_change_type FOREIGN KEY (change_type_code)
    REFERENCES file_change_type(code)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  KEY idx_diff_run (run_id),
  KEY idx_diff_path (path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS findings;
CREATE TABLE findings (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  run_id         BIGINT       NOT NULL,
  code           VARCHAR(64)  NOT NULL,    -- identificador de regla del mock
  title          VARCHAR(255) NOT NULL,
  description    TEXT         NOT NULL,
  severity_code  VARCHAR(20)  NOT NULL,
  file_path      VARCHAR(512) NOT NULL,
  line_start     INT          NULL,
  line_end       INT          NULL,
  category       VARCHAR(64)  NULL,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_findings PRIMARY KEY (id),
  CONSTRAINT fk_findings_run FOREIGN KEY (run_id)
    REFERENCES analysis_runs(id)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT fk_findings_sev FOREIGN KEY (severity_code)
    REFERENCES severity_type(code)
    ON UPDATE RESTRICT ON DELETE RESTRICT,
  KEY idx_findings_run_sev (run_id, severity_code),
  KEY idx_findings_file (file_path),
  KEY idx_findings_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- Analítica / Preferencias
-- =========================================================

DROP TABLE IF EXISTS user_stats;
CREATE TABLE user_stats (
  id              BIGINT  NOT NULL AUTO_INCREMENT,
  user_id         BIGINT  NOT NULL,
  period_start    DATE    NOT NULL,
  period_end      DATE    NOT NULL,
  analyses_count  INT     NOT NULL DEFAULT 0,
  findings_count  INT     NOT NULL DEFAULT 0,
  critical_count  INT     NOT NULL DEFAULT 0,
  high_count      INT     NOT NULL DEFAULT 0,
  medium_count    INT     NOT NULL DEFAULT 0,
  low_count       INT     NOT NULL DEFAULT 0,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_user_stats PRIMARY KEY (id),
  CONSTRAINT fk_user_stats_user FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  CONSTRAINT uq_user_stats_period UNIQUE (user_id, period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS metrics_snapshots;
CREATE TABLE metrics_snapshots (
  id                 BIGINT  NOT NULL AUTO_INCREMENT,
  period_start       DATE    NOT NULL,
  period_end         DATE    NOT NULL,
  total_analyses     INT     NOT NULL DEFAULT 0,
  total_findings     INT     NOT NULL DEFAULT 0,
  critical_count     INT     NOT NULL DEFAULT 0,
  high_count         INT     NOT NULL DEFAULT 0,
  medium_count       INT     NOT NULL DEFAULT 0,
  low_count          INT     NOT NULL DEFAULT 0,
  avg_severity_score DOUBLE  NULL,
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_metrics_snapshots PRIMARY KEY (id),
  KEY idx_metrics_period (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS dashboard_views;
CREATE TABLE dashboard_views (
  id             BIGINT  NOT NULL AUTO_INCREMENT,
  owner_user_id  BIGINT  NOT NULL,
  filters_json   TEXT    NOT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_dashboard_views PRIMARY KEY (id),
  CONSTRAINT fk_dash_owner FOREIGN KEY (owner_user_id)
    REFERENCES users(id)
    ON UPDATE RESTRICT ON DELETE CASCADE,
  KEY idx_dash_owner (owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- Semillas de catálogos (opcionales pero recomendadas)
-- =========================================================

INSERT INTO user_role_type (code, description) VALUES
  ('DEVELOPER','Desarrollador'),
  ('TECH_LEAD','Líder Técnico'),
  ('QA','Calidad'),
  ('ADMIN','Administrador')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO run_status_type (code, description) VALUES
  ('SUCCESS','Ejecución exitosa'),
  ('ERROR','Ejecución con error'),
  ('EMPTY_DIFF','No había cambios para analizar')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO file_change_type (code, description) VALUES
  ('ADDED','Archivo agregado'),
  ('MODIFIED','Archivo modificado'),
  ('DELETED','Archivo eliminado'),
  ('RENAMED','Archivo renombrado')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO severity_type (code, description) VALUES
  ('CRITICAL','Severidad crítica'),
  ('HIGH','Severidad alta'),
  ('MEDIUM','Severidad media'),
  ('LOW','Severidad baja')
ON DUPLICATE KEY UPDATE description = VALUES(description);
