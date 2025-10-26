-- =========================================================
-- Comandos SQL con datos reales - Asistente Local (MySQL 8)
-- Esquema: code_review_local
-- =========================================================
USE code_review_local;

-- (Opcional) Reinsertar catálogos por si no existen
INSERT INTO user_role_type (code, description) VALUES
  ('DEVELOPER','Desarrollador'),
  ('TECH_LEAD','Líder Técnico'),
  ('QA','Calidad'),
  ('ADMIN','Administrador')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO run_status_type (code, description) VALUES
  ('SUCCESS','Ejecución exitosa'),
  ('ERROR','Ejecución con error'),
  ('EMPTY_DIFF','Sin cambios para analizar')
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

-- =========================================================
-- users
-- =========================================================
-- INSERT (usuarios reales alineados a la seed)
INSERT INTO users (id, name, email, role_code, active, created_at) VALUES
('u-001','Diego Soler','diego@crombie.dev','DEVELOPER',1,NOW()),
('u-002','Lucía Romero','lucia@crombie.dev','TECH_LEAD',1,NOW()),
('u-003','Mariano Funes','mariano@crombie.dev','QA',1,NOW()),
('u-004','Admin System','admin@crombie.dev','ADMIN',1,NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), role_code=VALUES(role_code), active=VALUES(active);

-- SELECT (todos / por email)
SELECT id, name, email, role_code, active, created_at FROM users ORDER BY created_at DESC;
SELECT id, name, role_code FROM users WHERE email='diego@crombie.dev';

-- DELETE (ejemplo seguro: borra un usuario sin dependencias)
DELETE FROM users WHERE id='u-004';

-- =========================================================
-- repositories
-- =========================================================
-- INSERT
INSERT INTO repositories (id, local_path, vcs, created_at) VALUES
('r-001','/Users/diego/projects/code-review-assistant','git',NOW())
ON DUPLICATE KEY UPDATE local_path=VALUES(local_path);

-- SELECT
SELECT * FROM repositories WHERE id='r-001';

-- DELETE
-- (Evitar borrar si hay analysis_runs que referencian; tendría FK)
-- DELETE FROM repositories WHERE id='r-001';

-- =========================================================
-- severity_policies
-- =========================================================
-- INSERT v1 y v2 (versionadas)
INSERT INTO severity_policies (id, name, rules_json, version, effective_from, created_at) VALUES
('p-001','policy_v1',
 '{
   "security":"CRITICAL",
   "performance":"HIGH",
   "style":"LOW",
   "maintainability":"MEDIUM"
 }',1,CURDATE(),NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), rules_json=VALUES(rules_json), version=VALUES(version);

INSERT INTO severity_policies (id, name, rules_json, version, effective_from, created_at) VALUES
('p-002','policy_v2',
 '{
   "security":"CRITICAL",
   "performance":"HIGH",
   "style":"LOW",
   "maintainability":"LOW",
   "observability":"MEDIUM"
 }',2,CURDATE(),NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), rules_json=VALUES(rules_json), version=VALUES(version);

-- SELECT (última por fecha+versión)
SELECT * FROM severity_policies ORDER BY effective_from DESC, version DESC LIMIT 1;

-- DELETE (no recomendado si hay runs históricos)
-- DELETE FROM severity_policies WHERE id='p-001';

-- =========================================================
-- endpoint_mocks
-- =========================================================
-- INSERT
INSERT INTO endpoint_mocks (id,name,version,spec,active,created_at) VALUES
('e-001','Mock Local Endpoint','1.0.0',
 '{
   "request":{"diff":"string"},
   "response":{"findings":[{"code":"SEC-001","severity":"CRITICAL"}]}
 }',1,NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), version=VALUES(version), active=VALUES(active);

-- SELECT
SELECT id, name, version, active FROM endpoint_mocks;

-- DELETE
-- DELETE FROM endpoint_mocks WHERE id='e-001';

