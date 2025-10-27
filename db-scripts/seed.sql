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
  (1, 'Demo Developer', 'demo@example.com', 'DEVELOPER', 1, NOW()),
  (2, 'Team Lead', 'lead@example.com', 'TECH_LEAD', 1, NOW()),
  (3, 'QA Tester', 'qa@example.com', 'QA', 1, NOW()),
  (4, 'Admin User', 'admin@example.com', 'ADMIN', 1, NOW());

-- =========================
-- Repositorio
-- =========================

INSERT INTO repositories (id, local_path, vcs, created_at) VALUES
  (1, 'C:\\projects\\sample-repo', 'git', NOW());

-- =========================
-- Políticas de severidad
-- =========================

INSERT INTO severity_policies (id, name, rules_json, version, effective_from, created_at) VALUES
  (1, 'Default Policy',
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
  (1, 'Mock Local Endpoint', '1.0.0',
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
  base_branch, target_branch, status_code, total_files, total_findings,
  started_at, finished_at, duration_ms, created_at
) VALUES (
  1, 1, 1, 1, 1,
  'main', 'feature/refactor-service', 'SUCCESS', 5, 8,
  NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 50 MINUTE, 600000, NOW()
);

-- =========================
-- Diff Files
-- =========================

INSERT INTO diff_files (id, run_id, path, change_type_code, additions, deletions) VALUES
  (1, 1, 'src/service/UserService.java', 'MODIFIED', 25, 10),
  (2, 1, 'src/controller/UserController.java', 'MODIFIED', 15, 5),
  (3, 1, 'src/model/User.java', 'ADDED', 42, 0),
  (4, 1, 'README.md', 'MODIFIED', 3, 1),
  (5, 1, 'pom.xml', 'MODIFIED', 5, 2);

-- =========================
-- Findings (hallazgos simulados)
-- =========================

INSERT INTO findings (
  id, run_id, code, title, description, severity_code, file_path, line_start, line_end, category, created_at
) VALUES
  (1, 1, 'SEC-001', 'Hardcoded Password',
   'Password found in source code', 'CRITICAL',
   'src/service/UserService.java', 120, 120, 'security', NOW()),
  (2, 1, 'PERF-002', 'N+1 Query Detected',
   'Possible N+1 query pattern found', 'HIGH',
   'src/service/UserService.java', 85, 90, 'performance', NOW()),
  (3, 1, 'STYLE-005', 'Method Too Long',
   'Method exceeds 50 lines', 'LOW',
   'src/service/UserService.java', 45, 98, 'style', NOW()),
  (4, 1, 'SEC-002', 'SQL Injection Risk',
   'Unvalidated user input in SQL query', 'CRITICAL',
   'src/controller/UserController.java', 34, 36, 'security', NOW()),
  (5, 1, 'MAIN-010', 'Missing Javadoc',
   'Public method without documentation', 'MEDIUM',
   'src/controller/UserController.java', 50, 50, 'maintainability', NOW()),
  (6, 1, 'MAIN-011', 'Class Missing Javadoc',
   'Public class needs documentation', 'MEDIUM',
   'src/model/User.java', 1, 1, 'maintainability', NOW()),
  (7, 1, 'STYLE-003', 'Magic Number',
   'Avoid magic numbers, use constants', 'LOW',
   'src/model/User.java', 25, 25, 'style', NOW()),
  (8, 1, 'SEC-003', 'Vulnerable Dependency',
   'Dependency has known CVE', 'HIGH',
   'pom.xml', 42, 44, 'security', NOW());

-- =========================
-- Métricas agregadas (UserStats y Snapshots)
-- =========================

INSERT INTO user_stats (
  id, user_id, period_start, period_end,
  analyses_count, findings_count,
  critical_count, high_count, medium_count, low_count, created_at
) VALUES (
  1, 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(),
  3, 12, 2, 3, 4, 3, NOW()
);

INSERT INTO metrics_snapshots (
  id, period_start, period_end,
  total_analyses, total_findings,
  critical_count, high_count, medium_count, low_count,
  avg_severity_score, created_at
) VALUES (
  1, DATE_SUB(CURDATE(), INTERVAL 7 DAY), CURDATE(),
  10, 40, 6, 10, 12, 12, 2.75, NOW()
);

-- =========================
-- Vista de Dashboard del TL
-- =========================

INSERT INTO dashboard_views (id, owner_user_id, filters_json, created_at) VALUES
  (1, 2, '{"period":"last7days","severity":["CRITICAL","HIGH"]}', NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- END OF SEED
-- =========================
