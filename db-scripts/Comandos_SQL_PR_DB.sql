-- =========================================================
-- Comandos SQL (Plantillas) - Analizador de PR (MySQL 8)
-- Esquema asumido: proyecto_db
-- NOTA: Reemplace los <placeholders> por valores reales antes de ejecutar.
-- =========================================================
USE proyecto_db;

-- =========================
-- Tabla: usuario
-- =========================
-- INSERT
INSERT INTO usuario (nombre, email, rol, activo)
VALUES ('Juan Perez', 'juan.perez@acme.com', 'ADMIN', TRUE);
-- SELECT
SELECT id, nombre, email, rol, activo, created_at
FROM usuario
WHERE id = <id_usuario>;
-- DELETE
DELETE FROM usuario WHERE id = <id_usuario>;

-- =========================
-- Tabla: repo
-- =========================
-- INSERT
INSERT INTO repo (proveedor, nombre, slug, remoto_url, external_id, activo)
VALUES ('GITHUB', 'acme/app', 'acme-app', 'https://github.com/acme/app', '123456', TRUE);
-- SELECT
SELECT * FROM repo WHERE id = <id_repo>;
-- DELETE
DELETE FROM repo WHERE id = <id_repo>;

-- =========================
-- Tabla: politica
-- =========================
-- INSERT
INSERT INTO politica (nombre, version, activa)
VALUES ('Default', '1.0.0', TRUE);
-- SELECT
SELECT * FROM politica WHERE id = <id_politica>;
-- DELETE
DELETE FROM politica WHERE id = <id_politica>;

-- =========================
-- Tabla: plantilla_notificacion
-- =========================
-- INSERT
INSERT INTO plantilla_notificacion (codigo, asunto_template, cuerpo_template, descripcion)
VALUES ('ALERTA_RIESGO', 'Alerta: {{nivel}}', 'Se detectó riesgo {{nivel}} en PR {{pr}}', 'Template de alerta de riesgo');
-- SELECT
SELECT * FROM plantilla_notificacion WHERE id = <id_plantilla>;
-- DELETE
DELETE FROM plantilla_notificacion WHERE id = <id_plantilla>;

-- =========================
-- Tabla: reporte
-- =========================
-- INSERT
INSERT INTO reporte (nombre, descripcion, formato, definicion_sql, activo, created_by)
VALUES ('Resumen riesgos', 'Riesgos por PR', 'CSV', 'SELECT ...', TRUE, <id_usuario_autor>);
-- SELECT
SELECT * FROM reporte WHERE id = <id_reporte>;
-- DELETE
DELETE FROM reporte WHERE id = <id_reporte>;

-- =========================
-- Tabla: pull_request
-- =========================
-- INSERT
INSERT INTO pull_request (repo_id, numero, titulo, autor_id, estado, base_sha, head_sha, link_url)
VALUES (<id_repo>, 42, 'Fix login bug', <id_usuario_autor>, 'OPEN', 'abcdef123456...', '123456abcdef...', 'https://github.com/acme/app/pull/42');
-- SELECT
SELECT * FROM pull_request WHERE repo_id = <id_repo> AND numero = <numero_pr>;
-- DELETE
DELETE FROM pull_request WHERE id = <id_pr>;

-- =========================
-- Tabla: pr_commit
-- =========================
-- INSERT
INSERT INTO pr_commit (pr_id, sha, parent_sha, autor_nombre, autor_email, authored_at, committed_at, mensaje, stats_add, stats_del, stats_files)
VALUES (<id_pr>, '9c1c...abc', '7b2b...def', 'Juan', 'juan@acme.com', '2025-09-01 10:00:00', '2025-09-01 10:05:00', 'Refactor login', 120, 30, 5);
-- SELECT
SELECT * FROM pr_commit WHERE pr_id = <id_pr> ORDER BY committed_at DESC;
-- DELETE
DELETE FROM pr_commit WHERE id = <id_commit>;

-- =========================
-- Tabla: pr_file_change
-- =========================
-- INSERT
INSERT INTO pr_file_change (pr_id, path, status, additions, deletions, changes, module, language, blob_id)
VALUES (<id_pr>, 'src/auth/LoginService.java', 'modified', 25, 10, 35, 'auth', 'java', 'blob123');
-- SELECT
SELECT path, status, additions, deletions, module
FROM pr_file_change
WHERE pr_id = <id_pr>;
-- DELETE
DELETE FROM pr_file_change WHERE id = <id_change>;