-- =========================================================
-- analysis_runs
-- =========================================================
-- INSERT corrida SUCCESS y EMPTY_DIFF
INSERT INTO analysis_runs (
  id, user_id, repo_id, policy_id, endpoint_id,
  base_branch, target_branch, status_code, started_at, finished_at, duration_ms, created_at
) VALUES
('run-001','u-001','r-001','p-001','e-001','main','feature/refactor-service','SUCCESS',
 NOW() - INTERVAL 15 SECOND, NOW(), 15000, NOW()),
('run-002','u-001','r-001','p-001','e-001','main','feature/no-changes','EMPTY_DIFF',
 NOW() - INTERVAL 5 MINUTE, NULL, NULL, NOW())
ON DUPLICATE KEY UPDATE status_code=VALUES(status_code), finished_at=VALUES(finished_at), duration_ms=VALUES(duration_ms);

-- SELECT (historial por usuario)
SELECT id, base_branch, target_branch, status_code, started_at, finished_at, duration_ms
FROM analysis_runs
WHERE user_id='u-001'
ORDER BY started_at DESC;

-- DELETE (cascadeará diff_files y findings)
-- DELETE FROM analysis_runs WHERE id='run-002';

-- =========================================================
-- diff_files
-- =========================================================
-- INSERT (asociados a run-001)
INSERT INTO diff_files (id, run_id, path, change_type_code, additions, deletions) VALUES
('d-001','run-001','src/service/UserService.java','MODIFIED',25,4),
('d-002','run-001','src/model/User.java','ADDED',42,0),
('d-003','run-001','README.md','MODIFIED',5,1)
ON DUPLICATE KEY UPDATE additions=VALUES(additions), deletions=VALUES(deletions);

-- SELECT (por corrida)
SELECT path, change_type_code, additions, deletions
FROM diff_files
WHERE run_id='run-001'
ORDER BY path;

-- DELETE
-- DELETE FROM diff_files WHERE id='d-003';

-- =========================================================
-- findings
-- =========================================================
-- INSERT (hallazgos del mock)
INSERT INTO findings (
  id, run_id, code, title, description, severity_code,
  file_path, line_start, line_end, category, created_at
) VALUES
('f-001','run-001','SEC-001','Uso de contraseña hardcodeada',
 'Se detectó una cadena sensible en la clase UserService','CRITICAL',
 'src/service/UserService.java',120,125,'security',NOW()),
('f-002','run-001','PERF-002','Consulta SQL sin índice',
 'Posible impacto en rendimiento: consulta sin índice','HIGH',
 'src/service/UserService.java',210,215,'performance',NOW()),
('f-003','run-001','STYLE-005','Método con demasiadas líneas',
 'Refactor sugerido para legibilidad','LOW',
 'src/service/UserService.java',40,90,'style',NOW()),
('f-004','run-001','MAIN-010','Clase sin comentarios Javadoc',
 'Agregar documentación de clase','MEDIUM',
 'src/model/User.java',1,10,'maintainability',NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), severity_code=VALUES(severity_code);

-- SELECT (detalle y resumen por severidad)
SELECT code, title, severity_code, file_path, line_start, line_end, category
FROM findings
WHERE run_id='run-001'
ORDER BY FIELD(severity_code,'CRITICAL','HIGH','MEDIUM','LOW'), file_path;

SELECT severity_code, COUNT(*) AS qty
FROM findings
WHERE run_id='run-001'
GROUP BY severity_code;

-- DELETE
-- DELETE FROM findings WHERE id='f-004';

-- =========================================================
-- user_stats
-- =========================================================
-- INSERT (semana actual para Diego)
INSERT INTO user_stats (
  id, user_id, period_start, period_end,
  analyses_count, findings_count, critical_count, high_count, medium_count, low_count, created_at
) VALUES (
  'us-001','u-001', DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(),
  3,12,2,3,4,3,NOW()
)
ON DUPLICATE KEY UPDATE analyses_count=VALUES(analyses_count),
                        findings_count=VALUES(findings_count),
                        critical_count=VALUES(critical_count),
                        high_count=VALUES(high_count),
                        medium_count=VALUES(medium_count),
                        low_count=VALUES(low_count);

-- SELECT
SELECT * FROM user_stats WHERE user_id='u-001' ORDER BY period_start DESC;

