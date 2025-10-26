-- =========================================================
-- Local Code Review Assistant - Seed de Datos de Prueba
-- =========================================================
-- Ejecutar sobre la BD creada previamente
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- Usuarios
-- =========================

INSERT INTO users (id, name, email, role_code, active, created_at) VALUES
  ('u-001', 'Diego Soler', 'diego@crombie.dev', 'DEVELOPER', 1, NOW()),
  ('u-002', 'Lucía Romero', 'lucia@crombie.dev', 'TECH_LEAD', 1, NOW()),
  ('u-003', 'Mariano Funes', 'mariano@crombie.dev', 'QA', 1, NOW()),
  ('u-004', 'Admin System', 'admin@crombie.dev', 'ADMIN', 1, NOW());

-- =========================
-- Repositorio
-- =========================

INSERT INTO repositories (id, local_path, vcs, created_at) VALUES
  ('r-001', '/Users/diego/projects/code-review-assistant', 'git', NOW());

-- =========================
-- Políticas de severidad
-- =========================

INSERT INTO severity_policies (id, name, rules_json, version, effective_from, created_at) VALUES
  ('p-001', 'policy_v1',
   '{
     "security": "CRITICAL",
     "performance": "HIGH",
     "style": "LOW",
     "maintainability": "MEDIUM"
   }',
   1, CURDATE(), NOW()
  );

-- =========================
-- Endpoint Mock
-- =========================

INSERT INTO endpoint_mocks (id, name, version, spec, active, created_at) VALUES
  ('e-001', 'Mock Local Endpoint', '1.0.0',
   '{
      "request": {"diff":"string"},
      "response": {"findings":[{"code":"SEC-001","severity":"CRITICAL"}]}
    }',
   1, NOW()
  );

-- =========================
-- Análisis (AnalysisRun)
-- =========================

INSERT INTO analysis_runs (
  id, user_id, repo_id, policy_id, endpoint_id,
  base_branch, target_branch, status_code,
  started_at, finished_at, duration_ms, created_at
) VALUES (
  'run-001', 'u-001', 'r-001', 'p-001', 'e-001',
  'main', 'feature/refactor-service', 'SUCCESS',
  NOW() - INTERVAL 15 SECOND, NOW(), 15000, NOW()
);

-- =========================
-- Diff Files
-- =========================

INSERT INTO diff_files (id, run_id, path, change_type_code, additions, deletions) VALUES
  ('d-001', 'run-001', 'src/service/UserService.java', 'MODIFIED', 25, 4),
  ('d-002', 'run-001', 'src/model/User.java', 'ADDED', 42, 0),
  ('d-003', 'run-001', 'README.md', 'MODIFIED', 5, 1);

-- =========================
-- Findings (hallazgos simulados)
-- =========================

INSERT INTO findings (
  id, run_id, code, title, description, severity_code, file_path, line_start, line_end, category, created_at
) VALUES
  ('f-001', 'run-001', 'SEC-001', 'Uso de contraseña hardcodeada',
   'Se detectó una cadena sensible en la clase UserService', 'CRITICAL',
   'src/service/UserService.java', 120, 125, 'security', NOW()),
  ('f-002', 'run-001', 'PERF-002', 'Consulta SQL sin índice',
   'Posible impacto en rendimiento: consulta sin índice en campo usuario', 'HIGH',
   'src/service/UserService.java', 210, 215, 'performance', NOW()),
  ('f-003', 'run-001', 'STYLE-005', 'Método con demasiadas líneas',
   'Refactor sugerido para cumplir con estándares de legibilidad', 'LOW',
   'src/service/UserService.java', 40, 90, 'style', NOW()),
  ('f-004', 'run-001', 'MAIN-010', 'Clase sin comentarios Javadoc',
   'Agregar documentación de clase para mejorar mantenibilidad', 'MEDIUM',
   'src/model/User.java', 1, 10, 'maintainability', NOW());

-- =========================
-- Métricas agregadas (UserStats y Snapshots)
-- =========================

INSERT INTO user_stats (
  id, user_id, period_start, period_end,
  analyses_count, findings_count,
  critical_count, high_count, medium_count, low_count, created_at
) VALUES (
  'us-001', 'u-001', DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(),
  3, 12, 2, 3, 4, 3, NOW()
);

INSERT INTO metrics_snapshots (
  id, period_start, period_end,
  total_analyses, total_findings,
  critical_count, high_count, medium_count, low_count,
  avg_severity_score, created_at
) VALUES (
  'ms-001', DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(),
  10, 40, 6, 10, 12, 12, 2.75, NOW()
);

-- =========================
-- Vista de Dashboard del TL
-- =========================

INSERT INTO dashboard_views (id, owner_user_id, filters_json, created_at) VALUES
  ('dash-001', 'u-002', '{"period":"last7days","severity":["CRITICAL","HIGH"]}', NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- END OF SEED
-- =========================