-- =========================
-- Tabla: pr_status_history
-- =========================
-- INSERT
INSERT INTO pr_status_history (pr_id, contexto, estado, descripcion, target_url)
VALUES (<id_pr>, 'risk-check', 'success', 'Riesgo bajo', 'https://ci.example/job/123');
-- SELECT
SELECT * FROM pr_status_history WHERE pr_id = <id_pr> ORDER BY created_at DESC;
-- DELETE
DELETE FROM pr_status_history WHERE id = <id_status>;

-- =========================
-- Tabla: umbral
-- =========================
-- INSERT
INSERT INTO umbral (politica_id, warn, block, max_tiempo_seg)
VALUES (<id_politica>, 'MEDIO', 'ALTO', 300);
-- SELECT
SELECT * FROM umbral WHERE politica_id = <id_politica>;
-- DELETE
DELETE FROM umbral WHERE id = <id_umbral>;

-- =========================
-- Tabla: regla
-- =========================
-- INSERT
INSERT INTO regla (politica_id, codigo, tipo, severidad, parametros_json, activa)
VALUES (<id_politica>, 'DEP-001', 'DEPENDENCY', 'ALTO', '{"maxDepth":3}', TRUE);
-- SELECT
SELECT * FROM regla WHERE politica_id = <id_politica> AND activa = TRUE;
-- DELETE
DELETE FROM regla WHERE id = <id_regla>;

-- =========================
-- Tabla: modulo_critico
-- =========================
-- INSERT
INSERT INTO modulo_critico (politica_id, patron)
VALUES (<id_politica>, 'payments/*');
-- SELECT
SELECT * FROM modulo_critico WHERE politica_id = <id_politica>;
-- DELETE
DELETE FROM modulo_critico WHERE id = <id_modcritico>;

-- =========================
-- Tabla: ruta_sensible
-- =========================
-- INSERT
INSERT INTO ruta_sensible (politica_id, patron)
VALUES (<id_politica>, 'config/*.yml');
-- SELECT
SELECT * FROM ruta_sensible WHERE politica_id = <id_politica>;
-- DELETE
DELETE FROM ruta_sensible WHERE id = <id_ruta>;

-- =========================
-- Tabla: exclusion
-- =========================
-- INSERT
INSERT INTO exclusion (politica_id, patron, motivo)
VALUES (<id_politica>, 'docs/**', 'Cambios de documentación');
-- SELECT
SELECT * FROM exclusion WHERE politica_id = <id_politica>;
-- DELETE
DELETE FROM exclusion WHERE id = <id_exclusion>;

-- =========================
-- Tabla: analisis_pr
-- =========================
-- INSERT
INSERT INTO analisis_pr (repo_id, pr_id, base_sha, head_sha, politica_id, politica_version, riesgo, puntaje, parcial, tiempo_ms, artefacto_json, artefacto_pdf, resumen)
VALUES (<id_repo>, <id_pr>, 'abcdef', '123456', <id_politica>, '1.0.0', 'ALTO', 76.50, FALSE, 145000, 'storage/pr42.json', 'storage/pr42.pdf', 'Riesgos en auth y pagos');
-- SELECT
SELECT id, riesgo, puntaje, parcial, tiempo_ms
FROM analisis_pr
WHERE pr_id = <id_pr>
ORDER BY started_at DESC
LIMIT 1;
-- DELETE
DELETE FROM analisis_pr WHERE id = <id_analisis>;

-- =========================
-- Tabla: analisis_pr_impacto
-- =========================
-- INSERT
INSERT INTO analisis_pr_impacto (analisis_id, categoria, severidad, archivo, modulo, evidencia_json)
VALUES (<id_analisis>, 'DEPENDENCY', 'ALTO', 'src/auth/LoginService.java', 'auth', '{"reason":"import"}');
-- SELECT
SELECT categoria, severidad, archivo, modulo
FROM analisis_pr_impacto
WHERE analisis_id = <id_analisis>;
-- DELETE
DELETE FROM analisis_pr_impacto WHERE id = <id_impacto>;