-- DELETE
-- DELETE FROM user_stats WHERE id='us-001';

-- =========================================================
-- metrics_snapshots
-- =========================================================
-- INSERT (agregado semanal)
INSERT INTO metrics_snapshots (
  id, period_start, period_end,
  total_analyses, total_findings,
  critical_count, high_count, medium_count, low_count,
  avg_severity_score, created_at
) VALUES (
  'ms-001', DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(),
  10,40,6,10,12,12,2.75,NOW()
)
ON DUPLICATE KEY UPDATE total_analyses=VALUES(total_analyses),
                        total_findings=VALUES(total_findings),
                        critical_count=VALUES(critical_count),
                        high_count=VALUES(high_count),
                        medium_count=VALUES(medium_count),
                        low_count=VALUES(low_count),
                        avg_severity_score=VALUES(avg_severity_score);

-- SELECT
SELECT * FROM metrics_snapshots ORDER BY period_start DESC;

-- DELETE
-- DELETE FROM metrics_snapshots WHERE id='ms-001';

-- =========================================================
-- dashboard_views
-- =========================================================
-- INSERT (preferencias del TL Lucía)
INSERT INTO dashboard_views (id, owner_user_id, filters_json, created_at) VALUES
('dash-001','u-002','{"period":"last7days","severity":["CRITICAL","HIGH"]}',NOW())
ON DUPLICATE KEY UPDATE filters_json=VALUES(filters_json);

-- SELECT
SELECT id, owner_user_id, created_at, filters_json
FROM dashboard_views
WHERE owner_user_id='u-002'
ORDER BY created_at DESC;

-- DELETE
-- DELETE FROM dashboard_views WHERE id='dash-001';

-- =========================================================
-- Consultas útiles (reales)
-- =========================================================

-- Historial por usuario con resumen de severidades
SELECT r.id AS run_id, r.base_branch, r.target_branch, r.status_code,
       r.started_at, r.duration_ms,
       SUM(CASE WHEN f.severity_code='CRITICAL' THEN 1 ELSE 0 END) AS crit,
       SUM(CASE WHEN f.severity_code='HIGH'     THEN 1 ELSE 0 END) AS high_,
       SUM(CASE WHEN f.severity_code='MEDIUM'   THEN 1 ELSE 0 END) AS med,
       SUM(CASE WHEN f.severity_code='LOW'      THEN 1 ELSE 0 END) AS low_
FROM analysis_runs r
LEFT JOIN findings f ON f.run_id = r.id
WHERE r.user_id = 'u-001'
GROUP BY r.id, r.base_branch, r.target_branch, r.status_code, r.started_at, r.duration_ms
ORDER BY r.started_at DESC;

-- Top 5 usuarios por findings CRITICAL últimos 30 días
SELECT u.id, u.name, COUNT(*) AS critical_findings
FROM users u
JOIN analysis_runs r ON r.user_id = u.id
JOIN findings f ON f.run_id = r.id
WHERE f.severity_code = 'CRITICAL'
  AND r.started_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY u.id, u.name
ORDER BY critical_findings DESC
LIMIT 5;

-- Distribución de severidades últimos 14 días
SELECT f.severity_code, COUNT(*) AS qty
FROM findings f
JOIN analysis_runs r ON r.id = f.run_id
WHERE r.started_at BETWEEN DATE_SUB(CURDATE(), INTERVAL 14 DAY) AND NOW()
GROUP BY f.severity_code
ORDER BY FIELD(f.severity_code,'CRITICAL','HIGH','MEDIUM','LOW');

-- Archivos más señalados (top 10) últimos 14 días
SELECT f.file_path, COUNT(*) AS hits
FROM findings f
JOIN analysis_runs r ON r.id = f.run_id
WHERE r.started_at >= DATE_SUB(CURDATE(), INTERVAL 14 DAY)
GROUP BY f.file_path
ORDER BY hits DESC
LIMIT 10;

-- Limpieza segura de la corrida run-001 (ejemplo)
-- (Elimina findings y diff por ON DELETE CASCADE, luego el run)
-- DELETE FROM analysis_runs WHERE id='run-001';