-- =========================
-- Tabla: analisis_pr_modulo
-- =========================
-- INSERT
INSERT INTO analisis_pr_modulo (analisis_id, modulo, razon, evidencia_json)
VALUES (<id_analisis>, 'payments', 'contract', '{"iface":"PaymentGateway"}');
-- SELECT
SELECT modulo, razon
FROM analisis_pr_modulo
WHERE analisis_id = <id_analisis>;
-- DELETE
DELETE FROM analisis_pr_modulo WHERE id = <id_modulo>;

-- =========================
-- Tabla: silenciamiento_pr
-- =========================
-- INSERT
INSERT INTO silenciamiento_pr (repo_id, pr_number, hasta, motivo)
VALUES (<id_repo>, 42, '2025-12-31 23:59:59', 'Silencio temporal por investigación');
-- SELECT
SELECT * FROM silenciamiento_pr WHERE repo_id = <id_repo> AND pr_number = <num_pr>;
-- DELETE
DELETE FROM silenciamiento_pr WHERE repo_id = <id_repo> AND pr_number = <num_pr>;

-- =========================
-- Tabla: notificacion
-- =========================
-- INSERT
INSERT INTO notificacion (canal, asunto, cuerpo, usuario_id, relacionada_tipo, relacionada_id, estado)
VALUES ('CLI', 'Riesgo Alto en PR #42', 'Resumen: ...', <id_usuario>, 'PR', <id_pr>, 'PENDIENTE');
-- SELECT
SELECT * FROM notificacion
WHERE relacionada_tipo = 'PR' AND relacionada_id = <id_pr>
ORDER BY created_at DESC;
-- DELETE
DELETE FROM notificacion WHERE id = <id_notif>;

-- =========================
-- Tabla: reporte_filtro
-- =========================
-- INSERT
INSERT INTO reporte_filtro (reporte_id, nombre, tipo, valor_default, requerido)
VALUES (<id_reporte>, 'fecha_desde', 'date', NULL, TRUE);
-- SELECT
SELECT * FROM reporte_filtro WHERE reporte_id = <id_reporte>;
-- DELETE
DELETE FROM reporte_filtro WHERE id = <id_rep_filtro>;

-- =========================
-- Tabla: reporte_programacion
-- =========================
-- INSERT
INSERT INTO reporte_programacion (reporte_id, cron_expr, activo)
VALUES (<id_reporte>, '0 7 * * *', TRUE);
-- SELECT
SELECT * FROM reporte_programacion WHERE reporte_id = <id_reporte>;
-- DELETE
DELETE FROM reporte_programacion WHERE id = <id_rep_prog>;

-- =========================
-- Tabla: reporte_ejecucion
-- =========================
-- INSERT
INSERT INTO reporte_ejecucion (reporte_id, ejecutado_por, estado, parametros_json, resultado_path)
VALUES (<id_reporte>, <id_usuario>, 'PENDIENTE', '{"fecha_desde":"2025-10-01"}', 'storage/rep-001.csv');
-- SELECT
SELECT estado, resultado_path, started_at, ended_at
FROM reporte_ejecucion
WHERE reporte_id = <id_reporte>
ORDER BY started_at DESC;
-- DELETE
DELETE FROM reporte_ejecucion WHERE id = <id_rep_ejec>;

-- =========================
-- Tabla: auditoria
-- =========================
-- INSERT
INSERT INTO auditoria (entidad_tipo, entidad_id, accion, actor_id, detalle_json)
VALUES ('PR', <id_pr>, 'STATUS', <id_usuario>, '{"from":"warning","to":"failure"}');
-- SELECT
SELECT * FROM auditoria
WHERE entidad_tipo = 'PR' AND entidad_id = <id_pr>
ORDER BY creado_en DESC;
-- DELETE
DELETE FROM auditoria WHERE id = <id_audit>;

-- =========================
-- Tabla: metric_event
-- =========================
-- INSERT
INSERT INTO metric_event (name, valor, dimensiones_json, trace_id)
VALUES ('t_analisis_ms', 145000, '{"repo":"acme/app","pr":42}', 'trace-xyz');
-- SELECT
SELECT * FROM metric_event
WHERE name = 't_analisis_ms' AND ts >= '2025-10-01'
ORDER BY ts DESC;
-- DELETE
DELETE FROM metric_event WHERE id = <id_metric>;

-- =========================
-- FIN DEL ARCHIVO
-- =========================
